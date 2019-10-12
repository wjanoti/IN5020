import java.util.HashMap;

/**
 * Class responsible for checking if a transaction should be applied give the current state of the client
 */
public class ConsistencyChecker {

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
}
