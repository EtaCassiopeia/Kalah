import actors.*;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;
import securesocial.core.RuntimeEnvironment;
import service.MyEnvironment;

@SuppressWarnings("unused")
public class Module extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        bind(RuntimeEnvironment.class).to(MyEnvironment.class);
        bindActor(SupervisorActor.class, "supervisorActor");
        bindActorFactory(PlayerActor.class, PlayerActor.Factory.class);
    }
}

