package Server;

import TasteProfile.ProfilerPOA;
import TasteProfile.TopThreeSongs;
import TasteProfile.TopThreeUsers;

public class Servant extends ProfilerPOA {

    @Override
    public int getTimesPlayed(String song_id) {
        return 0;
    }

    @Override
    public int getTimesPlayedByUser(String user_id, String song_id) {
        return 0;
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
