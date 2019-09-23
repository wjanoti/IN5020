package Client;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Logger {

    Map<String, BufferedWriter> writers = new HashMap<>();

    public Logger(File resultFileForFirstTwoMethods, File resultFileForThirdMethod, File resultFileForFourthMethod) throws IOException {
        BufferedWriter writerForFirstTwoMethods = new BufferedWriter(new FileWriter(resultFileForFirstTwoMethods));
        BufferedWriter writerForThirdMethod = new BufferedWriter(new FileWriter(resultFileForThirdMethod));
        BufferedWriter writerForFourthMethod = new BufferedWriter(new FileWriter(resultFileForFourthMethod));

        writers.put("getTimesPlayedByUser", writerForFirstTwoMethods);
        writers.put("getTimesPlayed", writerForFirstTwoMethods);
        writers.put("getTopThreeUsersBySong", writerForThirdMethod);
        writers.put("getTopThreeSongsByUser", writerForFourthMethod);
    }
    /***
     * Logs a line to the file corresponding to the invoking method
     * @param invokingMethod the name of the method invoked in the client
     * @param line log line to write to the corresponding file
     */
    void LogLine(String invokingMethod, String line) {
        BufferedWriter writer = writers.get(invokingMethod);
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Failed to write the logline");
            e.printStackTrace();
        }
    }
}
