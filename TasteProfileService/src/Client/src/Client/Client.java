package Client;

import TasteProfile.Profiler;
import TasteProfile.ProfilerHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.File;
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

    // Args: -ORBInitialPort <Port> <inputFile> <useClientCache> <serverUsesCache>
    public static void main(String[] args) throws IOException {

        Profiler profilerRef = InitializeORB(args);
//        Profiler profilerRef = new FakeProfiler();

        boolean clientSideCache = Boolean.parseBoolean(args[3]);
        boolean serverSideCache = Boolean.parseBoolean(args[4]);

        String filenameBase;
        if (!clientSideCache && !serverSideCache) {
            filenameBase = "naive.txt";
        }
        else {
            if (clientSideCache) {
                filenameBase = "clientside_caching_on.txt";
            }
            else {
                filenameBase = "clientside_caching_off.txt";
            }
        }

        File resultFileForFirstTwoMethods = new File(filenameBase);
        File resultFileForThirdMethod = new File("topuser.txt");
        File resultFileForFourthMethod = new File("topsong.txt");

        try {
            resultFileForFirstTwoMethods.createNewFile();
            resultFileForThirdMethod.createNewFile();
            resultFileForFourthMethod.createNewFile();
        }
        catch (IOException e) {
            System.out.println("Failed to create output files");
            e.printStackTrace();
        }

        Logger logger = new Logger(resultFileForFirstTwoMethods, resultFileForThirdMethod, resultFileForFourthMethod);

        QueryExecutor executor = new QueryExecutor(profilerRef, clientSideCache, logger);

        Path filePath = Paths.get(args[2]);
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
