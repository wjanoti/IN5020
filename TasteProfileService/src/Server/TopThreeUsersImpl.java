package Server;

import TasteProfile.TopThreeUsers;
import TasteProfile.UserCounter;

/**
 * This class is the implementation of the valuetype TopThreeUsers defined in the IDL.
 */
public class TopThreeUsersImpl extends TopThreeUsers {

    public TopThreeUsersImpl() {
        super();
    }

    /**
     * @param topThreeUsers List of UserCounterImpl representing the top three listeners of a given song.
     */
    public TopThreeUsersImpl(UserCounter[] topThreeUsers) {
        this.topThreeUsers = topThreeUsers;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (UserCounter user: this.topThreeUsers) {
            res.append(user.user_id).append(" ").append(user.songid_play_time).append(" times.\n");
        }

        return res.toString();
    }
}
