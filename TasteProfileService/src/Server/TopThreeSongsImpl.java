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

}
