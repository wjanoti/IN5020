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

}
