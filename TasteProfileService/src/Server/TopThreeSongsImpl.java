package Server;

import TasteProfile.SongCounter;
import TasteProfile.TopThreeSongs;
import TasteProfile.UserCounter;

/**
 * This class is the implementation of the valuetype TopThreeSongs defined in the IDL.
 */
public class TopThreeSongsImpl extends TopThreeSongs {

    /**
     * @param topThreeSongs List of UserCounterImpl representing the top three listeners of a given song.
     */
    public void setTopThreeSongs(SongCounterImpl[] topThreeSongs) {
        this.topThreeSongs = topThreeSongs;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (SongCounter song: this.topThreeSongs) {
            res.append(song.song_id).append(" ").append(song.songid_play_time).append(" times.\n");
        }
        return res.toString();
    }
}
