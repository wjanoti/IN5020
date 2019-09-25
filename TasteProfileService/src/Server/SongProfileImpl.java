package Server;

import TasteProfile.SongProfile;
import TasteProfile.TopThreeUsers;

import java.util.Map;

public class SongProfileImpl extends SongProfile {

    public SongProfileImpl() {
        super();
    }

    public SongProfileImpl(int totalPlayCount, TopThreeUsers topThreeUsers) {
        this.total_play_count = totalPlayCount;
        this.top_three_users = topThreeUsers;
    }

    /**
     * Used when building the cache, increments a song total play count
     * @param playCount
     */
    public void updatePlayCount(int playCount) {
        this.total_play_count += playCount;
    }

    /**
     * Used when building the cache, updates the associated topThreeUsers list of a song.
     * @param newUser
     */
    public void updateTopThreeUsers(UserCounterImpl newUser) {
        for (int i = this.top_three_users.topThreeUsers.length - 1; i >= 0; i--) {
            if (this.top_three_users.topThreeUsers[i] == null
                    || newUser.songid_play_time > this.top_three_users.topThreeUsers[i].songid_play_time) {
                // pushback the existing values
                System.arraycopy(this.top_three_users.topThreeUsers, i, this.top_three_users.topThreeUsers, i + 1, this.top_three_users.topThreeUsers.length - 1 - i);

                // write the new one in it's place
                this.top_three_users.topThreeUsers[i] = newUser;
            }
            else {
                break;
            }
        }
    }
}
