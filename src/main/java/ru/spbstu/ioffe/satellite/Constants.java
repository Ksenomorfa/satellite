package ru.spbstu.ioffe.satellite;

import org.orekit.data.DataProvidersManager;
import org.orekit.data.ZipJarCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Constants {
    //Radius Earth [m]; WGS-84 (semi-major axis, a) (Equatorial Radius)
    public static final double earthRadius = 6378135;
    //Ellipsoid constants: eccentricity; WGS84
    public static final double eccentricity = 8.1819190842622e-2;

    // Next constants are set in properties file
    // latitude of observer place
    public static double latitude;
    // longitude of observer place
    public static double longitude;
    // Angle at which satellite is "visible" above the horizon
    public static double deltaAngle;
    // username for connection to Space-Track website
    public static String userName;
    // password for connection to Space-Track website
    public static String password;
    // Path to orekit-data.zip (needed to start Orekit library)
    public static String orekitDataProp;
    // Satellite mass in kg
    public static double satelliteMass;
    // Satellite NORAD id
    public static String satelliteId;

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
        satelliteId = prop.getProperty("satellite.default.id");
        deltaAngle = Double.parseDouble(prop.getProperty("observer.delta.angle"));

        DataProvidersManager manager = DataProvidersManager.getInstance();
        manager.addProvider(new ZipJarCrawler(new File(orekitDataProp)));
    }
}
