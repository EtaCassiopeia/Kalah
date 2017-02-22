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
import static protocol.ConnectionProtocol.Won;

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
                (states) -> states.forEach(state -> actorRefMap.get(state.getPlayer()).tell(state, self())),
                (winner, stones) -> {
                    Won wonMessage = new Won(winner, stones);
                    actorRefMap.forEach((actorName, actorRef) -> actorRef.tell(wonMessage, self()));
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
