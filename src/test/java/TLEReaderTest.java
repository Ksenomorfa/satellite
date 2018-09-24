import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.Before;
import org.junit.Test;
import org.orekit.attitudes.InertialProvider;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinates;
import ru.spbstu.ioffe.satellite.*;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class TLEReaderTest {
    @Before
    public void init() {
        Constants.configureProperties();
    }

    @Test
    public void testReadTleURL() throws OrekitException {
        // 33504 id - KORONAS_FOTON
        TLEReader urlReader = new TLEURLReader();
        urlReader.init("2009-09-10", "7");
        TLE tle = urlReader.readTLE().get(0);
    }

    @Test
    public void testReadTleFile() throws OrekitException {
        ClassLoader classLoader = getClass().getClassLoader();
        TLEReader fileReader = new TLEFileReader(new File(classLoader.getResource("some.tle").getFile()));

        TLE tle = fileReader.readTLE().get(0);
        System.out.println(tle.getDate());
    }

    @Test
    public void testSPG4() throws OrekitException {
        LocalDate dayToShow = LocalDate.now();
        String dateStartString = dayToShow.format(Utils.dateFormatter);

        TLEReader urlReader = new TLEURLReader();
        urlReader.init(dateStartString, "2");

        List<TLE> tles = urlReader.readTLE();
        CoordinateCalculator cc = new CoordinateCalculator();
        cc.calculateCoordinates(tles, urlReader);
    }
}
