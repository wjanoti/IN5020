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
     * Used when building the cache, updates the associated topThreeSongs list of a user.
     * @param newSong
     */
    public void updateTopThreeSongs(SongCounterImpl newSong) {
        for (int i = this.top_three_songs.topThreeSongs.length - 1; i >= 0; i--) {
            if (this.top_three_songs.topThreeSongs[i] == null
                    || newSong.songid_play_time > this.top_three_songs.topThreeSongs[i].songid_play_time) {
                // pushback the existing values
                System.arraycopy(this.top_three_songs.topThreeSongs, i, this.top_three_songs.topThreeSongs, i + 1, this.top_three_songs.topThreeSongs.length - 1 - i);

                // write the new one in it's place
                this.top_three_songs.topThreeSongs[i] = newSong;
            }
            else {
                break;
            }
        }
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

    public void addSong(SongCounterImpl songCounter) {
        // this is a very inefficient way to add stuff because we will create a new array everytime
        this.songCounterList.add(songCounter);

        this.songs = this.songCounterList.toArray(new SongCounter[0]);
    }
}
