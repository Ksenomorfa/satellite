package ru.spbstu.ioffe.satellite;


import org.orekit.data.DataProvidersManager;
import org.orekit.data.ZipJarCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Constants {
    // Mass in kg
    public static double satelliteMass;
    public static double satelliteId;

    /**
     * Radius Earth [m]; WGS-84 (semi-major axis, a) (Equatorial Radius)
     */
    public static final double earthRadius = 6378135;

    /**
     * Earth Flattening; WGS-84
     */
    public static final double flatteningEarth = 1.0 / 298.257223563;

    /**
     * Ellipsoid constants: eccentricity; WGS84
     */
    public static final double eccentricity = 8.1819190842622e-2;

    /** Machine precision for doubles.
     */
    public final static double MACHEPS = 1.1102230246251565E-16;

    // Setted in properties file
    public static double latitude;
    public static double longitude;
    public static String userName;
    public static String password;
    public static String orekitDataProp;

    public static void configureProperties(){
        Properties prop = new Properties();
        try(FileInputStream in = new FileInputStream("satellite.properties")) {
            prop.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        orekitDataProp = prop.getProperty("orekit.data.path");
        userName = prop.getProperty("web.username");
        password = prop.getProperty("web.password");
        latitude = Double.parseDouble(prop.getProperty("observer.latitude"));
        longitude = Double.parseDouble(prop.getProperty("observer.longitude"));
        satelliteMass = Double.parseDouble(prop.getProperty("satellite.default.mass"));
        satelliteId = Double.parseDouble(prop.getProperty("satellite.default.id"));

        DataProvidersManager manager = DataProvidersManager.getInstance();
        manager.addProvider(new ZipJarCrawler(new File(orekitDataProp)));
    }
}
