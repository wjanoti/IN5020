import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Client implements AdvancedMessageListener {

    private SpreadConnection connection;
    private String serverAddress;
    private String accountName;
    private int numberOfReplicas;
    private String inputFilePath;
    /**
     * Random port used by Spread server. The assignment does not mention that it must be passed as param,
     * so it's hardcoded here. Change it as needed.
     */
    int serverPort = 4803;
    State state;
    UUID clientId;
    private double balance;
    private List<Transaction> executedList;
    private List<Transaction> outstandingCollection;
    private int orderCount;
    private int outstandingCounter;
    private SpreadGroup group;

    public Client(String[] args) {
        clientId = UUID.randomUUID();
        balance = 0;
        executedList = new ArrayList<>();
        outstandingCollection = new ArrayList<>();
        orderCount = 0;
        outstandingCounter = 0;
        serverAddress = args[0];
        accountName = args[1];
        numberOfReplicas = Integer.parseInt(args[2]);
        if (args.length == 4) {
            inputFilePath = args[3];
        }
        connect();
        // TODO: wait until all replicas have joined.
        process();
    }

    private void process() {
        // TODO: process user commands or input file.
    }

    /**
     * Connects to the Spread server and joins the group.
     */
    private void connect() {
        try {
            this.state = State.CONNECTING;
            // connect to server
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(serverAddress), serverPort, clientId.toString(), false, true);

            // subscribe to messages
            connection.add(this);

            // join group
            group = new SpreadGroup();
            group.join(connection, accountName);
            System.out.println("Client connected successfully");
        } catch (SpreadException | UnknownHostException connectionException) {
            System.out.println("Connection error: " + connectionException.getMessage());
            connectionException.printStackTrace();
        }
    }

    /**
     * Disconnects from the Spread server and leaves the group.
     */
    private void disconnect() {
        // TODO: call this on the "exit" command.
        try {
            group.leave();
            connection.disconnect();
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected successfully.");
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        // TODO: deal with the message received. 
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        // TODO: update group accordingly. Multicast the state to all replicas.
        MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
        if (membershipInfo.isCausedByJoin()) {
            System.out.println("Someone joined.");
        } else if (membershipInfo.isCausedByDisconnect() || membershipInfo.isCausedByLeave()) {
            System.out.println("Someone left.");
        }
        System.out.println(spreadMessage.getMembershipInfo().getMembers().length + " replicas.");
    }
}
