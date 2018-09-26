package ru.spbstu.ioffe.satellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public class CommandLine {
    public TLEReader reader;

    public void start() throws OrekitException {
        while (true) {
            List<TLE> tles = tleChoose();
            if (!tles.isEmpty()) {
                tles.forEach(System.err::println);
                CoordinateCalculator coordinateCalculator = new CoordinateCalculator();
                List<PeriodOfPresence> periods = coordinateCalculator.calculateCoordinates(tles, reader);
                System.out.println("| Date From, UTC          | Date To, UTC            | Duration, sec |");
                System.out.println("| ----------------------- | ----------------------- | ------------- |");
                for (PeriodOfPresence period : periods) {
                    System.out.println("| " + period.getPresenceFrom() + " | " + period.getPresenceTo()
                            + " | " + period.getPeriodOfPresence() + "        |");
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

    private List<TLE> tleChoose() throws OrekitException {
        System.out.println("Enter mode: 1) TLE file or 2) Downloading from server or 3) Quit: ");

        Scanner sc = new Scanner(System.in);
        boolean choiceIs = false;

        while (!choiceIs) {
            String choice = sc.next();
            switch (choice) {
                case "1":
                    System.out.println("Enter file path: ");
                    String filePath = sc.next();
                    File filepathFile = new File(filePath);
                    if (filepathFile.exists()) {
                        reader = new TLEFileReader(filepathFile);
                        choiceIs = true;
                    } else {
                        System.out.println("Wrong file, try again.");
                    }
                    break;
                case "2":
                    reader = new TLEURLReader();
                    System.out.println("Default satellite : " + Constants.satelliteId + " with mass: " +
                            Constants.satelliteMass + " kg. Do you want to change it? (Y/N)");
                    if (sc.next().equalsIgnoreCase("Y")) {
                        System.out.println("Enter NORAD satellite id: ");
                        Constants.satelliteId = sc.next();
                        System.out.println("Enter satellite mass in kg: ");
                        Constants.satelliteMass = Double.parseDouble(sc.next());
                    }
                    choiceIs = true;
                    break;
                case "3":
                    System.out.println("Goodbye! ");
                    System.exit(0);
                default:
                    System.out.println("Wrong choice. Choose 1, or 2, or 3 and press <Enter>.");
                    break;
            }
        }
        System.out.println("Enter the start date to propagate in format YYYY-MM-DD: ");
        String dateFrom = sc.next();
        System.out.println("Enter period to propagate in days: ");
        String period = sc.next();

        reader.init(dateFrom, period);
        System.out.println(reader.getPeriod());
        System.out.println("TLE content:");
        return reader.readTLE();
    }
}
