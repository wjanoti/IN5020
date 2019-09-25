package Server;

import TasteProfile.UserCounter;

/**
 * This class is the implementation of the valuetype UserCounter defined in the IDL.
 */
public class UserCounterImpl extends UserCounter implements Comparable<UserCounterImpl> {

    public UserCounterImpl() {
        super();
    }

    public UserCounterImpl(String userId, int songTimesPlayed) {
        this.user_id = userId;
        this.songid_play_time = songTimesPlayed;
    }

    @Override
    public int compareTo(UserCounterImpl o) {
        if (this.user_id == null) {
            return 1;
        }
        if (this.songid_play_time == o.songid_play_time) {
            return 0;
        } else if (this.songid_play_time > o.songid_play_time) {
            return 1;
        }
        return -1;
    }
}
