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

}
