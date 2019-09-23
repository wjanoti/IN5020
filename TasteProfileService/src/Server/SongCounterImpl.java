package Server;

import TasteProfile.SongCounter;

/**
 * This class is the implementation of the valuetype SongCounter defined in the IDL.
 */
public class SongCounterImpl extends SongCounter implements Comparable<SongCounterImpl> {

    public void setSong_id(String songId) { this.song_id = songId; }

    public void setsongid_play_time(int songTimesPlayed) {
        this.songid_play_time = songTimesPlayed;
    }

    @Override
    public int compareTo(SongCounterImpl o) {
        if (this.songid_play_time == o.songid_play_time) {
            return 0;
        } else if (this.songid_play_time > o.songid_play_time) {
            return 1;
        }
        return -1;
    }
}
