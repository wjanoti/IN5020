package Server;

import TasteProfile.TopThreeSongs;

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

}
