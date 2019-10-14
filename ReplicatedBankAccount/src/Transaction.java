import java.io.Serializable;

class Transaction implements Serializable {

    String command;
    String unique_id;
    String client_id;

    public Transaction(String c, String id) {
        command = c;
        unique_id = id;
        client_id = id.split(" ")[0];
    }

    public String getCommand() {
        return command;
    }

    public String getClientId() {
        return client_id;
    }

    public String getUniqueId() {
        return unique_id;
    }

    @Override
    public String toString() {
        return "Transaction ID: " + unique_id + " Command: " + command + " Issuer: " + client_id;
    }

}