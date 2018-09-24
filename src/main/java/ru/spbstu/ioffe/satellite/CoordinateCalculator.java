package ru.spbstu.ioffe.satellite;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.InertialProvider;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CoordinateCalculator {
    List<Periodis> periods = new ArrayList<>();

    public void calculateCoordinates(List<TLE> tles, TLEReader reader) throws OrekitException {
        LocalDate dayStart = reader.getStart();
        long period = reader.getPeriod();
        TLE tle = tles.get(0);

        AbsoluteDate dateStart = tle.getDate();
        TLEPropagator sgp4 = TLEPropagator.selectExtrapolator(tle,
                InertialProvider.EME2000_ALIGNED, Constants.satelliteMass);

        if (dayStart.isAfter(LocalDate.parse(tle.getDate().toString().substring(0, 10)))) {
            System.out.println("Something wrong. The needed date isn't presented in TLE. Date will be changed to the latest in TLE: "
                    + tles.get(tles.size() - 1).getDate().toString().substring(0, 10));
            dateStart = tles.get(tles.size() - 1).getDate();
        }

        PVCoordinates pvCoordinates = sgp4.getPVCoordinates(dateStart);
        System.out.println(pvCoordinates);

        //2458373.329873
        for (int i = 0; i < 3600 * 24 * period; i = i + 60) {
            AbsoluteDate newDate = dateStart.shiftedBy(i);
            pvCoordinates = sgp4.propagate(newDate).getPVCoordinates();
            Vector3D position = pvCoordinates.getPosition();

            double julianDate = Utils.julianDate(newDate.toString().substring(0,10), newDate.toString().substring(11, 19));
            System.out.println("Julian date:" + julianDate);

            ECEF ecef = FormatsConverter.teme2ecef(position, julianDate, newDate);
            LLA lla = FormatsConverter.ecef2lla(ecef);

            System.out.println("TIME UTC: " + newDate);
            double r = Math.sqrt(Math.pow(ecef.getX(), 2) + Math.pow(ecef.getY(), 2) + Math.pow(ecef.getZ(), 2));

            System.out.println("  ECEF: x: " + ecef.getX() + " y: " + ecef.getY() + " z: " + ecef.getZ() + " r: " + r);
            System.out.println("  LLA: l: " + Math.toDegrees(lla.getLatitude()) + " l: " + Math.toDegrees(lla.getLongitude())
                    + " a: " + lla.getAltitude());

        }
    }

}
