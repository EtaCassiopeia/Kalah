package controllers;

import javax.inject.Singleton;
import play.mvc.*;
import views.html.index;

@Singleton
public class Application extends Controller{
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }
}
