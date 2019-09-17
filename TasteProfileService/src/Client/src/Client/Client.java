package Client;

import TasteProfile.Profiler;
import TasteProfile.ProfilerHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class for the client application in the TasteProfile service
 */
public class Client {

    public static void main(String[] args) {

        Profiler profilerRef = InitializeORB(args);

        // todo initialize filenames with the format {fileName}_{clientSideCacheFlag}_{serverSideCache}
        Logger logger = new Logger();

        QueryExecutor executor = new QueryExecutor(profilerRef, false, logger);

        // todo set this up via args
        Path filePath = Paths.get("/home/alin/projects/IN5020/TasteProfileService/src/Client/data/input.txt");
        try {
            Files.lines(filePath)
                    .map(Client::ParseQueryLine)
                    .forEach(executor::executeQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for initializing the ORB environment
     * @param args cmdline arguments give to the program
     * @return an initialized profiler instance which can be used
     */
    private static Profiler InitializeORB(String[] args) {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            String name = "Profiler";

            Profiler profiler = ProfilerHelper.narrow(ncRef.resolve_str(name));

            return profiler;

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            return null;
        }
    }

    private static Query ParseQueryLine(String s) {
        String[] strings = s.split("\t");
        String name = strings[0];

        List<String> args = new ArrayList<>(Arrays.asList(strings));
        args.remove(0);

        Query q = new Query(name, args);

        System.out.println("Parsed query: " + q);

        return q;
    }
}
