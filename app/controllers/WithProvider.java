package controllers;

import securesocial.core.java.Authorization;
import service.User;

/**
 * WithProvider is part of OAuth2 authentication process and has been borrowed
 * from {@see <a href="http://www.securesocial.ws/">securesocial</a>} project.
 */
public class WithProvider implements Authorization<User> {
    public boolean isAuthorized(User user, String params[]) {
        return user.main.providerId().equals(params[0]);
    }
}