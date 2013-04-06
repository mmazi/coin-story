package si.mazi.coinstory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bitcoin24.Bitcoin24Exchange;
import com.xeiam.xchange.bitcoincentral.BitcoinCentralExchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btce.BTCEExchange;
import com.xeiam.xchange.mtgox.v1.MtGoxExchange;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.io.IOException;
import java.util.*;

/**
 * @author Matija Mazi <br/>
 * @created 3/30/13 6:19 PM
 */
@Singleton
public class Exchanges {
    private static final Logger log = LoggerFactory.getLogger(Exchanges.class);

    public static final String USD = "USD";
    public static final String EUR = "EUR";

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
        currencies.put(Bitcoin24Exchange.class, EUR);
        currencies.put(BitcoinCentralExchange.class, EUR);

        List<Class<? extends Exchange>> exchanges = new ArrayList<Class<? extends Exchange>>(currencies.keySet());
        for (Class<? extends Exchange> exchange : exchanges) {
            Exchange exc = null;
            if (MtGoxExchange.class.isAssignableFrom(exchange)) {
                // https (default) doesn't work for MtGox since we don't have the issuing CA in our trust store. Using http.
                try {
                    ExchangeSpecification exSpec = exchange.newInstance().getDefaultExchangeSpecification();
                    exSpec.setUri(exSpec.getUri().replaceAll("^https://", "http://"));
                    exc = ExchangeFactory.INSTANCE.createExchange(exSpec);
                } catch (ReflectiveOperationException e) {
                    log.error("Error creating exchange with no-https uri; using default: {}", exchange);
                }
            }
            if (exc == null) {
                exc = ExchangeFactory.INSTANCE.createExchange(exchange.getCanonicalName());
            }
            services.put(exchange, exc.getPollingMarketDataService());
        }
    }

    @Schedule(hour = "*", minute = "*/15", persistent = false, info = "Exchange data reader")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void readAll() {
        // todo: do this in prarallel
        Date now = getThisMinute();
        log.info("======= Exchanges.readAll for {}", now);
        for (Class<? extends Exchange> service : services.keySet()) {
            String serviceName = service.getSimpleName();
            for (String currency : currencies.get(service)) {
                log.info("Getting from {} for {}", serviceName, currency);
                try {
                    downloader.readData(services.get(service), currency, service.getSimpleName(), now);
                } catch (IOException e) {
                    log.error("Error reading data for {} from {}: {}", new Object[]{currency, serviceName, Utils.joinToString(e)});
                }
            }
        }
        log.debug("--- Done. ");
    }

    static Date getThisMinute() {
        return new DateTime(System.currentTimeMillis()).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
    }
}
