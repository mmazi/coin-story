package si.mazi.coinstory;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author Matija Mazi <br/>
 */
@Entity
public class Tick {
    @Id @GeneratedValue
    private Long id;

    private String tradableIdentifier;
    private Double last;
    private Double bid;
    private Double ask;
    private Double high;
    private Double low;
    private Double volume;
    private Date timestamp;
    private String currency;
    private String service;

    protected Tick() {
    }

    public Tick(String tradableIdentifier, Double last, Double bid, Double ask, Double high, Double low, Double volume, Date timestamp, String currency, String service) {
        this.tradableIdentifier = tradableIdentifier;
        this.last = last;
        this.bid = bid;
        this.ask = ask;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.timestamp = timestamp;
        this.currency = currency;
        this.service = service;
    }
}
