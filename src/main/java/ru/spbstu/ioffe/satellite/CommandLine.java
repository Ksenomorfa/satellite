package ru.spbstu.ioffe.satellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class CommandLine {
    public TLEReader reader;

    public void start() throws OrekitException {
        while(true) {
            List<TLE> tles = tleChoose();
            if (!tles.isEmpty()) {
                tles.forEach(TLE::toString);
                CoordinateCalculator coordinateCalculator = new CoordinateCalculator();
                List<Periodis> periods = coordinateCalculator.calculateCoordinates(tles, reader);
                System.out.println(periods);
                System.out.println("Date From | Date To | Duration");
                for (Periodis period : periods) {
                    System.out.println(period.getFrom() + " | " + period.getTo()
                            + " | " +  period.getTo().durationFrom(period.getFrom()));
                }
            } else {
                System.out.println("Sorry, tles for this satellite are absent.");
            }

            System.out.println("Do you want to continue with next TLE (Y/N)?");
            Scanner sc = new Scanner(System.in);
            String choice = sc.next();
            if (!choice.equalsIgnoreCase("Y")) {
                System.out.println("Goodbye!");
                System.exit(0);
            }
        }
    }

    public List<TLE> tleChoose() throws OrekitException {
        System.out.println("Enter mode: 1) TLE file or 2) Downloading from server or 3) Quit: ");

        Scanner sc = new Scanner(System.in);
        boolean choiceIs = false;

        while (!choiceIs) {
            String choice = sc.next();
            if (choice.equals("1")) {
                System.out.println("Enter file path: ");
                String filePath = sc.next();
                File filepathFile = new File(filePath);
                if (filepathFile.exists()) {
                    reader = new TLEFileReader(filepathFile);
                    choiceIs = true;
                } else {
                    System.out.println("Wrong file, try again.");
                }
            } else if (choice.equals("2")) {
                reader = new TLEURLReader();
                System.out.println("Default satellite : " + Constants.satelliteId + ". Do you want to change it? (Y/N)");
                if (sc.next().equalsIgnoreCase("Y")) {
                    System.out.println("Enter NORAD satellite id: ");
                    Constants.satelliteId = sc.next();
                }
                choiceIs = true;
            } else if (choice.equals("3")) {
                System.out.println("Goodbye! ");
                System.exit(0);
            } else {
                System.out.println("Wrong choice. Choose 1, or 2, or 3 and press <Enter>.");
            }
        }
        System.out.println("Enter the start date to propagate in format YYYY-MM-DD: ");
        String dateFrom = sc.next();
        System.out.println("Enter period to propagate in days: ");
        String period = sc.next();

        reader.init(dateFrom, period);
        System.out.println(reader.getPeriod());
        return reader.readTLE();
    }
}
