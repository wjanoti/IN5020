import spread.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

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
    private int serverPort = 4803;
    private State state;
    private String clientId;
    private double balance;
    private List<Transaction> executedList;
    private List<Transaction> outstandingCollection;
    private List<Transaction> ongoingTransactions = new ArrayList<>();
    private int orderCount;
    private int outstandingCounter;
    private SpreadGroup group;
    private Set<String> members;
    private Snapshot snapshot;
    private static final short OUTSTANDING_TRANSACTIONS = 43; // used to identify the type of message
    private static final short STATE_UPDATE = 44; // used to identify the type of message
    private static final int PROPAGATION_INTERVAL = 10000; // 10 seconds

    /**
     * Lock to control access to the paths that can actually modify the state of the replica
     */
    private Lock stateLock = new ReentrantLock();

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
                synchronized (outstandingCollection) {
                    message.digest((Serializable) outstandingCollection);
                    ongoingTransactions = new ArrayList<>(outstandingCollection);
                    connection.multicast(message);
                }
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Indicates if the client is initialized properly (if the balance and orderCount are set correctly
     * Especially useful in the case of late-joiners.
     */
    private boolean isInitialized = false;

    private List<Transaction> refusedTransactions = new ArrayList<>();

    public Client(String[] args) {
        balance = 0;
        this.snapshot = new Snapshot();
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
    }

    /**
     * Connects to the Spread server and joins the group.
     */
    public void connect() {
        System.out.println("Connecting...");
        try {
            this.state = State.CONNECTING;

            // connect to server
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(serverAddress), serverPort, UUID.randomUUID().toString(), false, true);

            // subscribe to messages
            connection.add(this);

            // join group
            group = new SpreadGroup();
            group.join(connection, accountName);


            // this is the only way we can get to know our own ID
            clientId = connection.getPrivateGroup().toString();

            waitForMembers();
        } catch (SpreadException | UnknownHostException | InterruptedException connectionException) {
            System.out.println("Connection error: " + connectionException.getMessage());
            connectionException.printStackTrace();
        }
    }

    /**
     * Parses a string containing a client command and possible arguments.
     * @param line
     */
    private void parseCommand(String line) {
        String[] commandLine = line.split("\\s+");
        switch (commandLine[0]) {
            case "getQuickBalance":
                System.out.println("Local balance: " + String.format("%.2f", getQuickBalance()));
                break;
            case "getSyncedBalance":
                // wait until there are no more outstanding transactions from this client.
                while (!outstandingCollection.isEmpty() || !ongoingTransactions.isEmpty()) {
                    System.out.println("Waiting for balance sync...");
                    sleep(1000);
                }

                System.out.println("Synced balance: " + String.format("%.2f", this.getQuickBalance()));
                break;
            case "deposit":
            case "addInterest":
                if (commandLine.length == 2) {
                    Transaction transaction = new Transaction(String.join(" ", commandLine[0], commandLine[1]), generateTransactionId());
                    addTransaction(transaction);
                } else {
                    System.out.println("Usage: deposit <amount> | addInterest <interestRate>");
                }
                break;
            case "getHistory":
                System.out.println("-> Executed transactions:");
                AtomicInteger transactionStart = new AtomicInteger(orderCount - executedList.size() + 1);
                executedList.forEach(executedTransaction ->
                        System.out.println(transactionStart.getAndIncrement() + "." + executedTransaction.getCommand()));
                System.out.println("\n-> Outstanding transactions:");
                outstandingCollection.forEach(outstandingTransaction -> System.out.println(outstandingTransaction.getCommand()));
                break;
            case "checkTxStatus":
                if (commandLine.length == 3) {
                    String transactionId = String.join(" ", commandLine[1], commandLine[2]);
                    if (outstandingCollection.stream().anyMatch(transaction -> transaction.getUniqueId().equals(transactionId))) {
                        System.out.println("Status: Outstanding");
                    } else if (executedList.stream().anyMatch(transaction -> transaction.getUniqueId().equals(transactionId))) {
                        System.out.println("Status: Executed");
                    } else {
                        System.out.println("Status: Unknown");
                    }
                } else {
                    System.out.println("Usage: checkTxStatus <clientId> <outstandingCounter>");
                }
                break;
            case "cleanHistory":
                cleanHistory();
                break;
            case "memberInfo":
                memberInfo();
                break;
            case "sleep":
                if (commandLine.length == 2) {
                    int arg = Integer.parseInt(commandLine[1]);
                    sleep(arg);
                }
                break;
            case "exit":
                exit();
                break;
            default:
                System.out.println("Invalid command.");
        }
    }

    /**
     * Main client logic.
     */
    public void run() throws InterruptedException {
        // setup broadcast of outstanding transactions every 10 seconds.
        Timer timer = new Timer("OutstandingTransactionsTimer");
        timer.scheduleAtFixedRate(outstandingTransactionsTask, PROPAGATION_INTERVAL, PROPAGATION_INTERVAL);

        System.out.println("Client ready");
        setState(State.RUNNING);

        while (state != State.TERMINATING) {
            if (state == State.WAITING) {
                waitForMembers();
            }
            // process inline commands.
            if (inputFilePath == null) {
                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine().trim();
                parseCommand(line);
            } else {
                // process file commands.
                try (Stream<String> lines = Files.lines(Paths.get(inputFilePath))) {
                    lines.forEach(this::parseCommand);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputFilePath = null;
                //exit();
            }
        }
    }

    /**
     * Wait until the required number of replicas connect to start up.
     * @throws InterruptedException
     */
    private void waitForMembers() throws InterruptedException {
        setState(State.WAITING);
        System.out.print("Waiting for members to connect...");
        while (members.size() < numberOfReplicas || !this.isInitialized) {
            Thread.sleep(1000);
        }
        System.out.println("OK");
        setState(State.RUNNING);
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
     * Adds a new transaction the the outstanding transactions list
     * @param transaction transaction to be added.
     */
    private void addTransaction(Transaction transaction) {
        synchronized (outstandingCollection) {
            outstandingCollection.add(transaction);
            outstandingCounter++;
        }
    }

    /**
     * Generates the id used when creating a new transaction.
     * @return transaction id.
     */
    private String generateTransactionId() {
        return clientId + " " + outstandingCounter;
    }

    /**
     * Get the local balance.
     * @return double
     */
    private double getQuickBalance() {
        return this.balance;
    }

    /**
     * Disconnects from the Spread server and leaves the group.
     */
    private void exit() {
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
        executedList.clear();
    }

    /**
     * Prints the names of the members of the group.
     */
    private void memberInfo() {
        System.out.println("-> Current members:");
        members.forEach(System.out::println);
    }

    /**
     * Pauses the client for "duration" milliseconds
     * @param duration milliseconds
     */
    public void sleep(int duration) {
        System.out.println("-> Sleeping for " + duration + " ms.");
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void commitTransactions(List<Transaction> outstandingTransactions) {
        outstandingTransactions.forEach(
                transaction -> {
                    // if we can apply the transaction
                    applyTransaction(transaction);
                }
        );
    }

    private void applyTransaction(Transaction transaction) {
        System.out.println("Received transaction " + transaction);
        String[] args = transaction.command.split(" ");
        if (!this.isInitialized || !this.snapshot.CanApplyTransaction(transaction))
        {
            System.out.println("Can't apply transaction " + transaction);
            this.refusedTransactions.add(transaction);
            return;
        }

        switch (args[0])
        {
            case "deposit":
                double value = Double.parseDouble(args[1]);
                this.deposit(value);
                break;
            case "addInterest":
                double interest = Double.parseDouble(args[1]);
                this.addInterest(interest);
                break;
        }
        this.markTransactionAsDone(transaction);
        orderCount++;
    }

    private void markTransactionAsDone(Transaction t) {
        synchronized (this.outstandingCollection) {
            this.outstandingCollection.removeIf(item -> item.unique_id.equals(t.unique_id));
            this.ongoingTransactions.removeIf(item -> item.unique_id.equals(t.unique_id));
        }
        this.executedList.add(t);
        this.snapshot.RegisterTransaction(t);
    }

    /**
     * This gets called when a regular message is received by the clients. Meaning messages that contain transactions
     * to be applied, or state updates to new members.
     * @param spreadMessage
     */
    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        try {
            this.stateLock.lock();
            if (spreadMessage.getType() == OUTSTANDING_TRANSACTIONS) {
                List<Transaction> transactions = (List<Transaction>) spreadMessage.getDigest().get(0);
                commitTransactions(transactions);
            } else if (spreadMessage.getType() == STATE_UPDATE) {
                StateUpdate stateUpdate = (StateUpdate) spreadMessage.getDigest().get(0);
                System.out.println("Updating my state with:" + stateUpdate);
                this.updateState(stateUpdate);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.stateLock.unlock();
        }
    }

    private void updateState(StateUpdate stateUpdate) {
        try {
            // since we are modifying the state we need to lock
            this.stateLock.lock();
            if (!this.isInitialized) {
                this.isInitialized = true;
                this.balance = stateUpdate.getBalance();
                this.orderCount = stateUpdate.getOrderCount();
                this.snapshot = stateUpdate.getSnapshot();
                // apply all transactions that we might have skipped before we received the snapshot
                this.refusedTransactions.forEach(transaction -> {
                    if (this.snapshot.CanApplyTransaction(transaction)) {
                        this.applyTransaction(transaction);
                    }
                });
            }
        }
        finally {
            this.stateLock.unlock();
        }
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
        if (membershipInfo.isCausedByJoin()) {
//            System.out.println(membershipInfo.getJoined().toString());
            Arrays.asList(membershipInfo.getMembers()).forEach(member -> members.add(member.toString()));
            // only send StateUpdate if this replica actually knows something
            // todo revisit this
            if (membershipInfo.getMembers().length > this.numberOfReplicas) {
                sendStateUpdateMessage();
            }
            else {
                // if we are below or with the exact number of required replicas we are automatically initialized
                this.isInitialized = true;
            }
        } else if (membershipInfo.isCausedByDisconnect() || membershipInfo.isCausedByLeave()) {
            // set the state to waiting in case the number of replicas went below the required threshold
            // this will be reset to RUNNING immediately in the run() method
            setState(State.WAITING);

            // remove member from set
            this.members.remove(membershipInfo.getLeft().toString());
            this.snapshot.RemoveMember(membershipInfo.getLeft().toString());
        }
    }

    /**
     * Sends a state update message containing the current snapshot
     */
    private void sendStateUpdateMessage(){
        stateLock.lock();
        try {
            SpreadMessage stateUpdateMessage = new SpreadMessage();
            stateUpdateMessage.setType(STATE_UPDATE);
            stateUpdateMessage.setReliable();
            stateUpdateMessage.setSafe();
            stateUpdateMessage.addGroup(this.group);
            // balance, orderCount and the snapshot cannot be updated in this time because of the stateLock
            StateUpdate update = new StateUpdate(this.balance, this.orderCount, snapshot);
            stateUpdateMessage.digest(update);
            stateUpdateMessage.digest(update);
            connection.multicast(stateUpdateMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            stateLock.unlock();
        }
    }
    
    /**
     * Updates the client state.
     * @param newState new state
     */
    private synchronized void setState(State newState) {
        this.state = newState;
    }

}
