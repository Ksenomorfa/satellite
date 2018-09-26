package ru.spbstu.ioffe.satellite;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.InertialProvider;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;

public class CoordinateCalculator {

    public List<PeriodOfPresence> calculateCoordinates(List<TLE> tles, TLEReader reader) throws OrekitException {
        List<PeriodOfPresence> periods = new ArrayList<>();
        TLE tleLoaded = getTleBasedOnTLEAndStartDates(tles, reader);
        LocalDate dayStart = reader.getStart();
        long period = reader.getPeriod();

        TLEPropagator sgp4 = TLEPropagator.selectExtrapolator(tleLoaded,
                InertialProvider.EME2000_ALIGNED, Constants.satelliteMass);

        AbsoluteDate dateStartToPropogate = new AbsoluteDate(dayStart.getYear(),
                Utils.toOrekitMonth(dayStart.getMonth()),
                dayStart.getDayOfMonth(),
                TimeScalesFactory.getUTC());
        System.out.println("TLE loaded from: " + tleLoaded.getDate() + ". Date start to propogate: " + dateStartToPropogate);

        PVCoordinates pvCoordinates = sgp4.getPVCoordinates(dateStartToPropogate);
        System.out.println("PV coordinates:" + pvCoordinates);

        boolean satelliteIn = false;
        boolean satelliteOut = false;
        PeriodOfPresence periodOfPresence = new PeriodOfPresence();
        System.out.println("Calculating periods ...");

        // Calculation precision is 0.1 s
        for (double i = 0; i < 3600 * 24 * period; i = i + 0.1) {
            AbsoluteDate newDate = dateStartToPropogate.shiftedBy(i);
            pvCoordinates = sgp4.propagate(newDate).getPVCoordinates();
            Vector3D position = pvCoordinates.getPosition();

            double julianDate = Utils.julianDate(newDate.toString().substring(0, 10),
                    newDate.toString().substring(11, 19));

            ECEF ecef = FormatsConverter.teme2ecef(position, julianDate);
            LLA lla = FormatsConverter.ecef2lla(ecef);

            double neededDistance = neededDistanceThroughHorizon(lla);
            double actualDistance = actualDistanceSatellitePlace(ecef);
            //System.out.println("TIME UTC: " + newDate);
            //System.out.println("needed distance: " + neededDistance);
            //System.out.println("actual distance: " + actualDistance);

            if (actualDistance <= neededDistance && !satelliteIn) {
                periodOfPresence.setPresenceFrom(newDate);
                satelliteIn = true;
                //System.out.println("Satellite in zone: " + newDate);
            }
            if (actualDistance > neededDistance && !satelliteOut && satelliteIn) {
                periodOfPresence.setPresenceTo(newDate);
                satelliteOut = true;
                //System.out.println("Satellite out of zone: " + newDate);
            }
            if (periodOfPresence.getPresenceFrom() != null && periodOfPresence.getPresenceTo() != null) {
                periods.add(periodOfPresence);
                periodOfPresence = new PeriodOfPresence();
                satelliteIn = false;
                satelliteOut = false;
            }

            //System.out.println("  ECEF: x: " + ecef.getX() + " y: " + ecef.getY() + " z: " + ecef.getZ() + " r: " + r);
            //System.out.println("  LLA: l: " + Math.toDegrees(lla.getLatitude()) + " l: " + Math.toDegrees(lla.getLongitude())
            //       + " a: " + lla.getAltitude());
        }
        return periods;
    }

    private TLE getTleBasedOnTLEAndStartDates(List<TLE> tles, TLEReader reader) throws OrekitException {
        TLE tleLoaded = null;
        for (TLE tle : tles) {
            String tleDate = tle.getDate().toString(TimeScalesFactory.getUTC()).substring(0, 10);
            String readerDate = reader.getTleDateStart().format(Utils.dateFormatter).substring(0, 10);

            if (tle.getDate().toString(TimeScalesFactory.getUTC()).substring(0, 10)
                    .equals(reader.getTleDateStart().format(Utils.dateFormatter).substring(0, 10))) {
                tleLoaded = tle;
                break;
            } else if (LocalDate.parse(tleDate).isAfter(LocalDate.parse(readerDate))) {
                tleLoaded = tles.get(0);
                break;
            } else if (LocalDate.parse(tleDate).isBefore(LocalDate.parse(readerDate))) {
                tleLoaded = tles.get(tles.size() - 1);
                break;
            }
        }
        return tleLoaded;
    }

    private double actualDistanceSatellitePlace(ECEF ecef) {
        double x1 = ecef.getX();
        double y1 = ecef.getY();
        double z1 = ecef.getZ();

        LLA placeLLA = new LLA(Constants.latitude * Math.PI / 180, Constants.longitude * Math.PI / 180, 0);

        ECEF placeECEF = FormatsConverter.lla2ecef(placeLLA);
        double x2 = placeECEF.getX();
        double y2 = placeECEF.getY();
        double z2 = placeECEF.getZ();

        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    private double neededDistanceThroughHorizon(LLA lla) {
        double delta = Constants.deltaAngle / 180 * Math.PI;
        double height = lla.getAltitude();
        double earthRadius = Constants.earthRadius;

        return Math.sqrt(Math.pow(earthRadius + height, 2) - Math.pow(earthRadius, 2) * Math.pow(cos(delta), 2))
                - earthRadius * Math.sin(delta);
    }

}
