package ru.spbstu.ioffe.satellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TLEFileReader implements TLEReader {
    List<TLE> tleList = new ArrayList<>();
    private LocalDate start;
    private long period;
    File file;

    public TLEFileReader(File file) {
        this.file = file;
    }

    public void init(String start, String period) {
        this.start = LocalDate.parse(start, Utils.dateFormatter);
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
        tleList.forEach(System.out::println);

        return tleList;
    }

    public LocalDate getStart() {
        return start;
    }

    public long getPeriod() {
        return period;
    }

}

