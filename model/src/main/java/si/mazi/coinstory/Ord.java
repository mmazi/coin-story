package si.mazi.coinstory;

import com.xeiam.xchange.dto.Order;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author Matija Mazi <br/>
 */
@Entity
public class Ord {
    @Id @GeneratedValue
    private Long id;

    private Order.OrderType orderType;

    private double amount;

    private double price;

    private Date time;

    protected Ord() { }

    public Ord(Order.OrderType orderType, double amount, double price, Date time) {
        this.orderType = orderType;
        this.amount = amount;
        this.price = price;
        this.time = time;
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

    @Override
    public String toString() {
        return String.format("Ord[id=%d, orderType=%s, amount=%s, price=%s, time=%s]", id, orderType, amount, price, time);
    }
}
