package Server;

import TasteProfile.UserCounter;

/**
 * This class is the implementation of the valuetype UserCounter defined in the IDL.
 */
public class UserCounterImpl extends UserCounter {

    private String user_id;
    // This is actually the number of times the user played a given song. The name in the interface is a bit misleading.
    private int songid_play_time;

    public void setUser_id(String userId) {
        this.user_id = userId;
    }

    public void setsongid_play_time(int songTimesPlayed) {
        this.songid_play_time = songTimesPlayed;
    }

}
