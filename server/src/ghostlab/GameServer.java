package ghostlab;

import java.net.InetSocketAddress;

public class GameServer {
    byte id;
    String UDPport;
    Player[] players;
    LabyrInterface labyrinth;
    Ghost[] ghosts;
    boolean started = false;

    public GameServer(int id, String UDPport, String hostID, InetSocketAddress hostAddr) {
        this.id = (byte)id;
        this.players = new Player[0xFF];
        this.UDPport = UDPport;
        this.players[0] = new Player(hostID, UDPport, hostAddr);
        this.labyrinth = new Labyrinth(120, 120);

    }

    public void joinGame(String id) {
    }

    public byte getId() {
        return (this.id);
    }

    public byte getNbOfPlayers() {
        // if players > 254 this breaks the protocol.
        return ((byte) players.length);
    }
}
