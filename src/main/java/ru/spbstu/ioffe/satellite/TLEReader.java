package ru.spbstu.ioffe.satellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;

import java.time.LocalDate;
import java.util.List;

public interface TLEReader {
    List<TLE> readTLE() throws OrekitException;
    void init(String start, String period);
    LocalDate getStart();
    LocalDate getTleDateStart();
    long getPeriod();
}
