package si.mazi.coinstory;

import com.google.common.collect.Iterables;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;
import org.joda.money.BigMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Matija Mazi <br/>
 * @created 3/30/13 6:13 PM
 */
@Stateful
public class OrderBookDownloader {
    private static final Logger log = LoggerFactory.getLogger(OrderBookDownloader.class);

    @PersistenceContext private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void readData(PollingMarketDataService exchange, String currency, String service, Date time) throws IOException {
        OrderBook orderBook;
        Ticker tck;
        log.info("Connecting to {} for {}...", service, currency);
        try {
            tck = exchange.getTicker("BTC", currency);
            orderBook = exchange.getFullOrderBook("BTC", currency);
        } catch (Exception e) {
            throw new IOException("Error connecting to exchange", e);
        }
        int i = 0;
        log.info("Got ticker, {} bids and {} asks.", orderBook.getBids().size(), orderBook.getAsks().size());
        em.persist(new Tick(tck.getTradableIdentifier(), dbl(tck.getLast()), dbl(tck.getBid()), dbl(tck.getAsk()), dbl(tck.getHigh()), dbl(tck.getLow()), getDouble(tck.getVolume()), tck.getTimestamp(), currency));
        for (LimitOrder limitOrder : Iterables.concat(orderBook.getAsks(), orderBook.getBids())) {
            em.persist(new Ord(limitOrder.getType(), getDouble(limitOrder.getTradableAmount()), dbl(limitOrder.getLimitPrice()), time, service, currency));
            if (i++ % 100 == 0) {
                em.flush();
            }
        }
    }

    private Double getDouble(BigDecimal bigDecimal) {
        return bigDecimal == null ? null : bigDecimal.doubleValue();
    }

    private Double dbl(BigMoney bigMoney) {
        return bigMoney == null ? null : getDouble(bigMoney.getAmount());
    }
}
