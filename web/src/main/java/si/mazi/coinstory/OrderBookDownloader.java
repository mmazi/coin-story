package si.mazi.coinstory;

import com.google.common.collect.Iterables;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
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
    public void readData(PollingMarketDataService exchange, String currency, String service) throws IOException {
        Date now = new DateTime().withSecondOfMinute(0).withMillis(0).toDate();
        OrderBook orderBook;
        log.info("Connecting to {} for {}...", service, currency);
        try {
            orderBook = exchange.getFullOrderBook("BTC", currency);
        } catch (Exception e) {
            throw new IOException("Error connecting to exchange", e);
        }
        int i = 0;
        log.info("Got {} bids and {} asks.", orderBook.getBids().size(), orderBook.getAsks().size());
        for (LimitOrder limitOrder : Iterables.concat(orderBook.getAsks(), orderBook.getBids())) {
            em.persist(new Ord(limitOrder.getType(), limitOrder.getTradableAmount().doubleValue(), limitOrder.getLimitPrice().getAmount().doubleValue(), now, service, currency));
            if (++i % 100 == 0) {
                em.flush();
            }
        }
    }

}
