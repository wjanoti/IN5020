public class AccountReplica {

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: <Spread server address> <Account name> <Number of replicas> [Input file name]");
            System.exit(1);
        }
        Client client = new Client(args);
        client.connect();
        client.run();
    }

}
