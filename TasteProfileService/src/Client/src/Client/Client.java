package Client;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Main class for the client application in the TasteProfile service
 */
public class Client {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        QueryExecutor executor = new QueryExecutor();

        Path filePath = Paths.get("/home/alin/projects/IN5020/TasteProfileService/src/Client/data/input.txt");
        try {
            Files.lines(filePath)
                    .map(Client::ParseQueryLine)
                    .forEach(executor::executeQuery);
        } catch (IOException e) {
            e.printStackTrace();
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
