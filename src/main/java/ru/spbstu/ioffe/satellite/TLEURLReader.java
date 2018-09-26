package ru.spbstu.ioffe.satellite;

import org.orekit.propagation.analytical.tle.TLE;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

/**
 * Reads TLE list from Space-Track website
 */
public class TLEURLReader implements TLEReader {
    private final String baseURL = "https://www.space-track.org";
    private final String authPath = "/ajaxauth/login";
    private final String logoutPath = "/ajaxauth/logout";
    private String query;
    private HttpsURLConnection connection;

    // Read TLEs
    private List<TLE> tleList = new ArrayList<>();
    // Start date to propagate
    private LocalDate start;
    // Start date to load TLE
    private LocalDate tleDateStart;
    // Period to propogate
    private long period;

    public void init(String start, String period) {
        LocalDate startDate = LocalDate.parse(start, Utils.dateFormatter);
        this.start = startDate;
        this.period = Long.parseLong(period);
        this.tleDateStart = startDate.minusDays(1);
        String end = startDate.plus(Long.parseLong(period), ChronoUnit.DAYS).format(Utils.dateFormatter);
        if (startDate.isAfter(LocalDate.now().minus(2, ChronoUnit.DAYS))) {
            System.out.println("Date is more then today, we will use the latest TLE from server.");
            query = "/basicspacedata/query/class/tle_latest/ORDINAL/1/NORAD_CAT_ID/" +
                    Constants.satelliteId + "/orderby/TLE_LINE1%20ASC/format/tle";
        } else {
            System.out.println("Data will be loaded from: " + this.tleDateStart + " to: " + end);
            query = "/basicspacedata/query/class/tle/EPOCH/" + this.tleDateStart.format(Utils.dateFormatter) + "--" + end + "/NORAD_CAT_ID/" +
                    Constants.satelliteId + "/orderby/TLE_LINE1%20ASC/format/tle";
        }
    }

    public List<TLE> readTLE() {
        openConnection();
        closeConnection();
        return tleList;
    }

    private void closeConnection() {
        try {
            URL url = new URL(baseURL + logoutPath);
            url.openStream();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openConnection() {
        try {
            CookieManager manager = new CookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(manager);

            URL url = new URL(baseURL + authPath);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            String input = "identity=" + Constants.userName + "&password=" + Constants.password;

            OutputStream os = connection.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            connection.getInputStream();
            System.out.println("Login to Server: " + baseURL);

            url = new URL(baseURL + query);
            BufferedReader br = new BufferedReader(new InputStreamReader((url.openStream())));
            List<String> resultList = new ArrayList<>();
            String output;
            while ((output = br.readLine()) != null) {
                resultList.add(output);
            }
            for (int i = 0; i < resultList.size(); i = i + 2) {
                TLE tle = new TLE(resultList.get(i), resultList.get(i + 1));
                tleList.add(tle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LocalDate getStart() {
        return start;
    }

    public long getPeriod() {
        return period;
    }

    @Override
    public LocalDate getTleDateStart() {
        return tleDateStart;
    }
}

