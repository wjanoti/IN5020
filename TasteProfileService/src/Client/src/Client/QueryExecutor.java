package Client;

import TasteProfile.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QueryExecutor {

    private Profiler profilerRef;
    private boolean useCaching;
    private Logger logger;

    private Map<String, UserProfile> cache = new HashMap<>();

    public QueryExecutor(Profiler profilerRef, boolean useCaching, Logger logger) {
        this.profilerRef = profilerRef;
        this.useCaching = useCaching;
        this.logger = logger;
    }

    /**
     * Executes a query using the corresponding server method and logs the result to the appropriate file
     * @param q query to execute
     */
    public void executeQuery(Query q) {
        switch (q.name) {
            case "getTimesPlayedByUser":
                this.getTimesPlayedByUser(q.args.get(0), q.args.get(1));
                break;
            case "getTimesPlayed":
                this.getTimesPlayed(q.args.get(0));
                break;
            case "getTopThreeUsersBySong":
                this.getTopThreeUsersBySong(q.args.get(0));
                break;
            case "getTopThreeSongsByUser":
                this.getTopThreeSongsByUser(q.args.get(0));
                break;
        }
    }

    private void getTopThreeSongsByUser(String userId) {
        TopThreeSongs songs = null;
        if (useCaching) {
            if (cache.containsKey(userId)) {
                UserProfile profile = cache.get(userId);
                songs = profile.top_three_songs;
            }
            else {
                // todo add call to the server here for getUserProfile
                UserProfile profile = null;
                cache.put(userId, profile);
                songs = profile.top_three_songs;
            }
        }
        else {
             songs = profilerRef.getTopThreeSongsByUser(userId);
        }

        for (SongCounter songCounter:songs.topThreeSongs) {
            String line = String.format("Song %s was played %d times.", songCounter.song_id, songCounter.songid_play_time);
            logger.LogLine("getTopThreeSongsByUser", line);
        }
    }

    private void getTopThreeUsersBySong(String songId) {
        TopThreeUsers users = profilerRef.getTopThreeUsersBySong(songId);

        for (UserCounter userCounter:users.topThreeUsers) {
            String line = String.format("User %s played %d times.", userCounter.user_id, userCounter.songid_play_time);
            logger.LogLine("getTopThreeUsersBySong", line);
        }
    }

    private void getTimesPlayed(String songId) {
        long startTimeMs = System.currentTimeMillis();
        long timesPlayed = this.profilerRef.getTimesPlayed(songId);
        long elapsed = System.currentTimeMillis() - startTimeMs;
        logger.LogLine("getTimesPlayed", String.format("Song %s played %d times. (%d ms)", songId, timesPlayed, elapsed));
    }

    private void getTimesPlayedByUser(String userId, String songId) {
        long startTimeMs = System.currentTimeMillis();
        long timesPlayed = 0;
        if (useCaching) {
            if (cache.containsKey(userId)) {
                UserProfile profile = cache.get(userId);
                timesPlayed = Arrays.stream(profile.songs)
                        .filter(sc -> sc.song_id.equals(songId))
                        .count();
            }
            else {
                // todo add call to the server here for getUserProfile
                UserProfile profile = null;
                cache.put(userId, profile);
                timesPlayed = Arrays.stream(profile.songs)
                        .filter(sc -> sc.song_id.equals(songId))
                        .count();
            }
        }
        else {
            timesPlayed = this.profilerRef.getTimesPlayedByUser(userId, songId);
        }
        long elapsed = System.currentTimeMillis() - startTimeMs;

        logger.LogLine("getTimesPlayedByUser", String.format("Song %s played %d times by user %s. (%d ms)", userId, timesPlayed, songId, elapsed));
    }
}
