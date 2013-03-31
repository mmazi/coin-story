package si.mazi.coinstory;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Matija Mazi <br/>
 * @created 3/30/13 11:57 PM
 */
public class ExchangesTest {
    @Test
    public void testGetThisMinute() throws Exception {
        Assert.assertTrue(new DateTime(Exchanges.getThisMinute()).getYear() > 2000);
    }
}
