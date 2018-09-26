import org.junit.Before;
import org.junit.Test;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import ru.spbstu.ioffe.satellite.*;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TLEReaderTest {
    @Before
    public void init() {
        Constants.configureProperties();
    }

    @Test
    public void testReadTleURL() throws OrekitException {
        TLEReader urlReader = new TLEURLReader();
        urlReader.init("2009-09-10", "7");
        List<TLE> tles = urlReader.readTLE();
        tles.forEach(System.out::println);
    }

    @Test
    public void testReadTleFile() throws OrekitException {
        ClassLoader classLoader = getClass().getClassLoader();
        TLEReader fileReader = new TLEFileReader(new File(classLoader.getResource("some.tle").getFile()));

        fileReader.init("2009-09-30", "2");
        List<TLE> tles = fileReader.readTLE();
        tles.forEach(System.out::println);

        CoordinateCalculator cc = new CoordinateCalculator();
        List<PeriodOfPresence> actualPeriods = cc.calculateCoordinates(tles, fileReader);
        actualPeriods.forEach(System.out::println);
    }

    @Test
    public void testSPG4URL() throws OrekitException {
        LocalDate dayToShow = LocalDate.parse("2018-09-26", Utils.dateFormatter);
        String dateStartString = dayToShow.format(Utils.dateFormatter);

        TLEReader urlReader = new TLEURLReader();
        urlReader.init(dateStartString, "2");
        List<TLE> tles = urlReader.readTLE();
        tles.forEach(System.out::println);

        CoordinateCalculator cc = new CoordinateCalculator();
        List<PeriodOfPresence> periods = cc.calculateCoordinates(tles, urlReader);
        periods.forEach(System.out::println);
    }
}
