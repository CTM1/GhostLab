package ghostlab;

import ghostlab.messages.clientmessages.*;
import ghostlab.messages.servermessages.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class GameServer {
  private byte id;
  private Socket hostTCPSocket;
  private InetAddress hostMulticastAddress;
  private String hostUDPport;
  private ArrayList<Player> lobby;
  private ArrayList<Player> playersReady;
  private ArrayList<Ghost> ghosts;
  private HashMap<Player, PlayerHandler> handlers;
  private LabyrInterface labyrinth;
  private boolean started = false;
  private boolean over = false;
  private MulticastGameServer multicast;
  static final int MAXPLAYERS = 256;

  public GameServer(byte id, String hostUDPport, String hostID, Socket hostTCPSocket) {
    this.id = id;
    this.lobby = new ArrayList<Player>();
    this.playersReady = new ArrayList<Player>();
    this.handlers = new HashMap<Player, PlayerHandler>();

    String newIP = String.format("224.255.0.%d", id);
    try {
      this.hostMulticastAddress = InetAddress.getByName(newIP);
      this.multicast = new MulticastGameServer(this.hostMulticastAddress, hostUDPport);

    } catch (Exception e) {
      Logger.log("Couldn't get new multicast address for game %d", id);
      e.printStackTrace();
    }

    this.hostUDPport = hostUDPport;
    try {
      this.lobby.add(new Player(0, hostID, hostUDPport, hostTCPSocket));
    } catch (SocketException e) {
      System.out.println("Failed to register " + hostID + " to game " + id + " at index 0");
    }

    this.labyrinth = new Maze(120, 120);
    this.hostTCPSocket = hostTCPSocket;
    Logger.verbose(
        "Started new game server %d, multicast on %s:%s\n",
        id, this.hostMulticastAddress.toString(), hostUDPport);
  }

  private class PlayerHandler extends Thread {
    Player playa;
    GameServer daddy;
    InputStream inStream;
    OutputStream outStream;
    BufferedReader br;
    PrintWriter pw;

    public boolean con; // tinue

    public PlayerHandler(Player p, GameServer daddy) {
      playa = p;
      this.daddy = daddy;
      con = true;
      try {
        inStream = playa.TCPSocket.getInputStream();
        outStream = playa.TCPSocket.getOutputStream();
        br = new BufferedReader(new InputStreamReader(inStream));
        pw = new PrintWriter(new OutputStreamWriter(outStream));
      } catch (Exception e) {
        Logger.log("Whoopsy");
      }
    }

    public void run() {
      try {
        handleRequests();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void handleRequests() throws IOException {
      while (con) {
        String request = "";
        try {
          for (int i = 0; i < 5; i++) {
            request += (char) (br.read());
          }
          switch (request) {
            case "START":
              daddy.playersReady.add(playa);
              if (daddy.playersReady.size() == daddy.lobby.size()) {
                daddy.startTheGame();
              }
              break;
            case "UPMOV": // TODO
              break;
            case "DOMOV": // TODO
              break;
            case "LEMOV": // TODO
              break;
            case "RIMOV": // TODO
              break;
            case "SEND?": // TODO
              break;
            case "GLIS?": // TODO
              break;
            case "MALL?": // TODO
              break;
            default:
              // TODO Gobye
              break;
          }
        } catch (Exception e) {
          Logger.log("%d : Invalid message from player %s", daddy.getGameId(), playa.getPlayerID());
          e.printStackTrace();
        }
      }
    }

    public OutputStream getOutputStream() {
      return outStream;
    }
  }

  public void startTheGame() {
    started = true;
    WELCO w =
        new WELCO(
            this.id,
            labyrinth.getHeight(),
            labyrinth.getWidth(),
            ghosts.size(),
            hostMulticastAddress.toString(),
            hostUDPport);

    for (Player p : lobby) {
      try {
        w.send(handlers.get(p).getOutputStream());
      } catch (Exception e) {
        Logger.verbose("fuk u");
      }
    }
    // placer joueur
    // envoyer POSIT
  }

  public boolean joinGame(REGIS regis, Socket TCPSocket) {
    if (this.lobby.size() <= 256 && !started) {
      try {
        Player p = new Player(this.lobby.size(), regis.getPlayerID(), regis.getPort(), TCPSocket);
        lobby.add(p);
        PlayerHandler hl = new PlayerHandler(p, this);
        handlers.put(p, hl);
        hl.start();
      } catch (SocketException e) {
        System.out.println(
            "Failed to register "
                + regis.getPlayerID()
                + " to game "
                + this.id
                + " at index "
                + this.lobby.size());
      }
      return true;
    }

    return false;
  }

  public boolean unregister(String playerID) {
    for (Player p : lobby) {
      if (p.getPlayerID().equals(playerID)) {
        lobby.remove(p);
        playersReady.remove(p);
        handlers.get(p).con = false; // stop handler to avoid memleaks
        handlers.remove(p);
        return true;
      }
    }
    return false;
  }

  public byte getGameId() {
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
