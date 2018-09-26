package ru.spbstu.ioffe.satellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.TimeScalesFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reads TLE list from TLE file
 */
public class TLEFileReader implements TLEReader {
    // Path to TLE file
    private File file;
    // Read TLEs
    private List<TLE> tleList = new ArrayList<>();
    // Start date to propagate
    private LocalDate start;
    // Start date to load TLE
    private LocalDate tleDateStart;
    // Period to propogate
    private long period;

    public TLEFileReader(File file) {
        this.file = file;
    }

    public void init(String start, String period) {
        this.start = LocalDate.parse(start, Utils.dateFormatter);
        this.tleDateStart = this.start.minusDays(1);
        this.period = Long.parseLong(period);
    }

    public List<TLE> readTLE() throws OrekitException {
        List<String> resultList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                resultList.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Wrong file, try again.");
        }
        for (int i = 0; i < resultList.size(); i = i + 2) {
            TLE tle = new TLE(resultList.get(i), resultList.get(i + 1));
            tleList.add(tle);
        }
        LocalDate lastTLEInFile = LocalDate.parse(tleList.get(tleList.size() - 1).getDate().toString(TimeScalesFactory.getUTC()).substring(0,10));

        if (start.isAfter(lastTLEInFile)) {
            System.out.println("We have no such date in TLE file, it will be set to the last in TLE: " + lastTLEInFile);
            tleDateStart = lastTLEInFile;
        }
        LocalDate firstTLEInFile = LocalDate.parse(tleList.get(0).getDate().toString(TimeScalesFactory.getUTC()).substring(0,10));
        if (start.isBefore(firstTLEInFile)) {
            System.out.println("We have no such date in TLE file, it will be set to the first in TLE: " + firstTLEInFile);
            tleDateStart = firstTLEInFile;
        }

        return tleList;
    }

    public LocalDate getStart() {
        return start;
    }

    public long getPeriod() {
        return period;
    }

    public LocalDate getTleDateStart() {
        return tleDateStart;
    }
}

