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

    public List<Periodis> calculateCoordinates(List<TLE> tles, TLEReader reader) throws OrekitException {
        List<Periodis> periods = new ArrayList<>();

        LocalDate dayStart = reader.getTleDateStart();
        long period = reader.getPeriod();
        TLE tle = tles.get(0);

        AbsoluteDate dateStart = new AbsoluteDate(dayStart.getYear(), Utils.toOrekitMonth(dayStart.getMonth()),
                dayStart.getDayOfMonth(), TimeScalesFactory.getUTC());
        TLEPropagator sgp4 = TLEPropagator.selectExtrapolator(tle,
                InertialProvider.EME2000_ALIGNED, Constants.satelliteMass);

        PVCoordinates pvCoordinates = sgp4.getPVCoordinates(dateStart);
        System.out.println(pvCoordinates);

        //2458373.329873
        boolean satelliteIn = false;
        boolean satelliteOut = false;
        Periodis periodOfExistence = new Periodis();

        for (int i = 0; i < 3600 * 24 * period; i++) {
            AbsoluteDate newDate = dateStart.shiftedBy(i);
            pvCoordinates = sgp4.propagate(newDate).getPVCoordinates();
            Vector3D position = pvCoordinates.getPosition();

            double julianDate = Utils.julianDate(newDate.toString().substring(0, 10), newDate.toString().substring(11, 19));
           // System.out.println("Julian date:" + julianDate);

            ECEF ecef = FormatsConverter.teme2ecef(position, julianDate, newDate);
            LLA lla = FormatsConverter.ecef2lla(ecef);
            double neededDistance = neededDistanceThroughHorizont(lla);
            //System.out.println("needed distance: " + neededDistance);
            double actualDistance = actualDistanceSatellitePlace(ecef);
            //System.out.println("actual distance: " + actualDistance);
            if (actualDistance <= neededDistance && !satelliteIn) {
                periodOfExistence.setFrom(newDate);
                satelliteIn = true;
                System.out.println("Satellite in: " + newDate);
            }

            if (actualDistance > neededDistance && !satelliteOut && satelliteIn) {
                periodOfExistence.setTo(newDate);
                System.out.println("Satellite out: " + newDate);
                satelliteOut = true;
                System.out.println(periodOfExistence);
            }

            if (periodOfExistence.getFrom() != null && periodOfExistence.getTo() != null) {
                periods.add(periodOfExistence);
                System.out.println("period old:" + periodOfExistence);
                int idex = periods.indexOf(periodOfExistence);
                periodOfExistence = new Periodis();
                System.out.println(" period index: " + periods.get(idex));
                satelliteIn = false;
                satelliteOut = false;
            }

            //System.out.println("TIME UTC: " + newDate);

            //System.out.println("  ECEF: x: " + ecef.getX() + " y: " + ecef.getY() + " z: " + ecef.getZ() + " r: " + r);
            //System.out.println("  LLA: l: " + Math.toDegrees(lla.getLatitude()) + " l: " + Math.toDegrees(lla.getLongitude())
              //      + " a: " + lla.getAltitude());
        }
        return periods;
    }

    private double actualDistanceSatellitePlace(ECEF ecef) {
        double x1 = ecef.getX();
        double y1 = ecef.getY();
        double z1 = ecef.getZ();

        LLA placeLLA = new LLA(Constants.latitude * Math.PI / 180, Constants.longitude * Math.PI / 180, 0);
        //System.out.println("latitude: " + Constants.latitude + " " + Constants.longitude);
        //System.out.println(placeLLA.getLatitude() + " " + placeLLA.getLongitude() + " " + placeLLA.getAltitude());

        ECEF placeECEF = FormatsConverter.lla2ecef(placeLLA);
        double x2 = placeECEF.getX();
        double y2 = placeECEF.getY();
        double z2 = placeECEF.getZ();
        double r = Math.sqrt(x2*x2 + y2*y2 + z2*z2);

        //System.out.println("PLACE: " + x2+ " " + y2 + " " + z2 + " r: " + r);
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
       // System.out.println("distance: " + distance);

        return distance;
    }

    private double neededDistanceThroughHorizont(LLA lla) {
        int delta = 15;
        double height = lla.getAltitude();
        double earthRadius = Constants.earthRadius;
        double beta = Math.acos(cos(delta) / (1 + height / earthRadius)) - delta;
        double x = (earthRadius + height)* Math.sin(beta) / Math.sin(delta + 90);
        //System.out.println("beta angle: " + beta + " distance needed to satellite: " + x);

        return x;
    }

}
