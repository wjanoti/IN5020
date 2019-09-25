package Server;

import TasteProfile.SongProfile;
import TasteProfile.TopThreeUsers;

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
        ((TopThreeUsersImpl) this.top_three_users).addUser(newUser);
    }
}
