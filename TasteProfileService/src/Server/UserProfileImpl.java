package Server;

import TasteProfile.SongCounter;
import TasteProfile.TopThreeSongs;
import TasteProfile.UserProfile;

import java.util.ArrayList;

/**
 * This class is the implementation of the valuetype UserProfile defined in the IDL.
 */
public class UserProfileImpl extends UserProfile implements Comparable<UserProfileImpl> {

    public UserProfileImpl() {
        super();
    }

    public UserProfileImpl(String userId, int totalPlayCount, SongCounter[] songs, TopThreeSongs topThreeSongs) {
        this.user_id = userId;
        this.total_play_count = totalPlayCount;
        this.songs = songs;
        this.top_three_songs = topThreeSongs;
    }

    /**
     * Used when building the cache, updates the associated topThreeSongs list of a user.
     * @param newSong
     */
    public void updateTopThreeSongs(SongCounterImpl newSong) {
        ((TopThreeSongsImpl) this.top_three_songs).addSong(newSong);
    }

    /**
     * Used when building the cache, increments a user total play count
     * @param playCount
     */
    public void updatePlayCount(int playCount) {
        this.total_play_count += playCount;
    }

    /**
     * Used when building the cache, set the song list for a user.
     * @param songList
     */
    public void setSongs(ArrayList<SongCounter> songList) {
        this.songs = songList.toArray(new SongCounter[0]);
    }

    @Override
    public int compareTo(UserProfileImpl o) {
        if (this.total_play_count == o.total_play_count) {
            return 0;
        } else if (this.total_play_count > o.total_play_count) {
            return 1;
        }
        return -1;
    }
}
