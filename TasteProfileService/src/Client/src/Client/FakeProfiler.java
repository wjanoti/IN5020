package Client;

import Server.*;
import TasteProfile.*;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;

import java.util.Random;

public class FakeProfiler implements Profiler {
    private Random rng;
    public FakeProfiler() {
        rng = new Random();
    }
    @Override
    public int getTimesPlayed(String song_id) {
        try {
            Thread.sleep(rng.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int getTimesPlayedByUser(String user_id, String song_id) {
        try {
            Thread.sleep(rng.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public TopThreeUsers getTopThreeUsersBySong(String song_id) {
        try {
            Thread.sleep(rng.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TopThreeUsers result = new TopThreeUsersImpl();

        UserCounter userCounter = new UserCounterImpl();
        userCounter.songid_play_time = 10;
        userCounter.user_id = "userId";

        result.topThreeUsers = new UserCounter[3];
        result.topThreeUsers[0] = userCounter;
        result.topThreeUsers[1] = userCounter;
        result.topThreeUsers[2] = userCounter;

        return result;
    }

    @Override
    public TopThreeSongs getTopThreeSongsByUser(String user_id) {
        try {
            Thread.sleep(rng.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TopThreeSongs result = new TopThreeSongsImpl();

        SongCounter counter = new SongCounterImpl();
        counter.song_id = "song_id";
        counter.songid_play_time = 10;

        result.topThreeSongs = new SongCounter[3];
        result.topThreeSongs[0] = counter;
        result.topThreeSongs[1] = counter;
        result.topThreeSongs[2] = counter;

        return result;
    }

    @Override
    public UserProfile getUserProfile(String user_id) {
        try {
            Thread.sleep(rng.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UserProfile impl = new UserProfileImpl();
        impl.top_three_songs = getTopThreeSongsByUser(user_id);
        impl.total_play_count = 100;
        impl.user_id = user_id;
        SongCounter counter = new SongCounterImpl();
        counter.song_id = "song_id";
        counter.songid_play_time = 10;
        impl.songs = new SongCounter[10];
        for (int i = 0; i < 10; i++) {
            impl.songs[i] = counter;
        }
        return impl;
    }

    @Override
    public boolean _is_a(String s) {
        return false;
    }

    @Override
    public boolean _is_equivalent(Object object) {
        return false;
    }

    @Override
    public boolean _non_existent() {
        return false;
    }

    @Override
    public int _hash(int i) {
        return 0;
    }

    @Override
    public Object _duplicate() {
        return null;
    }

    @Override
    public void _release() {

    }

    @Override
    public Object _get_interface_def() {
        return null;
    }

    @Override
    public Request _request(String s) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue, ExceptionList exceptionList, ContextList contextList) {
        return null;
    }

    @Override
    public Policy _get_policy(int i) {
        return null;
    }

    @Override
    public DomainManager[] _get_domain_managers() {
        return new DomainManager[0];
    }

    @Override
    public Object _set_policy_override(Policy[] policies, SetOverrideType setOverrideType) {
        return null;
    }
}
