package ghostlab;

public class GameServer {
    byte id;
    int port;
    Player[] players;
    LabyrInterface labyrinth;
    Ghost[] ghosts;

    public GameServer(int id, int port) {
        // Make ghosts players and shit
        this.id = (byte)id;
        this.players = new Player[0];
        this.port = port;
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
