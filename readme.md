Kalah (Mancala)
=================================

This is a simple web application to let players play an online Kalah game with each other.

## How to Setup and Play?  
From the project folder run bellow command to download all the required dependencies and run the project:
    ```
    ./activator run
    ```
After everything is done, open a browser and browse <a href="http://localhost:9000">http://localhost:9000</a>. You can use an another browser to represent 
a different user or use the <a href="https://github.com/cyberixae/dwst">Dark WebSocket Terminal</a> as a separate WebSocket client to send raw WebSocket commands.      
    
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
