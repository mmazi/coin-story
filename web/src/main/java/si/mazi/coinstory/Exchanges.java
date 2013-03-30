package si.mazi.coinstory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitcoin24.Bitcoin24Exchange;
import com.xeiam.xchange.bitcoincentral.BitcoinCentralExchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btce.BTCEExchange;
import com.xeiam.xchange.mtgox.v1.MtGoxExchange;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        currencies.put(BitstampExchange.class, USD);
        currencies.put(MtGoxExchange.class, USD);
        currencies.put(MtGoxExchange.class, EUR);
        currencies.put(BTCEExchange.class, USD);
        currencies.put(BTCEExchange.class, EUR);
        currencies.put(Bitcoin24Exchange.class, EUR);
        currencies.put(BitcoinCentralExchange.class, USD);
        currencies.put(BitcoinCentralExchange.class, EUR);

        List<Class<? extends Exchange>> exchanges = Arrays.<Class<? extends Exchange>>asList(BitstampExchange.class, MtGoxExchange.class, BTCEExchange.class, Bitcoin24Exchange.class, BitcoinCentralExchange.class);
        for (Class<? extends Exchange> exchange : exchanges) {
            services.put(exchange, ExchangeFactory.INSTANCE.createExchange(exchange.getCanonicalName()).getPollingMarketDataService());
        }
    }

    @Schedule(hour = "*", minute = "*/15", persistent = false, info = "Exchange data reader")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void readAll() {
        log.info("======= Exchanges.readAll");
        for (Class<? extends Exchange> service : services.keySet()) {
            String serviceName = service.getSimpleName();
            for (String currency : currencies.get(service)) {
                log.info("Getting from {} for {}", serviceName, currency);
                try {
                    downloader.readData(services.get(service), currency, service.getSimpleName());
                } catch (IOException e) {
                    log.error("Error reading data for {} from {}: {}", new Object[]{currency, serviceName, e.getCause()});
                }
            }
        }
    }
}