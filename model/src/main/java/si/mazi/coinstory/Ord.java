package si.mazi.coinstory;

import com.xeiam.xchange.dto.Order;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Matija Mazi <br/>
 */
@Entity
public class Ord {
    @Id @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Order.OrderType orderType;

    private double amount;

    private double price;

    private Date time;

    private String service;

    private String currency;

    protected Ord() { }

    public Ord(Order.OrderType type, double amount, double price, Date time, String service, String currency) {
        this.orderType = type;
        this.amount = amount;
        this.price = price;
        this.time = time;
        this.service = service;
        this.currency = currency;
    }

    public Order.OrderType getOrderType() {
        return orderType;
    }

    public double getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public Date getTime() {
        return time;
    }

    public String getService() {
        return service;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return String.format("Ord[id=%d, orderType=%s, amount=%s, price=%s, time=%s]", id, orderType, amount, price, time);
    }
}
