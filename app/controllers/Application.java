package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import service.User;
import views.html.index;
import com.google.inject.Inject;

import javax.inject.Singleton;

/**
 * Application class is the entry point of the web application
 */
@Singleton
public class Application extends Controller{

    private RuntimeEnvironment env;

    @Inject()
    public Application (RuntimeEnvironment env) {
        this.env = env;
    }

    @SecuredAction
    public Result index() {
        User user = (User) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user, SecureSocial.env()));
    }
}
