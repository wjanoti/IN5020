package Server;

import TasteProfile.UserCounter;

/**
 * This class is the implementation of the valuetype UserCounter defined in the IDL.
 */
public class UserCounterImpl extends UserCounter implements Comparable<UserCounterImpl> {

    public void setUser_id(String userId) {
        this.user_id = userId;
    }

    public void setsongid_play_time(int songTimesPlayed) {
        this.songid_play_time = songTimesPlayed;
    }

    @Override
    public int compareTo(UserCounterImpl o) {
        if (this.songid_play_time == o.songid_play_time) {
            return 0;
        } else if (this.songid_play_time > o.songid_play_time) {
            return 1;
        }
        return -1;
    }
}
