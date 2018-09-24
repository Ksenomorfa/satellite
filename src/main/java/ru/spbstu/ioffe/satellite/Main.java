package ru.spbstu.ioffe.satellite;

import org.orekit.errors.OrekitException;

public class Main {
    public static void main(String[] args) throws OrekitException {
        Constants.configureProperties();
        CommandLine cmd = new CommandLine();
        cmd.start();
    }
}
