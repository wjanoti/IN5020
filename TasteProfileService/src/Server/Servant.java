package Server;

import TasteProfile.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Implementation of the Profiler interface defined in IDL.
 */
public class Servant extends ProfilerPOA {

    // Path of the directory containing the data files.
    private String dataDirectory;
    private boolean useCaching;
    private Map<String, SongProfileImpl> songCache = new ConcurrentHashMap<>((int)(4 * 1e4));
    private Map<String, UserProfileImpl> userCache = new ConcurrentHashMap<>((int) 1e3);

    private Map<String, Integer> userPopularity = new ConcurrentHashMap<>((int)1e6);

    public Servant(String dataDirectory, boolean useCaching) {
        this.dataDirectory = dataDirectory;
        this.useCaching = useCaching;
        if (this.useCaching) {
            long start = System.currentTimeMillis();
            buildPopularitiesMaps();
            System.out.println("Finished building user popularity map");
            buildCaches();
            System.out.println("Finished building user and song caches");
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Building caches took " + elapsed + " ms.");
        }
    }

    private void buildPopularitiesMaps() {
        final File dataDirectory = new File(this.dataDirectory);

        Arrays.stream(dataDirectory.listFiles())
                .forEach(dataFile -> {
                    String line;
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                        while ((line = reader.readLine()) != null) {
                            String[] lineArray = line.split("\t");
                            String songId = lineArray[0];
                            String userId = lineArray[1];
                            int songTimesPlayed = Integer.parseInt(lineArray[2]);

                            if (this.userPopularity.containsKey(userId)) {
                                int currentValue = this.userPopularity.get(userId);
                                this.userPopularity.put(userId, currentValue + songTimesPlayed);
                            }
                            else {
                                this.userPopularity.put(userId, songTimesPlayed);
                            }
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Populates song and user cache
     */
    private void buildCaches() {
        final File dataDirectory = new File(this.dataDirectory);

        // build the top 1000 users
        List<String> topUsers = userPopularity.entrySet().stream().sorted((entry1, entry2) -> {
            return -entry1.getValue().compareTo(entry2.getValue());
        }).limit(1000).map(entry -> {return entry.getKey();}).collect(Collectors.toList());

        HashSet<String> topUserIds = new HashSet<>(topUsers);

        // mark for garbage collection, no need for it anymore
        this.userPopularity = null;

        Arrays.stream(dataDirectory.listFiles())
                .forEach(dataFile -> {
                    String line;
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                        // todo this can be parallelized using reader.lines()
                        while ((line = reader.readLine()) != null) {
                            String[] lineArray = line.split("\t");
                            String songId = lineArray[0];
                            String userId = lineArray[1];
                            int songTimesPlayed = Integer.parseInt(lineArray[2]);

                            UserCounterImpl userCounter = new UserCounterImpl(userId, songTimesPlayed);
                            SongCounterImpl songCounter = new SongCounterImpl(songId, songTimesPlayed);

                            if (songCache.containsKey(songId)) {
                                SongProfileImpl songProfile = songCache.get(songId);
                                songProfile.total_play_count += songTimesPlayed;
                                songProfile.updateTopThreeUsers(userCounter);
                            }
                            else {
                                SongProfileImpl songProfile = new SongProfileImpl();
                                songProfile.total_play_count = songTimesPlayed;
                                songProfile.top_three_users = new TopThreeUsersImpl();
                                songProfile.updateTopThreeUsers(userCounter);
                                songCache.put(songId, songProfile);
                            }

                            if (topUserIds.contains(userId)) {
                                if (userCache.containsKey(userId)) {
                                    UserProfileImpl userProfile = userCache.get(userId);
                                    userProfile.total_play_count += songTimesPlayed;
                                    userProfile.updateTopThreeSongs(songCounter);
                                    userProfile.addSong(songCounter);
                                } else {
                                    UserProfileImpl userProfile = new UserProfileImpl();
                                    userProfile.user_id = userId;
                                    userProfile.total_play_count = songTimesPlayed;
                                    userProfile.top_three_songs = new TopThreeSongsImpl();
                                    userProfile.updateTopThreeSongs(songCounter);
                                    userProfile.addSong(songCounter);
                                    userCache.put(userId, userProfile);
                                }
                            }
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Returns how many times a song was played overall.
     * @param song_id
     * @return how many times a song was played by all users
     */
    @Override
    public int getTimesPlayed(String song_id) {
        try {
            // sleep 80 seconds to mimic transfer latency
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // try to find the song in cache, otherwise return 0 object, since all songs are on the
        // cache, the requested song does not exist at all
        if (this.useCaching) {
            if (songCache.containsKey(song_id)) {
                return songCache.get(song_id).total_play_count;
            }
            return 0;
        }

        AtomicInteger timesPlayed = new AtomicInteger();
        Stream<Path> paths = null;

        try {
            paths = Files.walk(Paths.get(this.dataDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse each data file
        // todo here we can easily switch to parallelStream
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
                                // increment timesPlayed counter
                                timesPlayed.addAndGet(songTimesPlayed);
                            }
                        });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        return timesPlayed.get();
    }

    /**
     * Returns an integer indicating how many times the song was listened to by a give user.
     * @param user_id
     * @param song_id
     * @return number of times the song was listened to by the user.
     */
    @Override
    public int getTimesPlayedByUser(String user_id, String song_id) {
        try {
            // sleep 80 seconds to mimic transfer latency
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int timesPlayedByUser = 0;
        // try to find the user in cache, otherwise parse the files
        if (this.useCaching && userCache.containsKey(user_id)) {
            SongCounter[] userSongs = userCache.get(user_id).songs;
            for (SongCounter userSong : userSongs) {
                if (userSong.song_id.equals(song_id)) {
                    return userSong.songid_play_time;
                }
            }
        } else {
            final File dataDirectory = new File(this.dataDirectory);
            // Parse each data file line by line
            // todo turn this into a stream to be able to parallelStream it
            for (File dataFile : dataDirectory.listFiles()) {
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
        }

        return timesPlayedByUser;
    }

    /**
     * Returns the top three listeners of a given song.
     * @param song_id
     * @return TopThreeUsers object containing the three top listeners of the song.
     */
    @Override
    public TopThreeUsers getTopThreeUsersBySong(String song_id) {
        try {
            // sleep 80 seconds to mimic transfer latency
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // try to find the song in cache, otherwise return a an empty TopThreeUsers object, since all songs are on the
        // cache, the requested song does not exist at all.
        if (this.useCaching) {
            if (songCache.containsKey(song_id)) {
                return songCache.get(song_id).top_three_users;
            }
            return new TopThreeUsersImpl(new UserCounter[3]);
        }

        final File dataDirectory = new File(this.dataDirectory);
        HashMap<String, Integer> userSongMap = new HashMap<>();
        UserCounterImpl[] userCounter = new UserCounterImpl[3];

        // Parse each data file line by line
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
        // todo an alternative approach to sorting would be to use the pushback stuff as we encounter it

        // Order the songs descending by play count.
        HashMap<String, Integer> sortedUserSongMap = userSongMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        // Populate the userCounter array with the top 3 users.
        Iterator it = sortedUserSongMap.entrySet().iterator();
        int counter = 0;
        while (it.hasNext() && counter < 3) {
            Map.Entry pair = (Map.Entry)it.next();
            UserCounterImpl userCounterEntry = new UserCounterImpl((String) pair.getKey(), (Integer) pair.getValue());
            userCounter[counter] = userCounterEntry;
            counter++;
        }

        // Sort ascending by play count.
        Arrays.sort(userCounter);

        return new TopThreeUsersImpl(userCounter);
    }

    /**
     * Returns the top three songs (in play count) for a given user.
     * @param user_id
     * @return TopThreeSongs containing the top three songs listened to by the user.
     */
    @Override
    public TopThreeSongs getTopThreeSongsByUser(String user_id) {
        try {
            // sleep 80 seconds to mimic transfer latency
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TopThreeSongsImpl topThreeSongs = new TopThreeSongsImpl();

        // try to find the user in cache, otherwise parse the files
        if (this.useCaching && userCache.containsKey(user_id)) {
            return userCache.get(user_id).top_three_songs;
        } else {
            final File dataDirectory = new File(this.dataDirectory);
            HashMap<String, Integer> songUserMap = new HashMap<>();
            SongCounterImpl[] songCounter = new SongCounterImpl[3];

            // Parse each data file line by line
            for (File dataFile : dataDirectory.listFiles()) {
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

            // Order the users's songs descending by play count.
            HashMap<String, Integer> sortedSongUserMap = songUserMap.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

            // Add the top 3 songs in the songCounter list.
            Iterator it = sortedSongUserMap.entrySet().iterator();
            int counter = 0;
            while (it.hasNext() && counter < 3) {
                Map.Entry pair = (Map.Entry) it.next();
                SongCounterImpl songCounterEntry = new SongCounterImpl((String) pair.getKey(), (Integer) pair.getValue());
                songCounter[counter] = songCounterEntry;
                counter++;
            }

            // Sort ascending by play count.
            Arrays.sort(songCounter);

            topThreeSongs.setTopThreeSongs(songCounter);
        }

        return topThreeSongs;
    }

    @Override
    public UserProfile getUserProfile(String user_id) {
        try {
            // sleep 80 seconds to mimic transfer latency
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.useCaching && this.userCache.containsKey(user_id)) {
            return this.userCache.get(user_id);
        }

        final File dataDirectory = new File(this.dataDirectory);
        int userTotalPlays = 0;

        HashMap<String, Integer> sortedSongs = new HashMap<>();

        String line;
        // Parse each data file line by line
        // todo switch this to streams
        for (File dataFile: dataDirectory.listFiles()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                // todo this can be turned to reader.lines().parallelStream()
                while ((line = reader.readLine()) != null) {
                    String[] lineArray = line.split("\t");
                    String songId = lineArray[0];
                    String userId = lineArray[1];
                    Integer playCount = Integer.parseInt(lineArray[2]);
                    if (userId.equals(user_id)) {

                        userTotalPlays += playCount;

                        // if the song has been seen before, increment the value
                        if (sortedSongs.containsKey(songId)) {
                            Integer currentValue = sortedSongs.get(songId);
                            sortedSongs.put(songId, currentValue + playCount);
                        }
                        else {
                            // create a new entry in the map
                            sortedSongs.put(songId, playCount);
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        List<SongCounterImpl> songCounters = sortedSongs
                .entrySet()
                .stream()
                .map(entry -> new SongCounterImpl(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        TopThreeSongsImpl impl = new TopThreeSongsImpl();
        UserProfileImpl userProfile = new UserProfileImpl();
        userProfile.user_id = user_id;
        userProfile.songs = songCounters.toArray(new SongCounter[0]);
        userProfile.total_play_count = userTotalPlays;
        userProfile.top_three_songs = new TopThreeSongsImpl();
        for (SongCounterImpl sc : songCounters) {
            userProfile.updateTopThreeSongs(sc);
        }

        return userProfile;
    }
}
