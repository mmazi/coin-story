package si.mazi.coinstory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btce.BTCEExchange;
import com.xeiam.xchange.campbx.CampBXExchange;
import com.xeiam.xchange.mtgox.v1.MtGoxExchange;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.util.*;
import java.util.concurrent.Future;

//import javax.inject.Named;

/**
 * @author Matija Mazi <br/>
 * @created 3/30/13 6:19 PM
 */
@Singleton
//@Named
public class Exchanges {
    private static final Logger log = LoggerFactory.getLogger(Exchanges.class);

    public static final String USD = "USD";
    public static final String EUR = "EUR";

    private static final int FINISH_TIMEOUT_SEC = 90;

    private Map<Class<? extends Exchange>, PollingMarketDataService> services = new HashMap<Class<? extends Exchange>, PollingMarketDataService>();
    private Multimap<Class<? extends Exchange>, String> currencies = LinkedListMultimap.create();

    @EJB private OrderBookDownloader downloader;

    public Exchanges() {
        log.debug("Exchanges: init");
        currencies.put(MtGoxExchange.class, USD);
        currencies.put(MtGoxExchange.class, EUR);
        currencies.put(BitstampExchange.class, USD);
        currencies.put(BTCEExchange.class, USD);
        currencies.put(BTCEExchange.class, EUR);
        currencies.put(CampBXExchange.class, USD);

        List<Class<? extends Exchange>> exchanges = new ArrayList<Class<? extends Exchange>>(currencies.keySet());
        for (Class<? extends Exchange> exchange : exchanges) {
            Exchange exc = ExchangeFactory.INSTANCE.createExchange(exchange.getCanonicalName());
            services.put(exchange, exc.getPollingMarketDataService());
        }
    }

    @Schedule(hour = "*", minute = "0", persistent = false, info = "Exchange data reader")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void readAll() {
        Date now = getThisMinute();
        Map<Class<? extends Exchange>, Future<Boolean>> results = new HashMap<>();
        log.info("======= Exchanges.readAll for {}", now);
        for (Class<? extends Exchange> service : services.keySet()) {
            String serviceName = service.getSimpleName();
            for (String currency : currencies.get(service)) {
                log.info("Getting from {} for {}", serviceName, currency);
                Future<Boolean> result = downloader.readData(services.get(service), currency, service.getSimpleName(), now);
                results.put(service, result);
            }
        }
        boolean allComplete = false;
        while (!allComplete && System.currentTimeMillis() - now.getTime() < FINISH_TIMEOUT_SEC * 1000) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            allComplete = true;
            for (Future<Boolean> result : results.values()) {
                allComplete &= result.isDone() || result.isCancelled();
            }
        }
        log.info(allComplete ? "---- All Done." : "[Timed out waiting for exchanges to finish, but no matter.]");
    }

    static Date getThisMinute() {
        return new DateTime(System.currentTimeMillis()).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
    }
}
