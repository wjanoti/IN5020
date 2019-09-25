package Server;

import TasteProfile.TopThreeUsers;
import TasteProfile.UserCounter;

import java.util.Arrays;

/**
 * This class is the implementation of the valuetype TopThreeUsers defined in the IDL.
 */
public class TopThreeUsersImpl extends TopThreeUsers {

    public TopThreeUsersImpl() {
        super();
        this.topThreeUsers = new UserCounter[3];
    }

    /**
     * @param topThreeUsers List of UserCounterImpl representing the top three listeners of a given song.
     */
    public TopThreeUsersImpl(UserCounter[] topThreeUsers) {
        this.topThreeUsers = topThreeUsers;
    }

    /**
     * Used when building the cache. Adds a user to the list if its play count is greater than any one of the others.
     * @param newUser
     */
    public void addUser(UserCounterImpl newUser) {
        for (int i = 0; i < this.topThreeUsers.length; i++) {
            if (this.topThreeUsers[i] == null) {
                this.topThreeUsers[i] = newUser;
                return;
            }
        }
        if (this.topThreeUsers[2] != null) {
            Arrays.sort(this.topThreeUsers);
            if (newUser.songid_play_time > this.topThreeUsers[0].songid_play_time) {
                this.topThreeUsers[0] = newUser;
            }
        }
    }

}
