import spread.SpreadConnection;
import spread.SpreadException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class Client {

    SpreadConnection connection;
    /**
     * Random used by Spread server. The assignment does not mention that it must be passed as param,
     * so it's hardcoded here. Change it as needed.
     */
    int serverPort = 4803;
    UUID clientId = UUID.randomUUID();

    public Client(String[] args) {
        connect(args[1], args[2], Integer.parseInt(args[3]));
    }

    public void connect(String spreadServerAddress, String accountName, int numberOfReplicas) {
        try {
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(spreadServerAddress), serverPort, clientId.toString(), false, false);
            System.out.println("Client connected successfully");
        } catch (SpreadException | UnknownHostException connectionException) {
            System.out.println("Connection error: " + connectionException.getMessage());
            connectionException.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.disconnect();
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected successfully.");
    }
}
