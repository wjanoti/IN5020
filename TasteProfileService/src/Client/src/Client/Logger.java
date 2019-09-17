package Client;

public class Logger {

    /***
     * Logs a line to the file corresponding to the invoking method
     * @param invokingMethod the name of the method invoked in the client
     * @param line log line to write to the corresponding file
     */
    void LogLine(String invokingMethod, String line) {
        // todo actually involve files in here
        System.out.println(line);
    }
}
