package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.Lists;
import play.libs.akka.InjectedActorSupport;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static protocol.ConnectionProtocol.*;

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
                    ActorRef child = injectedChild(() -> childFactory.create(s.id, s.out), s.id);
                    sender().tell(child, self());
                })
                .match(GetPlayers.class, s -> {
                    sender().tell(
                            new Players(
                                    Lists.newArrayList(getContext().getChildren())
                                            .stream()
                                            .map(a -> a.path().name())
                                            .collect(Collectors.toList())),
                            self());
                })
                .match(CreateGame.class, s -> {
                    //TODO check valid player names
                    ActorRef opponent = getContext().getChild(s.getOpponentPlayer());
                    opponent.tell(new AreYouWantToPlayWith(s.currentPlayer), self());
                })
                .match(AreYouWantToPlayWithResponse.class, s -> {
                    ActorRef player1 = getContext().getChild(s.getCurrentPlayer());
                    ActorRef player2 = getContext().getChild(s.getOpponentPlayer());
                    if (null == player1 || null == player2) {
                        //TODO check players
                        //TODO send error message
                    } else {
                        if (s.getResponse() == Response.ACCEPT) {
                            String gameId = UUID.randomUUID().toString();
                            ActorRef actorRef = getContext().actorOf(Props.create(GameBoardActor.class, player1, player2), gameId);
                            activeGames.put(gameId, actorRef);

                            player1.tell(new GameStarted(gameId), actorRef );
                            player2.tell(new GameStarted(gameId), actorRef);

                        }
                    }
                })
                .build());
    }

    public static class GetPlayers {
    }

    public static class Players {
        private List<String> players;

        public Players(List<String> players) {
            this.players = players;
        }

        public List<String> getPlayers() {
            return players;
        }
    }

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

