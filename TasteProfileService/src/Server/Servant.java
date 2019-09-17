package Server;

import TasteProfile.ProfilerPOA;
import TasteProfile.TopThreeSongs;
import TasteProfile.TopThreeUsers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Implementation of the Profiler interface defined in IDL.
 */
public class Servant extends ProfilerPOA {

    private String dataDirectory;
    private boolean useCaching;

    public Servant(String dataDirectory, boolean useCaching) {
        this.dataDirectory = dataDirectory;
        this.useCaching = useCaching;
        if (this.useCaching) {
            buildCache();
        }
    }

    /**
     * Populates the cache from the files.
     */
    private void buildCache() {
        // TODO
    }

    @Override
    public int getTimesPlayed(String song_id) {
        AtomicInteger timesPlayed = new AtomicInteger();
        Stream<Path> paths = null;

        try {
            paths = Files.walk(Paths.get(this.dataDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(paths)
            .filter(Files::isRegularFile)
            .forEach(path -> {
                try {
                    Files.lines(path)
                        .forEach(line -> {
                            String[] lineArray = line.split("\t");
                            String songId = lineArray[0];
                            int songTimesPlayed = Integer.parseInt(lineArray[2]);
                            if (songId.equals(song_id)) {
                                timesPlayed.addAndGet(songTimesPlayed);
                            }
                        });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        return timesPlayed.get();
    }

    @Override
    public int getTimesPlayedByUser(String user_id, String song_id) {
        final File dataDirectory = new File(this.dataDirectory);
        int timesPlayedByUser = 0;

        for (File dataFile: dataDirectory.listFiles()) {
            String line;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                while ((line = reader.readLine()) != null && timesPlayedByUser == 0) {
                    String[] lineArray = line.split("\t");
                    String songId = lineArray[0];
                    String userId = lineArray[1];
                    if (songId.equals(song_id) && userId.equals(user_id)) {
                        timesPlayedByUser = Integer.parseInt(lineArray[2]);
                        break;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return timesPlayedByUser;
    }

    @Override
    public TopThreeUsers getTopThreeUsersBySong(String song_id) {
        return null;
    }

    @Override
    public TopThreeSongs getTopThreeSongsByUser(String user_id) {
        return null;
    }

}
