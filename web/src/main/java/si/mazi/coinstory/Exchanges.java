package si.mazi.coinstory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import com.xeiam.xchange.btce.v3.BTCEExchange;
import com.xeiam.xchange.campbx.CampBXExchange;
import com.xeiam.xchange.justcoin.JustcoinExchange;
import com.xeiam.xchange.kraken.KrakenExchange;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Matija Mazi <br/>
 */
@Singleton
@Named
public class Exchanges {
    private static final Logger log = LoggerFactory.getLogger(Exchanges.class);

    public static final String USD = "USD";
    public static final String EUR = "EUR";
    public static final String CNY = "CNY";

    private static final int FINISH_TIMEOUT_SEC = 90;

    private Map<Class<? extends Exchange>, PollingMarketDataService> services = new HashMap<>();
    private Multimap<Class<? extends Exchange>, String> currencies = LinkedListMultimap.create();

    @EJB private OrderBookDownloader downloader;

    public Exchanges() {
        log.debug("Exchanges: init");
        currencies.put(BitstampExchange.class, USD);
        currencies.put(BTCEExchange.class, USD);
        currencies.put(BTCEExchange.class, EUR);
        currencies.put(CampBXExchange.class, USD);
        currencies.put(BTCChinaExchange.class, CNY);
        currencies.put(KrakenExchange.class, EUR);
        currencies.put(KrakenExchange.class, USD);
        currencies.put(BitfinexExchange.class, USD);
        currencies.put(JustcoinExchange.class, EUR);

        List<Class<? extends Exchange>> exchanges = new ArrayList<>(currencies.keySet());
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
