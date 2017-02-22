package actors;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.assistedinject.Assisted;
import play.Configuration;
import play.libs.Json;
import protocol.SerializableCollection;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Collectors;

import static protocol.ConnectionProtocol.*;
import static util.ClassMatcher.match;

public class PlayerActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Configuration configuration;

    private String currentGameId;
    private Optional<ActorRef> gameActorRef;

    @SuppressWarnings("unchecked")
    @Inject
    public PlayerActor(@Assisted String playerName,
                       @Assisted ActorRef out,
                       Configuration configuration) {
        this.configuration = configuration;

        receive(ReceiveBuilder.
                match(JsonNode.class, s -> {
                    SerializableJsonObject event = Json.fromJson(s, SerializableJsonObject.class);
                    match()
                            .with(RequestPlayersList.class, x -> {
                                getContext().parent().tell(new SupervisorActor.GetPlayers(), self());
                            })
                            .with(GameRequest.class, x -> {
                                //TODO implement timeout
                                getContext().parent().tell(
                                        new SupervisorActor.CreateGame(playerName, x.getOpponentPlayer()),
                                        self());
                            })
                            .with(AreYouWantToPlayWithResponse.class, x -> getContext().parent().tell(x, self()))
                            .with(Move.class, x -> {
                                gameActorRef.ifPresent(r -> r.tell(x, self()));
                            })
                            .fallthrough(i -> log.info("received unknown message"))
                            .exec(event);

                })
                .match(SupervisorActor.Players.class, x -> {
                    out.tell(Json.toJson(new SerializableCollection((x).getPlayers()
                            .stream()
                            .filter(n -> !n.equals(playerName))
                            .collect(Collectors.toList()))), self());
                })
                .match(AreYouWantToPlayWith.class, x -> out.tell(Json.toJson(x), self()))
                .match(GameStarted.class, x -> {
                    currentGameId = x.getGameId();
                    gameActorRef = Optional.of(sender());
                    out.tell(Json.toJson(x), self());
                })
                .match(PlayerBoardState.class, x -> out.tell(Json.toJson(x), self()))
                .match(Won.class, x -> out.tell(Json.toJson(x), self()))
                .matchAny(o -> log.info("received unknown message"))
                .build());
    }

    public interface Factory {
        Actor create(String id, ActorRef out);
    }
}
