package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.Lists;
import play.libs.akka.InjectedActorSupport;
import scala.collection.immutable.Iterable;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static protocol.ConnectionProtocol.*;

/***
 * SupervisorActor class controls actor creation process. This game creates a separate actor per player.
 * SupervisorActor is responsible for managing the lifecycle of child actors and also plays a vital role in dispatching
 * messages between player actors.
 */
public class SupervisorActor extends AbstractActor implements InjectedActorSupport {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    private Map<String, ActorRef> activeGames = new HashMap<>();

    public static class Create {
        private String id;
        private ActorRef out;

        public Create(String id, ActorRef out) {
            this.id = id;
            this.out = out;
        }
    }

    private PlayerActor.Factory childFactory;

    @Inject
    public SupervisorActor(PlayerActor.Factory childFactory) {
        this.childFactory = childFactory;

        receive(ReceiveBuilder
                .match(Create.class, s -> {
                    ActorRef child = injectedChild(() -> this.childFactory.create(s.id, s.out), s.id);
                    sender().tell(child, self());

                    Players players = getPlayers();
                    getContext().getChildren().forEach(r -> r.tell(players, self()));
                })
                .match(GetPlayers.class, s -> sender().tell(getPlayers(), self()))
                .match(CreateGame.class, s -> {
                    //TODO check valid player names
                    ActorRef opponent = getContext().getChild(s.getOpponentPlayer());
                    opponent.tell(new AreYouWantToPlayWith(s.currentPlayer), self());
                })
                .match(AreYouWantToPlayWithResponse.class, s -> {
                    String player1Name=s.getCurrentPlayer();
                    String player2Name=s.getOpponentPlayer();

                    ActorRef player1 = getContext().getChild(player1Name);
                    ActorRef player2 = getContext().getChild(player2Name);
                    if (null == player1 || null == player2) {
                        //TODO check players
                        //TODO send error message
                    } else {
                        if (s.getResponse() == Response.ACCEPT) {
                            String gameId = UUID.randomUUID().toString();
                            ActorRef actorRef = getContext().actorOf(Props.create(GameBoardActor.class, player1, player2), gameId);
                            activeGames.put(gameId, actorRef);

                            player1.tell(new GameStarted(gameId,player1Name,player2Name), actorRef);
                            player2.tell(new GameStarted(gameId,player1Name,player2Name), actorRef);

                        }
                    }
                })
                .build());
    }

    /**
     * This method returns current online players.
     * Each player is represented as an actor. Therefore, Any child of SupervisorActor is considered as an online player
     * @return online players of the system
     */
    private Players getPlayers() {
        return new Players(
                Lists.newArrayList(getContext().getChildren())
                        .stream()
                        .map(a -> a.path().name())
                        .collect(Collectors.toList()));
    }

    /**
     * GetPlayers will be used as an inline message template to request current online users
     */
    public static class GetPlayers {
    }

    /**
     * Players is a wrapper class. It is used as a response to the {@see GetPlayers} request
     */
    public static class Players {
        private List<String> players;

        public Players(List<String> players) {
            this.players = players;
        }

        public List<String> getPlayers() {
            return players;
        }
    }

    /**
     * CreateGame is an inline class to make a request to creating a game between two online players
     */
    public static class CreateGame {
        private String currentPlayer;
        private String opponentPlayer;

        public CreateGame(String currentPlayer, String opponentPlayer) {
            this.currentPlayer = currentPlayer;
            this.opponentPlayer = opponentPlayer;
        }

        public String getCurrentPlayer() {
            return currentPlayer;
        }

        public String getOpponentPlayer() {
            return opponentPlayer;
        }
    }
}

