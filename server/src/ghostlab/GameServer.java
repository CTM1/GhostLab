package ghostlab;

import ghostlab.messages.clientmessages.menu.REGIS;
import ghostlab.messages.servermessages.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer {
  private byte id;
  private InetAddress hostMulticastAddress;
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

    this.endedPeacefully.put(hostTCPSocket, true);
    Logger.log(endedPeacefully + "\n");

    String newIP = String.format("224.255.0.%d", id);
    int udpPort = -1;
    try {
      this.hostMulticastAddress = InetAddress.getByName(newIP);

      // find port
      boolean foundPort = false;
      
      do {
        udpPort = ThreadLocalRandom.current().nextInt(1000, 10000);
        foundPort = true;

        for (Player p : lobby) {
          if (p.getUDPport() == udpPort) foundPort = false;
        }

      } while (!foundPort);

      this.multicast = new MulticastGameServer(this.hostMulticastAddress, udpPort);

    } catch (Exception e) {
      Logger.log("Couldn't get new multicast address for game %d", id);
      e.printStackTrace();
    }

    try {
      this.lobby.add(new Player(0, hostID, hostUDPport, hostTCPSocket));
    } catch (SocketException e) {
      System.out.println("Failed to register " + hostID + " to game " + id + " at index 0");
    }

    this.labyrinth = new RecursiveMaze(20, 20);
    this.ghosts = new ArrayList<Ghost>();
    Logger.verbose(
        "Started new game server %d, multicast on %s:%d\n",
        id, this.hostMulticastAddress.toString(), udpPort);
  }

  public static class PlayerHandler extends Thread {
    Player playa;
    GameServer daddy;
    InputStream inStream;
    OutputStream outStream;
    BufferedReader br;
    // PrintWriter pw;


    public PlayerHandler(Player p, GameServer daddy) {
      playa = p;
      this.daddy = daddy;
      try {
        inStream = playa.TCPSocket.getInputStream();
        outStream = playa.TCPSocket.getOutputStream();
        br = new BufferedReader(new InputStreamReader(inStream));
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
      String[] gameMessages = { "GLISQ", "RIMOV", "LEMOV", "UPMOV", "DOMOV", "MALLQ", "SENDQ", "IQUIT"};

      while (!daddy.isOver()) {
        String request = "";
        try {
          for (int i = 0; i < 5; i++) {
            try {
              request += (char) (br.read());
            } catch (SocketException e) {
              break;
            }
          }

          request = request.replace("?", "Q");
          Logger.log("Received "+request+"\n");

          if (Arrays.asList(gameMessages).contains(request)) {
            Class<?> c = Class.forName("ghostlab.messages.clientmessages.game." + request);
						Method parse = c.getMethod("parse", BufferedReader.class);
						Method exec = c.getMethod("executeRequest", GameServer.PlayerHandler.class, GameServer.class,
                    Player.class, OutputStream.class);

						Object reqObj = parse.invoke(null, br);
						exec.invoke(reqObj, this, daddy, playa, outStream);
          } else {
            outStream.write("GOBYE!***".getBytes());
            outStream.flush();
            daddy.endedPeacefully.put(playa.getTCPSocket(), false);
            return;
          }

        } catch (Exception e) {
          Logger.log("%d : Invalid message from player %s", daddy.getGameId(), playa.getPlayerID());
          e.printStackTrace();
          return;
        }
      }
    }

    public synchronized void testMoveAndSendBackMOVEF(int direction, int distance) {
      ArrayList<Ghost> realMFGs = daddy.getGhosts();
      boolean[][] maze = daddy.labyrinth.getSurface();

      int moved = 0; // distance traveled
      int[] position = new int[] {playa.getX(), playa.getY()};
      boolean metAGhost = false;

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

        if (!maze[position[1]][position[0]]) playa.setPos(position[0], position[1]);
        else break;

        moved++;
        // check for ghosts
        ArrayList<Ghost> toRemove = new ArrayList<Ghost>();
        for (Ghost g : realMFGs) {
          if (position[0] == g.getX() && position[1] == g.getY()) {
            // break the move
            metAGhost = true;

            // update the lobby order to be highest first
            Collections.sort(
                daddy.lobby, (p1, p2) -> ((Integer) p2.getScore()).compareTo(p1.getScore()));

            // update emit score
            playa.addToScore(1);
            daddy.multicast.SCORE(playa.getPlayerID(), playa.getScore(), position[0], position[1]);

            // new position
            try {
              (new MOVEF(playa)).send(outStream);
            } catch (Exception e) {
              Logger.log("Couldn't send message !");
              e.printStackTrace();
            }

            // remove ghost
            Logger.log("Caught a ghost!"+"\n");
            toRemove.add(g);
          }
        }
        for (Ghost g : toRemove) {
          realMFGs.remove(g);
        }
      }

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
              Integer.toString(multicast.getPort()));

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

      try {
        timeNow = System.currentTimeMillis();
        if (timeNow - lastGhostMove > 3 * 1000) {
          lastGhostMove = timeNow;
          epl = labyrinth.emptyPlace();
          int index = (int) (Math.random() * ghosts.size());
          ghosts.get(index).moveGhost(epl[0], epl[1]);
          multicast.GHOST(epl[0], epl[1]);
        }
      } catch (IndexOutOfBoundsException e) {
        this.over = true;
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
    this.over = true;
  }

  public synchronized boolean sendMessage(String from, String to, String content) {
    for (Player p : lobby) {
      if (p.getPlayerID().equals(to)) {
        p.propagateMessage(from, content);
        return true;
      }
    }

    return false; // OK if we found the player in the lobby
  }

  public boolean joinGame(REGIS regis, Socket TCPSocket) {
    if (this.lobby.size() <= 256 && !started) {
      try {
        Player p = new Player(this.lobby.size(), regis.getPlayerID(), regis.getPort(), TCPSocket);
        lobby.add(p);
        this.endedPeacefully.put(p.getTCPSocket(), true);
        Logger.log("AAA "+endedPeacefully+"\n");
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

  public MulticastGameServer getMulticast() {
    return multicast;
  }
}
