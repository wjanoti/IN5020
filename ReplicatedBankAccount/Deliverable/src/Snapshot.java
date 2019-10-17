import java.io.Serializable;
import java.util.HashMap;

/**
 * Class holding information about the current Snapshot of the replica. Can decide if a transaction is valid or not
 */
public class Snapshot implements Serializable {

    private HashMap<String, Integer> lastExecutedTransaction = new HashMap<>();

    public boolean CanApplyTransaction(Transaction t) {
        String[] transactionInfo = t.unique_id.split(" ");

        String clientName = transactionInfo[0];
        int sequencingNumber = Integer.parseInt(transactionInfo[1]);

        // We either see this client for the first time or the transaction is the one after the last known
        // note : this is needed for newly joined clients which might receive transaction messages before the state is updated
        return lastExecutedTransaction.getOrDefault(clientName, -1) == sequencingNumber - 1;
    }

    /**
     * Registers a transaction as done
     * @param t
     */
    public void RegisterTransaction(Transaction t) {
        String[] transactionInfo = t.unique_id.split(" ");

        String clientName = transactionInfo[0];
        int sequencingNumber = Integer.parseInt(transactionInfo[1]);

        lastExecutedTransaction.put(clientName, sequencingNumber);
    }

    public void RemoveMember(String member) {
        lastExecutedTransaction.remove(member);
    }
}
