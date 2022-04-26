package ghostlab;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import ghostlab.messages.clientmessages.*;
import ghostlab.messages.servermessages.*;

public class GameServer {
    private byte id;
    private Socket hostTCPSocket;
    private String hostUDPport;
    private ArrayList<Player> lobby;
    private Ghost[] ghosts;
    private LabyrInterface labyrinth;
    private boolean started = false;
    private boolean over = false;
    // private HashMap<String, Integer> playerIndexes;
    static final int MAXPLAYERS = 256;

    public GameServer(byte id, String hostUDPport, String hostID, Socket hostTCPSocket) {
        this.id = id;
        this.lobby = new ArrayList<Player>();
        this.hostUDPport = hostUDPport;
        // this.playerIndexes = new HashMap<String, Integer>();
        try {
            this.lobby.add(new Player(0, hostID, hostUDPport, hostTCPSocket));
        }
        catch (SocketException e) {
            System.out.println("Failed to register " + hostID + 
                                        " to game " + id + " at index 0");
        }
        // this.playerIndexes.put(hostID, 0);
        
        this.labyrinth = new Labyrinth(120, 120);
        this.hostTCPSocket = hostTCPSocket;
    }

    public boolean joinGame(REGIS regis, Socket TCPSocket) {
        
        // for (int i = 0; i < MAXPLAYERS; i++) {
        //     if (lobby[i] == null) {
        //         try {
        //             lobby[i] = new Player(i, regis.getPlayerID(), regis.getPort(), TCPSocket);
        //         } catch (SocketException e) {
        //             System.out.println("Failed to register " + regis.getPlayerID() + 
        //                                 " to game " + this.id + " at index " + i);
        //         }
                
        //         this.playerIndexes.put(regis.getPlayerID(), i);
        //         this.nbPlayers++;
        //         return true;
        //     }
        // }

        if (this.lobby.size() <= 256) {
            try {
                lobby.add(new Player(this.lobby.size(), regis.getPlayerID(), regis.getPort(), TCPSocket));
            } catch (SocketException e) {
                System.out.println("Failed to register " + regis.getPlayerID() + 
                                    " to game " + this.id + " at index " + this.lobby.size());
            }
            return true;
        }

        return false;
    }

    public boolean unregister(String playerID) {
        // int index;

        // if (this.playerIndexes.get(playerID) == null) {
        //     return false;
        // }

        // index = this.playerIndexes.get(playerID);
        // lobby[index] = null;
        // this.playerIndexes.remove(playerID);

        for(Player p : lobby) {
            if (p.getPlayerID().equals(playerID)) {
                lobby.remove(p);
                return true;
            }
        }
        return false;
    }

    public byte getId() {
        return (this.id);
    }

    public byte getNbOfPlayers() {
        return ((byte) this.lobby.size());
    }

    public ArrayList<Player> getLobby() {
        return (this.lobby);
    }

    public LabyrInterface getLabyrinth() {
        return (this.labyrinth);
    }

    public boolean hasStarted() {
        return (this.started);
    }
    
    public boolean isOver() {
        return (this.over);
    }
}
