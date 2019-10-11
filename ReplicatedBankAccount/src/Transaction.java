import java.io.Serializable;

class Transaction implements Serializable {

    String command;
    String unique_id;

    public Transaction(String c, String id) {
        command = c;
        unique_id = id;
    }

    @Override
    public String toString() {
        return "Transaction ID: " + unique_id + " Command: " + command;
    }

}