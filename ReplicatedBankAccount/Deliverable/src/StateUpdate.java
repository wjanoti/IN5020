import java.io.Serializable;

/**
 * Class containing the information an existing replica sends to a newly joined replica
 * It is simply a snapshot at some point in time
 */
public class StateUpdate implements Serializable {
    private final double balance;
    private final int orderCount;
    private Snapshot snapshot;

    public StateUpdate(double balance, int orderCount, Snapshot snapshot) {

        this.balance = balance;
        this.orderCount = orderCount;
        this.snapshot = snapshot;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public double getBalance() {
        return balance;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }
}
