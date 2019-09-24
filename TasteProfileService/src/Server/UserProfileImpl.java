package Server;

import TasteProfile.SongCounter;
import TasteProfile.TopThreeSongs;
import TasteProfile.TopThreeUsers;
import TasteProfile.UserProfile;

/**
 * This class is the implementation of the valuetype UserProfile defined in the IDL.
 */
public class UserProfileImpl extends UserProfile {

    public UserProfileImpl() {
        super();
    }

    public UserProfileImpl(String userId, int totalPlayCount, SongCounter[] songs, TopThreeSongs topThreeSongs) {
        this.user_id = userId;
        this.total_play_count = totalPlayCount;
        this.songs = songs;
        this.top_three_songs = topThreeSongs;
    }

}
