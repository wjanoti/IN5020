import spread.*;

import java.io.Serializable;
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
    public static final short OUTSTANDING_TRANSACTIONS = 43;
    public static final short STATE_UPDATE = 44;
    // task to broadcast outstanding transactions.
    private TimerTask outstandingTransactionsTask = new TimerTask() {
        public void run() {
            // build message
            SpreadMessage message = new SpreadMessage();
            message.setReliable();
            message.setSafe();
            message.addGroup(group);
            message.setType(OUTSTANDING_TRANSACTIONS);

            // send message
            try {
                message.digest((Serializable) outstandingCollection);
                connection.multicast(message);
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        }
    };

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
        run();
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

    private void run() {
        // setup broadcast of outstanding transactions every 10 seconds.
        Timer timer = new Timer("OutstandingTransactionsTimer");
        timer.scheduleAtFixedRate(outstandingTransactionsTask,10000, 10000);

        System.out.println("Client ready");
        setState(State.RUNNING);

        while (state != State.TERMINATING) {
            // process inline commands.
            if (inputFilePath == null) {
                Scanner scanner = new Scanner(System.in);
                String[] line = scanner.nextLine().trim().split("\\s+");
                String command = line[0];
                switch (command) {
                    case "getQuickBalance":
                        System.out.println("Local balance: " + String.format("%.2f", getQuickBalance()));
                        break;
                    case "getSyncedBalance":
                        break;
                    case "deposit":
                        if (line.length == 2) {
                            double amount = Double.parseDouble(line[1]);
                            // local command
                            deposit(amount);
                            Transaction transaction = new Transaction(line[0] + " " + line[1], generateTransactionId());
                            addTransaction(transaction);
                        }
                        break;
                    case "addInterest":
                        if (line.length == 2) {
                            double percentage = Double.parseDouble(line[1]);
                            // local command
                            addInterest(percentage);
                            Transaction transaction = new Transaction(line[0] + " " + line[1], generateTransactionId());
                            addTransaction(transaction);
                        }
                        break;
                    case "getHistory":
                        Collections.singletonList(outstandingCollection).forEach(System.out::println);
                        break;
                    case "checkTxStatus":
                        break;
                    case "cleanHistory":
                        break;
                    case "memberInfo":
                        memberInfo();
                        break;
                    case "sleep":
                        if (line.length == 2) {
                            int arg = Integer.parseInt(line[1]);
                            sleep(arg);
                        }
                        break;
                    case "exit":
                        exit();
                    default:
                        System.out.println("Invalid command.");
                }
            } else {
                // process file commands
                // TODO: read commands from file
            }
        }
    }

    /**
     * Add interest to account balance.
     * @param percentage amount to increase
     */
    private void addInterest(double percentage) {
        this.balance = this.balance * (1 + (percentage/100));
    }

    /**
     * Updates local balance.
     * @param amount amount to deposit.
     */
    private void deposit(double amount) {
        this.balance += amount;
    }

    /**
     * Adds a new transaction the the outstanding transactions list and updates the order count.
     * @param transaction transaction to be added.
     */
    private void addTransaction(Transaction transaction) {
        outstandingCollection.add(transaction);
        orderCount++;
    }

    /**
     * Generates the id used when creating a new transaction.
     * @return transaction id.
     */
    private String generateTransactionId() {
        return clientId + " " + outstandingCounter;
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
        System.out.println("Sleeping for " + duration + " ms.");
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void commitTransactions(List<Transaction> outstandingTrasactions) {

    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        // ignore messages to myself.
        if (spreadMessage.getSender().equals(this.connection.getPrivateGroup())) {
            return;
        }

        if (spreadMessage.getType() == OUTSTANDING_TRANSACTIONS) {
            try {
                List<Transaction> transactions = spreadMessage.getDigest();

            } catch (SpreadException e) {
                e.printStackTrace();
            }
        } else if (spreadMessage.getType() == STATE_UPDATE) {
        }
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
