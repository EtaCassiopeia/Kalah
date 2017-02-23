package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import game.Kalah;

import java.util.HashMap;
import java.util.Map;

import static protocol.ConnectionProtocol.Move;
import static protocol.ConnectionProtocol.GameFinished;

/**
 * GameBoardActor will be created for each confirmed play request between two players.
 * It creates an instance of {@see game.Kalah} class and performs player movement requests by
 * calling the {@see game.Kalah#move(Strin, String)} method.
 */
public class GameBoardActor extends AbstractActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private Map<String, ActorRef> actorRefMap = new HashMap<>(2);

    private Kalah game;

    public GameBoardActor(ActorRef player1, ActorRef player2) {

        String player1Name = name(player1);
        String player2Name = name(player2);

        actorRefMap.put(player1Name, player1);
        actorRefMap.put(player2Name, player2);

        game = new Kalah(
                player1Name,
                player2Name,
                (states) -> actorRefMap.forEach((p, ref) -> ref.tell(states, self())),
                (winner, stones) -> {
                    GameFinished gameFinishedMessage = new GameFinished(winner, stones);
                    actorRefMap.forEach((actorName, actorRef) -> actorRef.tell(gameFinishedMessage, self()));
                }
        );

        receive(ReceiveBuilder
                .match(Move.class, s -> {
                    game.move(name(sender()), s.getStartPitIndex());
                }).build());
    }

    private String name(ActorRef ref) {
        return ref.path().name();
    }
}
