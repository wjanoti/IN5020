package Server;

import TasteProfile.SongProfile;

public class SongProfileImpl extends SongProfile {

    public SongProfileImpl() {
        super();
    }

    /**
     * Used when building the cache, updates the associated topThreeUsers list of a song.
     * @param newUser
     */
    public void updateTopThreeUsers(UserCounterImpl newUser) {
        // todo this can be changed with compareTo when validated they are working the same
        for (int i = 0; i < 3; i++) {
            if (this.top_three_users.topThreeUsers[i] == null
                    || newUser.songid_play_time > this.top_three_users.topThreeUsers[i].songid_play_time)
            {
                if (i > 0) {
                    this.top_three_users.topThreeUsers[i - 1] = this.top_three_users.topThreeUsers[i];
                }
                this.top_three_users.topThreeUsers[i] = newUser;
            }
        }
    }
}
