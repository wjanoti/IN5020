package Client;

import TasteProfile.UserProfile;

import javax.jws.soap.SOAPBinding;
import java.util.Arrays;
import java.util.Map;

public class QueryExecutor {

    private boolean useCaching = false;

    private Map<String, UserProfile> cache;

    public QueryExecutor(boolean useCaching) {
        this.useCaching = useCaching;
    }

    public void executeQuery(Query q) {

        long startTimeMs = System.currentTimeMillis();

        String result = "";
        switch (q.name) {
            case "getTimesPlayedByUser":
                result = this.getTimesPlayedByUser(q.args.get(0), q.args.get(1));
                break;
            case "getTimesPlayed":
                result = this.getTimesPlayed(q.args.get(0));
                break;
            case "getTopThreeUsersBySong":
                result = this.getTopThreeUsersBySong(q.args.get(0));
                break;
            case "getTopThreeSongsByUser":
                result = this.getTopThreeSongsByUser(q.args.get(0));
                break;
        }

        long execTimeMs = System.currentTimeMillis() - startTimeMs;

        String executionResult = String.format("%s (%d ms)", result, execTimeMs);

        System.out.println(executionResult);
        // todo route this execution result to the required logger
    }

    private String getTopThreeSongsByUser(String userId) {
        // todo come up with a way to handle multiple lines of output + no time measurement
        return "getTopThreeSongsByUser";
    }

    private String getTopThreeUsersBySong(String songId) {
        // todo come up with a way to handle multiple lines of output + no time measurement
        return "getTopThreeUsersBySong";
    }

    private String getTimesPlayed(String songId) {
        // todo add call to the server here
        long timesPlayed = 3;
        return String.format("Song %s played %d times.", songId, timesPlayed);
    }

    private String getTimesPlayedByUser(String userId, String songId) {

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
            // todo add call to the server here for getTimesPlayedByUser
            timesPlayed = 3;
        }


        return String.format("Song %s played %d times by user %s.", userId, timesPlayed, songId);
    }
}
