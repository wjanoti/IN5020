package Server;

import TasteProfile.TopThreeUsers;

/**
 * This class is the implementation of the valuetype TopThreeUsers defined in the IDL.
 */
public class TopThreeUsersImpl extends TopThreeUsers {

    /**
     * @param topThreeUsers List of UserCounterImpl representing the top three listeners of a given song.
     */
    public void setTopThreeUsers(UserCounterImpl[] topThreeUsers) {
        this.topThreeUsers = topThreeUsers;
    }

}
