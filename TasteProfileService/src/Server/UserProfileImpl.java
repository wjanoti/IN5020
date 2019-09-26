package Server;

import TasteProfile.SongCounter;
import TasteProfile.TopThreeSongs;
import TasteProfile.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is the implementation of the valuetype UserProfile defined in the IDL.
 */
public class UserProfileImpl extends UserProfile implements Comparable<UserProfileImpl> {

    /**
     * Resizable list as buffer
     */
    private List<SongCounter> songCounterList = new ArrayList<>();

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
     * Used when building the cache, updates the associated topThreeSongs list of a user.user_id
     * Keeps the order as it is required.
     * @param newSong
     */
    public void updateTopThreeSongs(SongCounterImpl newSong) {
        for (int i = 0; i < 3; i++) {
            if (this.top_three_songs.topThreeSongs[i] == null
                    || newSong.songid_play_time > this.top_three_songs.topThreeSongs[i].songid_play_time)
            {
                if (i > 0) {
                    this.top_three_songs.topThreeSongs[i - 1] = this.top_three_songs.topThreeSongs[i];
                }
                this.top_three_songs.topThreeSongs[i] = newSong;
            }
        }
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

    public void addSong(SongCounterImpl songCounter) {
        // this is a very inefficient way to add stuff because we will create a new array everytime
        this.songCounterList.add(songCounter);

        this.songs = this.songCounterList.toArray(new SongCounter[0]);
    }
}
