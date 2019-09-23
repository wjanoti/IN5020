package Server;

import TasteProfile.ProfilerPOA;
import TasteProfile.TopThreeSongs;
import TasteProfile.TopThreeUsers;
import TasteProfile.UserProfile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Implementation of the Profiler interface defined in IDL.
 */
public class Servant extends ProfilerPOA {

    private String dataDirectory;
    private boolean useCaching;
    private Map<String, SongProfileImpl> songCache = new HashMap<>();
    private Map<String, UserProfileImpl> userCache = new HashMap<>();

    public Servant(String dataDirectory, boolean useCaching) {
        this.dataDirectory = dataDirectory;
        this.useCaching = useCaching;
        if (this.useCaching) {
            buildSongCache();
            buildUserCache();
        }
    }

    /**
     * Populates the song cache
     */
    private void buildSongCache() {
        // TODO
    }

    /**
     * Populates the user cache
     */
    private void buildUserCache() {
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
        final File dataDirectory = new File(this.dataDirectory);
        HashMap<String, Integer> userSongMap = new HashMap<>();
        UserCounterImpl[] userCounter = new UserCounterImpl[3];

        for (File dataFile: dataDirectory.listFiles()) {
            String line;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                while ((line = reader.readLine()) != null) {
                    String[] lineArray = line.split("\t");
                    String songId = lineArray[0];
                    String userId = lineArray[1];
                    int songTimesPlayed = Integer.parseInt(lineArray[2]);
                    if (songId.equals(song_id)) {
                        userSongMap.putIfAbsent(userId, songTimesPlayed);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Integer> sortedUserSongMap = userSongMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        Iterator it = sortedUserSongMap.entrySet().iterator();
        int counter = 0;
        while (it.hasNext() && counter < 3) {
            Map.Entry pair = (Map.Entry)it.next();
            UserCounterImpl userCounterEntry = new UserCounterImpl((String) pair.getKey(), (Integer) pair.getValue());
            userCounter[counter] = userCounterEntry;
            counter++;
        }

        Arrays.sort(userCounter);

        TopThreeUsersImpl topThreeUsers = new TopThreeUsersImpl();
        topThreeUsers.setTopThreeUsers(userCounter);

        return topThreeUsers;
    }

    @Override
    public TopThreeSongs getTopThreeSongsByUser(String user_id) {
        final File dataDirectory = new File(this.dataDirectory);
        HashMap<String, Integer> songUserMap = new HashMap<>();
        SongCounterImpl[] songCounter = new SongCounterImpl[3];

        for (File dataFile: dataDirectory.listFiles()) {
            String line;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                while ((line = reader.readLine()) != null) {
                    String[] lineArray = line.split("\t");
                    String songId = lineArray[0];
                    String userId = lineArray[1];
                    int songTimesPlayed = Integer.parseInt(lineArray[2]);
                    if (userId.equals(user_id)) {
                        songUserMap.putIfAbsent(songId, songTimesPlayed);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Integer> sortedSongUserMap = songUserMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        Iterator it = sortedSongUserMap.entrySet().iterator();
        int counter = 0;
        while (it.hasNext() && counter < 3) {
            Map.Entry pair = (Map.Entry)it.next();
            SongCounterImpl songCounterEntry = new SongCounterImpl((String) pair.getKey(), (Integer) pair.getValue());
            songCounter[counter] = songCounterEntry;
            counter++;
        }

        Arrays.sort(songCounter);
        TopThreeSongsImpl topThreeSongs = new TopThreeSongsImpl();
        topThreeSongs.setTopThreeSongs(songCounter);

        return topThreeSongs;
    }

    @Override
    public UserProfile getUserProfile(String user_id) {
        final File dataDirectory = new File(this.dataDirectory);
        int userTotalPlays = 0;
        ArrayList<SongCounterImpl> songs = new ArrayList<SongCounterImpl>();
        String line;

        for (File dataFile: dataDirectory.listFiles()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                while ((line = reader.readLine()) != null) {
                    String[] lineArray = line.split("\t");
                    String songId = lineArray[0];
                    String userId = lineArray[1];
                    Integer playCount = Integer.parseInt(lineArray[2]);
                    if (userId.equals(user_id)) {
                        userTotalPlays += playCount;
                        songs.add(new SongCounterImpl(songId, playCount));
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return new UserProfileImpl(
                user_id,
                userTotalPlays,
                songs.toArray(new SongCounterImpl[0]),
                getTopThreeSongsByUser(user_id)
        );
    }

}
