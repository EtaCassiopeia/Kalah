package game;

import com.google.common.annotations.VisibleForTesting;
import protocol.SerializableCollection;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static protocol.ConnectionProtocol.GameBoardState;
import static protocol.ConnectionProtocol.PlayerBoardState;
import static util.Util.compareLists;

/**
 * Kalah is a basic implementation of {@see <a href="https://en.wikipedia.org/wiki/Kalah">Kalah</a>},
 * also called Kalaha or Mancala.
 * This class handles all of the movements and user turns. It will be used inside an {@link akka.actor.AbstractActor}.
 * As a result, State transition will be done locally without any need of synchronization.
 */
public class Kalah {
    private static final int STONES_PER_PIT = 6;
    private static final int PITS_NUMBER_PER_PLAYER = 6;

    private final String player1;
    private final String player2;

    private final Consumer<GameBoardState> onMoveCompleted;
    private final BiConsumer<String, Integer> onGameFinished;

    private Turn turn;
    private Map<String, Turn> stateCache = new HashMap<>(2);

    private List<Integer> board = new ArrayList<>(PITS_NUMBER_PER_PLAYER * 2 + 2);

    public Kalah(String player1,
                 String player2,
                 Consumer<GameBoardState> onMoveCompleted,
                 BiConsumer<String, Integer> onGameFinished) {

        this.player1 = player1;
        this.player2 = player2;
        this.onMoveCompleted = onMoveCompleted;
        this.onGameFinished = onGameFinished;

        stateCache.put(player1, new Player1Turn(player1));
        stateCache.put(player2, new Player2Turn(player2));

        initGameBoard();

        setTurn(stateCache.get(player1));
    }

    private void initGameBoard() {
        for (int i = 0; i < PITS_NUMBER_PER_PLAYER; i++)
            board.add(STONES_PER_PIT);

        board.add(0); //Player1 home

        for (int i = PITS_NUMBER_PER_PLAYER; i < PITS_NUMBER_PER_PLAYER * 2; i++)
            board.add(STONES_PER_PIT);

        board.add(0); //Player2 home
    }

    /**
     * This method checks if the game is finished.
     * The game is over as soon as one of the sides run out of stones.
     * @return Returns <code>true</code> if game is completed
     */
    private boolean isGameFinished() {
        return player1Pits().stream().allMatch(i -> i == 0) ||
                player2Pits().stream().allMatch(i -> i == 0);
    }

    /**
     * This method checks if the game is in a running state or initialization phase
     * @return Returns <code>true</code> if game is started
     */
    private boolean isGameStarted() {
        return !(player1Pits().stream().allMatch(i -> i == STONES_PER_PIT) &&
                player2Pits().stream().allMatch(i -> i == STONES_PER_PIT));
    }

    /**
     * This method exposes current game board state
     * @return Return game board status
     */
    public PlayerBoardState[] getState() {
        return new PlayerBoardState[]{
                new PlayerBoardState(player1,
                        new SerializableCollection<>(player1Pits()),
                        turn.getPlayer(),
                        board.get(PITS_NUMBER_PER_PLAYER)),

                new PlayerBoardState(player2,
                        new SerializableCollection<>(player2Pits()),
                        turn.getPlayer(),
                        board.get(PITS_NUMBER_PER_PLAYER * 2 + 1))
        };
    }

    private List<Integer> player1Pits() {
        return board.subList(0, PITS_NUMBER_PER_PLAYER);
    }

    private List<Integer> player2Pits() {
        return board.subList(PITS_NUMBER_PER_PLAYER + 1, PITS_NUMBER_PER_PLAYER * 2 + 1);
    }

    public void move(String byPlayer, int startPit) {
        turn.move(byPlayer, startPit);
    }

    private int normalize(String byPlayer, int startPit) {
        if (byPlayer.equals(player1))
            return startPit - 1;
        return startPit + STONES_PER_PIT;
    }

    public void compareAndMove(List<Integer> latestKnownState, String byPlayer, int startPit) {
        if (null == latestKnownState || !isGameStarted() || compareLists(latestKnownState, board))
            move(byPlayer, startPit);
    }

    private void setTurn(Turn turn) {
        this.turn = turn;
    }

    /**
     * Turn class handles movements and user turns.
     * It is a realization of State and Template Pattern.It controls player turns and movements.
     * <p>
     * Results of any movement and the Game completion event is emitted using Functional Interface callbacks.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private abstract class Turn {

        private final String player;

        public Turn(String player) {
            this.player = player;
        }

        /**
         * This method moves stones from a pit
         * @param byPlayer The user who ask to move stones
         * @param startPit The start pit number to move stones
         */
        void move(String byPlayer, int startPit) {
            if (!isGameFinished() &&
                    byPlayer.equals(turn.getPlayer())) {
                int normalizedIndex = normalize(byPlayer, startPit);
                if (isPitIndexInMyRange(normalizedIndex)) {
                    sowStones(normalizedIndex);
                    SerializableCollection<PlayerBoardState> states =
                            new SerializableCollection(Arrays.asList(getState()));

                    onMoveCompleted.accept(new GameBoardState(states));
                    if (isGameFinished()) {
                        int stonesInPlayer1House = board.get(stateCache.get(player1).getMyHouseIndex());
                        int stonesInPlayer2House = board.get(stateCache.get(player2).getMyHouseIndex());
                        onGameFinished.accept(stonesInPlayer1House > stonesInPlayer2House ? player1 : player2,
                                Math.max(stonesInPlayer1House, stonesInPlayer2House));
                    }
                }
            }
        }

        String getPlayer() {
            return player;
        }

        /**
         * This method selects the proper next pit to move stone
         * @param index The start pit number
         * @return Returns proper pit index to sow next stone
         */
        private int getNextIndex(int index) {
            index++;
            if (isOpponentHouse(index))
                index++;
            return index <= (PITS_NUMBER_PER_PLAYER * 2 + 1) ? index : 0;
        }

        /**
         * This methods manipulates the game board by sowing stones from a player specified pit.
         * @param startPit The pit index number which will be used to start sowing from
         */
        private void sowStones(int startPit) {
            int stones = board.get(startPit);

            int index = startPit;
            while (stones != 0) {
                stones--;
                index = getNextIndex(index);
                int value = board.get(index);
                board.set(index, value + 1);
                board.set(startPit, stones);
            }
            if (isPitIndexInMyRange(index) && board.get(index) == 1) {
                int oppositePintIndex = 2 * PITS_NUMBER_PER_PLAYER - index;
                int stonesInOppositePit = board.get(oppositePintIndex);
                board.set(index, 0);
                board.set(oppositePintIndex, 0);

                board.set(getMyHouseIndex(), board.get(getMyHouseIndex()) + stonesInOppositePit + 1);
            }

            if (!isMyHouse(index) && !isGameFinished()) {
                toggleTurn();
            }
        }

        private boolean isOpponentHouse(int pitIndex) {
            return pitIndex == getOpponentHouseIndex();
        }

        private boolean isMyHouse(int pitIndex) {
            return pitIndex == getMyHouseIndex();
        }

        protected abstract boolean isPitIndexInMyRange(int startPit);

        protected abstract int getMyHouseIndex();

        protected abstract int getOpponentHouseIndex();

        protected abstract void toggleTurn();
    }

    private class Player1Turn extends Turn {

        private int myHouseIndex = PITS_NUMBER_PER_PLAYER;
        private int opponentHouseIndex = PITS_NUMBER_PER_PLAYER * 2 + 1;

        Player1Turn(String player) {
            super(player);
        }

        @Override
        protected boolean isPitIndexInMyRange(int startPit) {
            return startPit >= 0 && startPit < STONES_PER_PIT;
        }

        @Override
        protected int getMyHouseIndex() {
            return myHouseIndex;
        }

        @Override
        protected int getOpponentHouseIndex() {
            return opponentHouseIndex;
        }

        @Override
        protected void toggleTurn() {
            setTurn(stateCache.get(player2));
        }
    }

    private class Player2Turn extends Turn {

        private int myHouseIndex = PITS_NUMBER_PER_PLAYER * 2 + 1;
        private int opponentHouseIndex = PITS_NUMBER_PER_PLAYER;

        Player2Turn(String player) {
            super(player);
        }

        @Override
        protected boolean isPitIndexInMyRange(int startPit) {
            return startPit >= STONES_PER_PIT + 1 && startPit <= STONES_PER_PIT * 2;
        }

        @Override
        protected int getMyHouseIndex() {
            return myHouseIndex;
        }

        @Override
        protected int getOpponentHouseIndex() {
            return opponentHouseIndex;
        }

        @Override
        protected void toggleTurn() {
            setTurn(stateCache.get(player1));
        }
    }

    @VisibleForTesting
    void mockState(final List<Integer> mockBoard) {
        this.board = mockBoard;
    }

}
