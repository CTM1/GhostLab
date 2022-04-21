package ghostlab;

import java.net.InetSocketAddress;
import ghostlab.messages.clientmessages.*;
import ghostlab.messages.servermessages.*;

public class GameServer {
    private byte id;
    private String hostUDPport;
    private Player[] lobby;
    private LabyrInterface labyrinth;
    private Ghost[] ghosts;
    private boolean started = false;
    private boolean over = false;

    public GameServer(int id, String hostUDPport, String hostID, InetSocketAddress hostAddr) {
        this.id = (byte)id;
        this.lobby = new Player[0xFF];
        this.hostUDPport = hostUDPport;
        this.lobby[0] = new Player(hostID, hostUDPport, hostAddr);
        this.labyrinth = new Labyrinth(120, 120);
    }

    public void joinGame(REGIS regis, InetSocketAddress playerAddr) {
    }

    public byte getId() {
        return (this.id);
    }

    public byte getNbOfPlayers() {
        // if players > 254 this breaks the protocol.
        return ((byte) lobby.length);
    }

    public Player[] getLobby() {
        return (this.lobby);
    }

    public boolean hasStarted() {
        return (this.started);
    }
}
