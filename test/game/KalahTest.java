package game;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class KalahTest {
    @Test
    public void completeTest() {
        String player1 = "player1";
        String player2 = "player2";

        Kalah kalah = new Kalah(player1, player2,
                (l) -> System.out.println("Move completed: " + l.toString()),
                (s, i) -> System.out.println("We have a lucky winner: " + s + " " + i));


        final Movement[] moves = new Movement[]{
                new Movement(player1, 1),
                new Movement(player1, 2),
                new Movement(player2, 1),
                new Movement(player1, 1),
                new Movement(player2, 6),
                new Movement(player1, 2),
                new Movement(player2, 4),
                new Movement(player1, 2),
                new Movement(player2, 3),
                new Movement(player1, 2),
                new Movement(player2, 2),
                new Movement(player1, 1),
                new Movement(player2, 6),
                new Movement(player1, 2),
                new Movement(player2, 5),
                new Movement(player1, 1),
                new Movement(player2, 6),
                new Movement(player2, 4),
                new Movement(player1, 2),
                new Movement(player2, 5),
                new Movement(player1, 6),
                new Movement(player2, 3),
                new Movement(player1, 1),
                new Movement(player2, 6),
                new Movement(player2, 4),
                new Movement(player1, 5),
                new Movement(player2, 6),
                new Movement(player2, 5),
                new Movement(player2, 4),
                new Movement(player1, 6),
                new Movement(player1, 1),
                new Movement(player2, 6),
                new Movement(player2, 2),
                new Movement(player1, 4),
                new Movement(player2, 1),
                new Movement(player1, 6),
                new Movement(player1, 5),
                new Movement(player2, 3),
                new Movement(player2, 5),
                new Movement(player2, 4),
                new Movement(player1, 1)
        };

        Arrays.stream(moves).forEach(m -> {
            kalah.move(m.player, m.startPitIndex);
        });

    }

    private class Movement {
        private String player;
        private Integer startPitIndex;

        Movement(String player, Integer startPitIndex) {
            this.player = player;
            this.startPitIndex = startPitIndex;
        }
    }
}
