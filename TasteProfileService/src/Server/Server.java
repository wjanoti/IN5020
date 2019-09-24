package Server;

import TasteProfile.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server {

    // Required arguments: -ORBInitialPort <portNumber> <dataDirectory> [useCaching]
    public static void main(String[] args) {
        try {
            // Create and initialize the CORBA ORB
            ORB orb = ORB.init(args, null);

            // Get reference to the root POA and activate the POA manager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Get arguments
            String dataDirectory = args[2];

            boolean useCaching = false;
            if (args.length == 4) {
               useCaching = Boolean.parseBoolean(args[3]);
            }

            // Get object reference from the servant
            Servant servant = new Servant(dataDirectory, useCaching);
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);
            Profiler pref = ProfilerHelper.narrow(ref);

            // Get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Binding the object reference in naming service
            String name = "Profiler";
            NameComponent[] path = ncRef.to_name(name);
            ncRef.rebind(path, pref);

            // System.out.println("getTimesPlayed (no cache): " + servant.getTimesPlayed("SOUDSFN12A8C144B74"));
            // System.out.println("getTimesPlayedByUser (no cache): " + servant.getTimesPlayedByUser("b64cdd1a0bd907e5e00b39e345194768e330d652", "SONKFWL12A6D4F93FE"));
            // System.out.println("getTopThreeUsersBySong (no cache): " + servant.getTopThreeUsersBySong( "SONKFWL12A6D4F93FE"));
            // System.out.println("getTopThreeSongsByUser (no cache): " + servant.getTopThreeSongsByUser( "b64cdd1a0bd907e5e00b39e345194768e330d652"));
            // System.out.println("getUserProfile (no cache): " + servant.getUserProfile( "b64cdd1a0bd907e5e00b39e345194768e330d652"));
            // Wait for remote invocations
            System.out.println("Taste Profile Server running...");
            orb.run();
        } catch (Exception e) {
            System.err.println("Server Error: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
