package Server;

import TasteProfile.SongCounter;
import TasteProfile.TopThreeSongs;

import java.util.Arrays;

/**
 * This class is the implementation of the valuetype TopThreeSongs defined in the IDL.
 */
public class TopThreeSongsImpl extends TopThreeSongs {


    public TopThreeSongsImpl() {
        super();
        this.topThreeSongs = new SongCounter[3];
    }
    /**
     * @param topThreeSongs List of UserCounterImpl representing the top three listeners of a given song.
     */
    public void setTopThreeSongs(SongCounterImpl[] topThreeSongs) {
        this.topThreeSongs = topThreeSongs;
    }

    /**
     * Used when building the cache. Adds a song to the list if its play count is greater than any one of the others.
     * @param newSong
     */
    public void addSong(SongCounterImpl newSong) {
        for (int i = 0; i < this.topThreeSongs.length; i++) {
            if (this.topThreeSongs[i] == null) {
                this.topThreeSongs[i] = newSong;
                return;
            }
        }
        if (this.topThreeSongs[2] != null) {
            Arrays.sort(this.topThreeSongs);
            if (newSong.songid_play_time > this.topThreeSongs[0].songid_play_time) {
                this.topThreeSongs[0] = newSong;
            }
        }
    }

}
