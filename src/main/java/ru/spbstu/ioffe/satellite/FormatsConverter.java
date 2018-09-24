package ru.spbstu.ioffe.satellite;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static ru.spbstu.ioffe.satellite.Utils.norm;

public class FormatsConverter {
    /**
     * ECEF - Earth Centered Earth Fixed
     * LLA - Lat Lon Alt
     */
    private static final double asq = Math.pow(Constants.earthRadius, 2);
    private static final double esq = Math.pow(Constants.eccentricity, 2);

      /**
     * longitude in radians.
     * latitude in radians.
     * altitude in meters.
     * @param ecef
     */
    public static LLA ecef2lla(ECEF ecef) {
        double x = ecef.getX();
        double y = ecef.getY();
        double z = ecef.getZ();

        double b = Math.sqrt(asq * (1 - esq));
        double bsq = Math.pow(b, 2);
        double ep = Math.sqrt((asq - bsq) / bsq);
        double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double th = Math.atan2(Constants.earthRadius * z, b * p);

        double lon = Math.atan2(y, x);
        double lat = Math.atan2((z + Math.pow(ep, 2) * b * Math.pow(Math.sin(th), 3)), (p - esq * Constants.earthRadius * Math.pow(Math.cos(th), 3)));
        double N = Constants.earthRadius / (Math.sqrt(1 - esq * Math.pow(Math.sin(lat), 2)));
        double alt = p / Math.cos(lat) - N;

        // mod lat to 0-2pi
        lon = lon % (2 * Math.PI);
        double rArray[] = new double[]{x, y, z};

        if (norm(rArray) <= 0) {
            lon = 0.0;
            lat = 0.0;
            alt = -Constants.earthRadius;
        }
        return new LLA(lat, lon, alt);
    }

    public static ECEF lla2ecef(LLA lla) {
        double lat = lla.getLatitude();
        double lon = lla.getLongitude();
        double alt = lla.getAltitude();

        double N = Constants.earthRadius / Math.sqrt(1 - esq * Math.pow(Math.sin(lat), 2));

        double x = (N + alt) * Math.cos(lat) * Math.cos(lon);
        double y = (N + alt) * Math.cos(lat) * Math.sin(lon);
        double z = ((1 - esq) * N + alt) * Math.sin(lat);

        return new ECEF(x, y, z);
    }

    public static ECEF teme2ecef(Vector3D teme, double julianDate, AbsoluteDate date) {
        double gmst = 0;
        double st[][] = new double[3][3];
        double rpef[] = new double[3];
        double pm[][] = new double[3][3];

        //Get Greenwich mean sidereal time
        try {
            gmst = greenwichMeanSidereal(julianDate, date);
        } catch (OrekitException e) {
            e.printStackTrace();
        }

        //st is the pef - tod matrix
        st[0][0] = cos(gmst);
        st[0][1] = -sin(gmst);
        st[0][2] = 0.0;
        st[1][0] = sin(gmst);
        st[1][1] = cos(gmst);
        st[1][2] = 0.0;
        st[2][0] = 0.0;
        st[2][1] = 0.0;
        st[2][2] = 1.0;

        //Get pseudo earth fixed position vector by multiplying the inverse pef-tod matrix by rteme
        rpef[0] = st[0][0] * teme.getX() + st[1][0] * teme.getY() + st[2][0] * teme.getZ();
        rpef[1] = st[0][1] * teme.getX() + st[1][1] * teme.getY() + st[2][1] * teme.getZ();
        rpef[2] = st[0][2] * teme.getX() + st[1][2] * teme.getY() + st[2][2] * teme.getZ();

        //Get polar motion vector
        polarm(julianDate, pm);

        //ECEF postion vector is the inverse of the polar motion vector multiplied by rpef
        double x = pm[0][0] * rpef[0] + pm[1][0] * rpef[1] + pm[2][0] * rpef[2];
        double y = pm[0][1] * rpef[0] + pm[1][1] * rpef[1] + pm[2][1] * rpef[2];
        double z = pm[0][2] * rpef[0] + pm[1][2] * rpef[1] + pm[2][2] * rpef[2];

        return new ECEF(x, y, z);
    }

    static void polarm(double jdut1, double pm[][]) {
        double MJD; //Julian Date - 2,400,000.5 days
        double A;
        double C;
        double xp; //Polar motion coefficient in radians
        double yp; //Polar motion coefficient in radians

        //Predict polar motion coefficients using IERS Bulletin - A (Vol. XXVIII No. 030)
        MJD = jdut1 - 2400000.5;

        A = 2 * Math.PI * (MJD - 57226) / 365.25;
        C = 2 * Math.PI * (MJD - 57226) / 435;

        xp = (0.1033 + 0.0494 * cos(A) + 0.0482 * sin(A) + 0.0297 * cos(C) + 0.0307 * sin(C)) * 4.84813681e-6;
        yp = (0.3498 + 0.0441 * cos(A) - 0.0393 * sin(A) + 0.0307 * cos(C) - 0.0297 * sin(C)) * 4.84813681e-6;

        pm[0][0] = cos(xp);
        pm[0][1] = 0.0;
        pm[0][2] = -sin(xp);
        pm[1][0] = sin(xp) * sin(yp);
        pm[1][1] = cos(yp);
        pm[1][2] = cos(xp) * sin(yp);
        pm[2][0] = sin(xp) * cos(yp);
        pm[2][1] = -sin(yp);
        pm[2][2] = cos(xp) * cos(yp);
    }

    /**
     * Calculates the Greenwich mean sidereal time (GMST) on julDate (doesn't have to be 0h).
     * Used calculations from Meesus 2nd ed.
     *
     * @param jdut1 Julian Date
     * @return Greenwich mean sidereal time in degrees (0-360)
     */
    public static double greenwichMeanSidereal(double jdut1, AbsoluteDate date) throws OrekitException {
        double Tu = (jdut1 - 2451545)/36525.0;
        System.out.println("Tu: " + Tu);
        double H0 = 24110.54481 + 8640184.812866*Tu + 0.093104* Tu*Tu -
        6.2 * Tu * Tu* Tu * Math.pow(10, -6);
//        double w = 1.00273790935 + 5.9 * Tu * Math.pow(10, -11);
//        double t = date.toDate(TimeScalesFactory.getUTC()).getHours()*60*60 +
//                date.toDate(TimeScalesFactory.getUTC()).getMinutes()*60 +
//                date.toDate(TimeScalesFactory.getUTC()).getSeconds();

//        double mjd = jdut1 - 2400000.5;
//        double mjd2000 = 51544.5;
//        double int_mjd = Math.floor(mjd);
//        double frac_mjd = mjd - int_mjd;
//        double Tu = (int_mjd - mjd2000) / 36525.0;
//        double gmst = 24110.54841 + Tu * (8640184.812866 + Tu * (0.093104 - Tu * 6.2e-6));
//        gmst = gmst + frac_mjd * 86400 * 1.00273790934 % 86400;
//        gmst = gmst / 3600;
        //double gmst = H0 + w * t;
        System.out.println("gmst: " + H0);
        return H0/3600;
    }
}
