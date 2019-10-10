import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

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
    private Set<String> members;

    public Client(String[] args) {
        clientId = UUID.randomUUID();
        balance = 0;
        executedList = new ArrayList<>();
        outstandingCollection = new ArrayList<>();
        orderCount = 0;
        outstandingCounter = 0;
        members = new HashSet<>();
        serverAddress = args[0];
        accountName = args[1];
        numberOfReplicas = Integer.parseInt(args[2]);
        if (args.length == 4) {
            inputFilePath = args[3];
        }
        connect();
        System.out.println("Client running...");
        while (state != State.TERMINATING) {
            process();
        }
    }

    private void setState(State newState) {
        this.state = newState;
    }

    private void waitForMembers() throws InterruptedException {
        setState(State.WAITING);
        System.out.print("Waiting for members to connect...");
        while (members.size() != numberOfReplicas) {
            Thread.sleep(1000);
        }
        System.out.println("OK");
        setState(State.RUNNING);
    }

    private void process() {
        // TODO: process user commands or input file.
        // read commands from stdin
        setState(State.RUNNING);
        if (inputFilePath == null) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine().trim();
            switch (command) {
                case "getQuickBalance":
                    break;
                case "getSyncedBalance":
                    break;
                case "deposit":
                    break;
                case "addInterest":
                    break;
                case "getHistory":
                    break;
                case "checkTxStatus":
                    break;
                case "cleanHistory":
                    break;
                case "memberInfo":
                    memberInfo();
                    break;
                case "sleep":
                    break;
                case "exit":
                    exit();
                default:
                    System.out.println("Invalid command.");
            }
        } else {
            // TODO: read commands from file
        }
    }

    /**
     * Connects to the Spread server and joins the group.
     */
    private void connect() {
        System.out.println("Connecting...");
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

            waitForMembers();
        } catch (SpreadException | UnknownHostException | InterruptedException connectionException) {
            System.out.println("Connection error: " + connectionException.getMessage());
            connectionException.printStackTrace();
        }
    }

    /**
     * Get the local balance.
     * @return double
     */
    private double getQuickBalance() {
        // TODO: call this on the "getQuickBalance" command.
        return this.balance;
    }

    /**
     * Disconnects from the Spread server and leaves the group.
     */
    private void exit() {
        // TODO: call this on the "exit" command.
        setState(State.TERMINATING);
        try {
            // if we don't remove the listener it never disconnects.
            connection.remove(this);
            connection.disconnect();
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected successfully.");
        System.exit(0);
    }

    /**
     * Cleans the list of recent transactions
     */
    private void cleanHistory() {
        // TODO: call this on the "cleanHistory" command.
        executedList.clear();
    }

    /**
     * Prints the names of the members of the group.
     */
    private void memberInfo() {
        // TODO: call this on the "memberInfo" command.
        System.out.println("-> Current members:");
        Collections.singletonList(members).forEach(System.out::println);
    }

    /**
     * Pauses the client for "duration" milliseconds
     * @param duration milliseconds
     */
    public void sleep(int duration) {
        // TODO: call this on the "sleep" command.
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            // add member to set
            Arrays.asList(membershipInfo.getMembers()).forEach(member -> this.members.add(member.toString()));
        } else if (membershipInfo.isCausedByDisconnect() || membershipInfo.isCausedByLeave()) {
            // remove member from set
            this.members.remove(membershipInfo.getLeft().toString());
        }
    }
}
