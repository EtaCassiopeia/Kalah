# Sample WebSocket Requests

## connect
```sh
/connect ws://localhost:9000/ws/player/<PlayerName>
```

## disconnect
```sh
/disconnect
```

## Player List Request
```
{"type":"players-request"}
```

## Game request
```
{"type":"game-request", "opponent-player": "player-artin"}
```

## Want to play response
```
{"type":"want-to-play-response", "current-player" : "player-artin" , "opponentPlayer":"player-mohsen" , "response": "ACCEPT"}
```

## Move
```
{"type":"move", "player" : "player-mohsen" , "pit-index":"1" }
```
