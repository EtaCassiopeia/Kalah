package protocol;

import com.fasterxml.jackson.annotation.*;

/**
 * ConnectionProtocol class contains message types used to communicating between WebSocket server and clients.
 */
public class ConnectionProtocol {

    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SerializableCollection.class, name = "collection"),
            @JsonSubTypes.Type(value = RequestPlayersList.class, name = "players-request"),
            @JsonSubTypes.Type(value = GameRequest.class, name = "game-request"),
            @JsonSubTypes.Type(value = AreYouWantToPlayWith.class, name = "want-to-play"),
            @JsonSubTypes.Type(value = AreYouWantToPlayWithResponse.class, name = "want-to-play-response"),
            @JsonSubTypes.Type(value = GameStarted.class, name = "game-started"),
            @JsonSubTypes.Type(value = Move.class, name = "move"),
            @JsonSubTypes.Type(value = PlayerBoardState.class, name = "player-board-state"),
            @JsonSubTypes.Type(value = GameBoardState.class, name = "game-board-state"),
            @JsonSubTypes.Type(value = GameFinished.class, name = "game-finished")
    })
    public static abstract class SerializableJsonObject {
    }

    public static class RequestPlayersList extends SerializableJsonObject {
        @JsonCreator
        public RequestPlayersList() {
        }
    }

    public static class GameRequest extends SerializableJsonObject {
        private final String opponentPlayer;

        @JsonCreator
        public GameRequest(@JsonProperty("opponent-player") String opponentPlayer) {
            this.opponentPlayer = opponentPlayer;
        }

        public String getOpponentPlayer() {
            return opponentPlayer;
        }
    }

    public static class AreYouWantToPlayWith extends SerializableJsonObject {
        private final String opponentPlayer;

        @JsonCreator
        public AreYouWantToPlayWith(@JsonProperty("opponent-player") String opponentPlayer) {
            this.opponentPlayer = opponentPlayer;
        }

        public String getOpponentPlayer() {
            return opponentPlayer;
        }
    }

    public enum Response {
        ACCEPT,
        DECLINE
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum Error {
        INVALID_PLAYER(101, "Invalid player name"),
        PLAYER_IS_BUSY(102, "Player is busy");

        private String message;
        private final int code;

        private Error(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static class AreYouWantToPlayWithResponse extends SerializableJsonObject {
        private final String currentPlayer;
        private final String opponentPlayer;
        private final Response response;

        @JsonCreator
        public AreYouWantToPlayWithResponse(@JsonProperty("current-player") String currentPlayer,
                                            @JsonProperty("opponent-player") String opponentPlayer,
                                            @JsonProperty("response") Response response) {
            this.currentPlayer = currentPlayer;
            this.opponentPlayer = opponentPlayer;
            this.response = response;
        }

        public String getCurrentPlayer() {
            return currentPlayer;
        }

        public String getOpponentPlayer() {
            return opponentPlayer;
        }

        public Response getResponse() {
            return response;
        }
    }

    public static class GameStarted extends SerializableJsonObject {
        private final String gameId;
        private final String player1;
        private final String player2;

        @JsonCreator
        public GameStarted(@JsonProperty("game-id") String gameId,
                           @JsonProperty("player1") String player1,
                           @JsonProperty("player2") String player2) {
            this.gameId = gameId;
            this.player1 = player1;
            this.player2 = player2;
        }

        public String getGameId() {
            return gameId;
        }

        public String getPlayer1() {
            return player1;
        }

        public String getPlayer2() {
            return player2;
        }
    }

    public static class Move extends SerializableJsonObject {
        private final String player;
        private final Integer startPitIndex;

        public Move(@JsonProperty("player") String player, @JsonProperty("pit-index") Integer startPitIndex) {
            this.player = player;
            this.startPitIndex = startPitIndex;
        }

        public String getPlayer() {
            return player;
        }

        public Integer getStartPitIndex() {
            return startPitIndex;
        }
    }

    public static class GameBoardState extends SerializableJsonObject {
        private final SerializableCollection<PlayerBoardState> playerBoards;

        public GameBoardState(@JsonProperty("states") SerializableCollection<PlayerBoardState> playerBoards) {
            this.playerBoards = playerBoards;
        }

        public SerializableCollection<PlayerBoardState> getPlayerBoards() {
            return playerBoards;
        }
    }

    public static class PlayerBoardState extends SerializableJsonObject {
        private final String player;
        private final SerializableCollection<Integer> pits;
        private final Integer stonesInHome;
        private final String whoseTurn;

        public PlayerBoardState(@JsonProperty("player") String player,
                                @JsonProperty("pits") SerializableCollection<Integer> pits,
                                @JsonProperty("whose-turn") String whoseTurn,
                                @JsonProperty("stones-in-home") Integer stonesInHome) {

            this.player = player;
            this.pits = pits;
            this.whoseTurn = whoseTurn;
            this.stonesInHome = stonesInHome;
        }

        public String getPlayer() {
            return player;
        }

        public SerializableCollection<Integer> getPits() {
            return pits;
        }

        public String getWhoseTurn() {
            return whoseTurn;
        }

        public Integer getStonesInHome() {
            return stonesInHome;
        }

        @Override
        public String toString() {
            return "PlayerBoardState{" +
                    "player='" + player + '\'' +
                    ", pits=" + pits +
                    ", stonesInHome=" + stonesInHome +
                    ", whoseTurn='" + whoseTurn + '\'' +
                    '}';
        }
    }

    public static class GameFinished extends SerializableJsonObject {
        private String winner;
        private Integer stones;

        public GameFinished(@JsonProperty("winner") String winner, @JsonProperty("stones") Integer stones) {
            this.winner = winner;
            this.stones = stones;
        }

        public String getWinner() {
            return winner;
        }

        public Integer getStones() {
            return stones;
        }
    }

    public static final class Timeout {
    }

}

