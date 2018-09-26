package ru.spbstu.ioffe.satellite;

import org.orekit.time.Month;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utils {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    public static org.orekit.time.Month toOrekitMonth(java.time.Month javaMonth) {
        org.orekit.time.Month orekitMonth = Month.getMonth(javaMonth.getValue());
        return orekitMonth;
    }

    /**
     * vector 2-norm
     * @param a vector of length 3
     * @return norm(a)
     */
    public static double norm(double[] a) {
        double c = 0.0;

        for (int i = 0; i < a.length; i++) {
            c += a[i] * a[i];
        }

        return Math.sqrt(c);
    }

    public static int JGREG = 15 + 31 * (10 + 12 * 1582);

    public static double julianDate(String date, String time) {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        double hour = (Integer.parseInt(time.substring(0, 2)) - 12) / 24.0;
        double minute = Integer.parseInt(time.substring(3, 5)) / 1440.0;
        double second = Integer.parseInt(time.substring(6, 8)) / 86400.0;

        int julianYear = year;
        if (year < 0) julianYear++;
        int julianMonth = month;
        if (month > 2) {
            julianMonth++;
        } else {
            julianYear--;
            julianMonth += 13;
        }
        double julian = (java.lang.Math.floor(365.25 * julianYear)
                + java.lang.Math.floor(30.6001 * julianMonth) + day + 1720995.0);
        if (day + 31 * (month + 12 * year) >= JGREG) {
            // change over to Gregorian calendar
            int ja = (int) (0.01 * julianYear);
            julian += 2 - ja + (0.25 * ja);
        }
        return julian + hour + minute + second;
    }
}
