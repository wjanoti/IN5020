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

    @Override
    public String toString() {
        String ret = "User id: " + this.user_id  + " | ";
        ret += " Total plays: " + this.total_play_count + " | ";
        ret += " Top 3 Songs: " + this.top_three_songs + " | ";
        String songIds = "";
        for (SongCounter song: this.songs) {
            songIds += song.song_id + " , ";
        }
        ret += " Songs: " + songIds;
        return ret;
    }
}
