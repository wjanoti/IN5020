package Server;

import TasteProfile.*;

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

    // Path of the directory containing the data files.
    private String dataDirectory;
    private boolean useCaching;
    private Map<String, SongProfileImpl> songCache = new HashMap<>();
    private Map<String, UserProfileImpl> userCache = new HashMap<>();


    public Servant(String dataDirectory, boolean useCaching) {
        this.dataDirectory = dataDirectory;
        this.useCaching = useCaching;
        if (this.useCaching) {
            buildCache();
        }
    }

    /**
     * Populates song cache
     */
    private void buildCache() {
        final File dataDirectory = new File(this.dataDirectory);
        UserProfileImpl leastPopularUser = null;

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
                    SongCounterImpl currentSong = new SongCounterImpl(songId, songTimesPlayed);
                    UserCounterImpl currentUser = new UserCounterImpl(userId, songTimesPlayed);

                    // update song cache
                    if (!this.songCache.containsKey(songId)) {
                        TopThreeUsersImpl topThreeUsers = new TopThreeUsersImpl();
                        topThreeUsers.addUser(currentUser);
                        SongProfileImpl songProfile = new SongProfileImpl(songTimesPlayed, topThreeUsers);
                        this.songCache.put(songId, songProfile);
                    } else {
                        SongProfileImpl songProfile = this.songCache.get(songId);
                        songProfile.updatePlayCount(songTimesPlayed);
                        songProfile.updateTopThreeUsers(currentUser);
                    }

                    UserProfileImpl currentUserProfile = new UserProfileImpl();
                    // create user profile
                    if (!this.userCache.containsKey(userId)) {
                        TopThreeSongsImpl topThreeSongs = new TopThreeSongsImpl();
                        ArrayList<SongCounter> userSongs = new ArrayList<>();
                        topThreeSongs.addSong(currentSong);
                        userSongs.add(currentSong);
                        currentUserProfile = new UserProfileImpl(userId, songTimesPlayed, userSongs.toArray(new SongCounter[0]), topThreeSongs);
                    } else {
                        currentUserProfile = this.userCache.get(userId);
                        ArrayList<SongCounter> userSongs = new ArrayList<>(Arrays.asList(currentUserProfile.songs));
                        userSongs.add(currentSong);
                        currentUserProfile.updatePlayCount(songTimesPlayed);
                        currentUserProfile.updateTopThreeSongs(currentSong);
                        currentUserProfile.setSongs(userSongs);
                    }

                    if (leastPopularUser == null) {
                        leastPopularUser = currentUserProfile;
                    }

                    // update user cache
                    if (userCache.size() < 1000) {
                        // update the least popular user if the current one is less popular and we still have space on the cache.
                        if (currentUserProfile.total_play_count < leastPopularUser.total_play_count) {
                            leastPopularUser = currentUserProfile;
                        }
                        this.userCache.put(userId, currentUserProfile);
                    } else {
                        // if the cache is full and the current user is more popular we replace it and update the least popular user.
                        if (currentUserProfile.total_play_count > leastPopularUser.total_play_count) {
                            this.userCache.remove(leastPopularUser.user_id);
                            this.userCache.put(userId, currentUserProfile);
                            Comparator<? super Map.Entry<String, UserProfileImpl>> comp = (e1, e2) -> e1.getValue().compareTo(e2.getValue());
                            leastPopularUser = this.userCache.entrySet().stream().min(comp).get().getValue();
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished building cache");
    }

    /**
     * Returns how many times a song was played overall.
     * @param song_id
     * @return how many times a song was played by all users
     */
    @Override
    public int getTimesPlayed(String song_id) {
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
        int timesPlayedByUser = 0;

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
        TopThreeSongsImpl topThreeSongs = new TopThreeSongsImpl();

        if (this.useCaching && userCache.containsKey(user_id)) {
            System.out.println("Found user in cache");
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
        final File dataDirectory = new File(this.dataDirectory);
        int userTotalPlays = 0;
        ArrayList<SongCounterImpl> songs = new ArrayList<SongCounterImpl>();
        String line;

        // Parse each data file line by line
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
