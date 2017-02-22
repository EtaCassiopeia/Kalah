package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecuredAction;
import views.html.index;

import javax.inject.Singleton;

@Singleton
public class Application extends Controller{

    @SecuredAction
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }
}
