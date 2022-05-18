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
import java.util.Collections;
import java.util.HashMap;

public class GameServer {
  private byte id;
  private Socket hostTCPSocket;
  private InetAddress hostMulticastAddress;
  private String hostUDPport;
  private ArrayList<Player> lobby;
  private ArrayList<Character> playersReady;
  private ArrayList<Ghost> ghosts;
  private HashMap<Player, PlayerHandler> handlers;
  private HashMap<Socket, Boolean> endedPeacefully;
  private LabyrInterface labyrinth;
  private boolean started = false;
  private boolean over = false;
  private MulticastGameServer multicast;
  static final int MAXPLAYERS = 256;

  public GameServer(byte id, String hostUDPport, String hostID, Socket hostTCPSocket) {
    this.id = id;
    this.lobby = new ArrayList<Player>();
    this.playersReady = new ArrayList<Character>();
    this.handlers = new HashMap<Player, PlayerHandler>();
    this.endedPeacefully = new HashMap<Socket, Boolean>();

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

    this.labyrinth = new RecursiveMaze(20, 20);
    this.hostTCPSocket = hostTCPSocket;
    this.ghosts = new ArrayList<Ghost>();
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
      int direction;
      int distance;
      while (con) {
        String request = "";
        try {
          for (int i = 0; i < 5; i++) {
            request += (char) (br.read());
          }

          switch (request) {
            case "UPMOV":
              direction = 0;
              distance = MovementMessage.parseDistance(br);
              MovementMessage.getMsgTail(br);
              testMoveAndSendBackMOVEF(direction, distance);
              break;

            case "DOMOV":
              direction = 1;
              distance = MovementMessage.parseDistance(br);
              MovementMessage.getMsgTail(br);
              testMoveAndSendBackMOVEF(direction, distance);
              break;

            case "LEMOV":
              direction = 2;
              distance = MovementMessage.parseDistance(br);
              MovementMessage.getMsgTail(br);
              testMoveAndSendBackMOVEF(direction, distance);
              break;

            case "RIMOV":
              direction = 3;
              distance = MovementMessage.parseDistance(br);
              MovementMessage.getMsgTail(br);
              testMoveAndSendBackMOVEF(direction, distance);
              break;

            case "SEND?": // TODO
              break;
            case "GLIS?":
              for (int i = 0; i < 3; i++) br.read(); // read end of message ***

              (new GLIS(daddy.lobby.size())).send(outStream);

              // send GPLYR
              for (Player p : daddy.lobby) {
                (new GPLYR(p)).send(outStream);
              }

              break;
            case "MALL?": // TODO
              br.read(); // espace
              char[] buff = new char[200];
              int read = 0;
              while (buff[read] != '*' && read < 200) {
                buff[read] = (char) br.read();
                read++;
              }

              daddy.multicast.MESSA(playa.getPlayerID(), new String(buff));

              outStream.write("MALL!".getBytes());
              outStream.flush();
              break;
            default:
              outStream.write("GOBYE!".getBytes());
              outStream.flush();
              endedPeacefully.put(playa.getTCPSocket(), false);
              return;
          }
        } catch (Exception e) {
          Logger.log("%d : Invalid message from player %s", daddy.getGameId(), playa.getPlayerID());
          e.printStackTrace();
        }
      }
    }

    private void testMoveAndSendBackMOVEF(int direction, int distance) {
      ArrayList<Ghost> realMFGs = daddy.getGhosts();
      boolean[][] maze = daddy.labyrinth.getSurface();

      int moved = 0; // distance traveled
      int[] position = new int[] {playa.getX(), playa.getY()};
      boolean metAGhost = false;

      // System.out.println(String.format("Moving from (%d, %d), dist %d", position[0], position[1], distance));

      while (!metAGhost && moved < distance) {
        switch (direction) {
          case 0:
            position[1]--;
            break;
          case 1:
            position[1]++;
            break;
          case 2:
            position[0]--;
            break;
          case 3:
            position[0]++;
            break;
        }

        if (!maze[position[1]][position[0]])
          playa.setPos(position[0], position[1]);
        else
          break;

        moved++;
        // check for ghosts
        for (Ghost g : realMFGs) {
          if (position[0] == g.getX() && position[1] == g.getY()) {
            // break the move
            metAGhost = true;

            // update the lobby order to be highest first
            Collections.sort(
                daddy.lobby, (p1, p2) -> ((Integer) p1.getScore()).compareTo(p2.getScore()));

            // update emit score
            playa.addToScore(1);
            multicast.SCORE(playa.getPlayerID(), playa.getScore(), position[0], position[1]);

            // new position
            try {
              (new MOVEF(playa)).send(outStream);
            } catch (Exception e) {
              Logger.log("Couldn't send message !");
              e.printStackTrace();
            }

            // remove ghost
            realMFGs.remove(g);
          }
        }
      }

      // System.out.println(String.format("New pos, (%d, %d), travelled %d", position[0], position[1], moved));
      // Send new position
      if (!metAGhost) {
        try {
          (new MOVED(playa)).send(outStream);
        } catch (Exception e) {
          Logger.log("Couldn't send message !");
          e.printStackTrace();
        }
      }
    }

    public OutputStream getOutputStream() {
      return outStream;
    }
  }

  public void startTheGameIfAllReady() {
    if (playersReady.size() == lobby.size()) {
      started = true;
      WELCO w =
          new WELCO(
              this.id,
              labyrinth.getHeight(),
              labyrinth.getWidth(),
              (byte) ghosts.size(),
              hostMulticastAddress.toString(),
              hostUDPport);

      for (Player p : lobby) {
        try {
          PlayerHandler hl = new PlayerHandler(p, this);
          handlers.put(p, hl);
          hl.start();

          w.send(handlers.get(p).getOutputStream());
        } catch (Exception e) {
          Logger.verbose("fuk u");
        }
      }

      int[] emplacement;
      for (int i = 0; i < lobby.size() * 2; i++) {
        emplacement = labyrinth.emptyPlace();
        Ghost g = new Ghost(emplacement[0], emplacement[1]);
        ghosts.add(g);
      }

      for (Player p : lobby) {
        emplacement = labyrinth.emptyPlace();
        p.setPos(emplacement[0], emplacement[1]);
        try {
          (new POSIT(p.getPlayerID(), emplacement[0], emplacement[1]))
              .send(handlers.get(p).getOutputStream());
        } catch (Exception e) {
          Logger.log("Nop");
        }
      }

      System.out.println(labyrinth);
      gameLoop();
    }
  }

  /** The main game loop, takes care of score, moves the ghosts around, check collision, etc */
  public void gameLoop() {
    long lastGhostMove = System.currentTimeMillis();
    long timeNow;
    int[] epl;
    while (ghosts.size() > 0) {
      // TODO the loop
      // TODO THREAD SAFETY !!!!!
      // lock displacements
      // Collision detection between ghosts and players
      // this is now done at move time
      // for (Ghost g : ghosts) {
      //  for (Player p : lobby) {
      //    if (p.getX() == g.getX() && p.getY() == g.getY()) {
      //      p.addToScore(1);
      //      ghosts.remove(g);
      //      multicast.SCORE(p.getPlayerID(), p.getScore(), p.getX(), p.getY());
      //    }
      //  }
      // }

      // move a ghost around every 3 second
      timeNow = System.currentTimeMillis();
      if (timeNow - lastGhostMove > 3 * 1000) {
        lastGhostMove = timeNow;
        epl = labyrinth.emptyPlace();
        int index = (int) (Math.random() * ghosts.size());
        ghosts.get(index).moveGhost(epl[0], epl[1]);
        multicast.GHOST(epl[0], epl[1]);
      }
    }

    // find the winner
    String id = "";
    int maxScore = 0;
    for (Player p : lobby) {
      if (p.getScore() > maxScore) {
        id = p.getPlayerID();
        maxScore = p.getScore();
      }
    }
    multicast.ENDGA(id, maxScore);
  }

  public boolean joinGame(REGIS regis, Socket TCPSocket) {
    if (this.lobby.size() <= 256 && !started) {
      try {
        Player p = new Player(this.lobby.size(), regis.getPlayerID(), regis.getPort(), TCPSocket);
        lobby.add(p);
        this.endedPeacefully.put(p.getTCPSocket(), true);
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
        // TODO : Remove player handler only when game has already started (so on IQUIT)
        // playersReady.remove(0);
        // handlers.get(playerID).con = false; // stop handler to avoid memleaks
        // handlers.remove(p);
        return true;
      }
    }
    return false;
  }

  public HashMap<Socket, Boolean> getEndedPeacefully() {
    return this.endedPeacefully;
  }

  public ArrayList<Ghost> getGhosts() {
    return ghosts;
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

  public void addPlayerReady() {
    playersReady.add('a');
  }
}
