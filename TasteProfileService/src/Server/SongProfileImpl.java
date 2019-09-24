package Server;

import TasteProfile.SongProfile;

public class SongProfileImpl extends SongProfile {

    public void setPlayCount(int count) {
        this.total_play_count = count;
    }

    public void setTopThreeUsers(TopThreeUsersImpl top) {
        this.top_three_users = top;
    }
}
