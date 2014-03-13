package si.mazi.coinstory;

import com.google.common.collect.Iterables;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * @author Matija Mazi <br/>
 */
@Stateless
public class OrderBookDownloader {
    private static final Logger log = LoggerFactory.getLogger(OrderBookDownloader.class);

    @PersistenceContext private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Asynchronous
    public Future<Boolean> readData(PollingMarketDataService exchange, String currency, String service, Date time) {
        OrderBook orderBook;
        Ticker tck;
        log.info("Connecting to {} for {}...", service, currency);
        String what = "ticker";
        try {
            final CurrencyPair currencyPair = new CurrencyPair("BTC", currency);
            tck = exchange.getTicker(currencyPair);
            // Wait a while between requests; some exchanges sometimes don't allow frequent requests.
            what = "order book";
            Thread.sleep(1000);
            orderBook = exchange.getOrderBook(currencyPair);
        } catch (IOException e) {
            log.error("Error getting {} from {}: {}", what, service, Utils.joinToString(e));
            return new AsyncResult<>(false);
        } catch (RuntimeException e) {
            log.error("Error connecting to " + service + " for " + what, e);
            return new AsyncResult<>(false);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interrupt", e);
        }
        int i = 0;
        log.info("{} {}: Got ticker, {} bids and {} asks.", service, currency, orderBook.getBids() == null ? "no" : orderBook.getBids().size(), orderBook.getAsks() == null ? "no" : orderBook.getAsks().size());
        em.persist(new Tick(tck.getCurrencyPair().baseSymbol, getDouble(tck.getLast()), getDouble(tck.getBid()), getDouble(tck.getAsk()), getDouble(tck.getHigh()), getDouble(tck.getLow()), getDouble(tck.getVolume()), time, currency, service));
        for (LimitOrder limitOrder : Iterables.concat(orderBook.getAsks(), orderBook.getBids())) {
            em.persist(new Ord(limitOrder.getType(), getDouble(limitOrder.getTradableAmount()), getDouble(limitOrder.getLimitPrice()), time, service, currency));
            if (i++ % 100 == 0) {
                em.flush();
            }
        }
        return new AsyncResult<>(true);
    }

    private Double getDouble(BigDecimal bigDecimal) {
        return bigDecimal == null ? null : bigDecimal.doubleValue();
    }
}
