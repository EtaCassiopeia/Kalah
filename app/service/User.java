package service;

import securesocial.core.BasicProfile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User is part of OAuth2 authentication process and has been borrowed
 * from {@see <a href="http://www.securesocial.ws/">securesocial</a>} project.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public BasicProfile main;
    public List<BasicProfile> identities;

    public User(BasicProfile user) {
        this.main = user;
        identities = new ArrayList<>();
        identities.add(user);
    }

    public BasicProfile getMain() {
        return main;
    }
}
