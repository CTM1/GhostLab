package ghostlab;

public class GameServer {
    byte id;
    Player[] players;
    Character[][] labyrinth;
    Ghost[] ghosts;

    public GameServer(String id, int port) {
        // Make ghosts players and shit
    }

    public void joinGame(String id) {
        // Make new player, drop into labyrinth
        // MAKE SURE THERE IS LESS THAN 254 PLAYERS OR DENY THE SHITS
        // (doing it in MainServer is better).
    }

    public byte getId() {
        return (this.id);
    }

    public byte getNbOfPlayers() {
        // if players > 254 this breaks the protocol.
        return ((byte) players.length);
    }
}