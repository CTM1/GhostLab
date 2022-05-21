package ghostlab;

import ghostlab.messages.clientmessages.menu.REGIS;
import ghostlab.messages.clientmessages.menu.START;
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
  private ArrayList<START> playersReady;
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
    this.playersReady = new ArrayList<START>();
    this.handlers = new HashMap<Player, PlayerHandler>();
    this.endedPeacefully = new HashMap<Socket, Boolean>();

    this.endedPeacefully.put(hostTCPSocket, true);

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
      Logger.log("[-] Couldn't get new multicast address for game %d\n", id);
      e.printStackTrace();
    }

    try {
      this.lobby.add(new Player(0, hostID, hostUDPport, hostTCPSocket));
    } catch (SocketException e) {
      Logger.log("[-] Failed to register " + hostID + " to game " + id + "\n");
    }

    this.labyrinth = new RecursiveMaze(20, 20);
    this.ghosts = new ArrayList<Ghost>();
    Logger.log(
        "[*] Started new game server %d, multicast on %s:%d\n",
        id, this.hostMulticastAddress.toString(), udpPort);
  }

  public static class PlayerHandler extends Thread {
    Player player;
    GameServer parentgs;
    InputStream inStream;
    OutputStream outStream;
    BufferedReader br;
    public boolean shouldQuit = false;

    public PlayerHandler(Player p, GameServer parentgs) {
      player = p;
      this.parentgs = parentgs;
      try {
        inStream = player.TCPSocket.getInputStream();
        outStream = player.TCPSocket.getOutputStream();
        br = new BufferedReader(new InputStreamReader(inStream));
      } catch (Exception e) {
        Logger.log("[-] Error getting streams from socket\n");
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
      String[] gameMessages = {
        "GLISQ", "RIMOV", "LEMOV", "UPMOV", "DOMOV", "MALLQ", "SENDQ", "IQUIT"
      };

      while (!parentgs.isOver()) {
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

          if (Arrays.asList(gameMessages).contains(request)) {
            Class<?> c = Class.forName("ghostlab.messages.clientmessages.game." + request);
            Method parse = c.getMethod("parse", BufferedReader.class);
            Method exec =
                c.getMethod(
                    "executeRequest",
                    GameServer.PlayerHandler.class,
                    GameServer.class,
                    Player.class,
                    OutputStream.class);
            Method toString = c.getMethod("toString");

            Object reqObj = parse.invoke(null, br);
            String res = (String) toString.invoke(reqObj);
	    	    Logger.verbose("> (GS) (%s) : %s\n", player.name, res);
            exec.invoke(reqObj, this, parentgs, player, outStream);
            if (shouldQuit) break;
          } else {
            outStream.write("GOBYE!***".getBytes());
            outStream.flush();
            parentgs.endedPeacefully.put(player.getTCPSocket(), false);
            return;
          }

        } catch (Exception e) {
          Logger.log(
              "[-] %d : Invalid message from player %s\n", parentgs.getGameId(), player.getPlayerID());
          // e.printStackTrace();
          return;
        }
      }
    }

    public synchronized void testMoveAndSendBackMOVEF(int direction, int distance) {
      ArrayList<Ghost> realMFGs = parentgs.getGhosts();
      boolean[][] maze = parentgs.labyrinth.getSurface();

      int moved = 0; // distance traveled
      int[] position = new int[] {player.getX(), player.getY()};
      boolean metAGhost = false;

      while (!metAGhost && moved < distance) {
        switch (direction) {
          case 0: //  UP
            position[0]--;
            break;
          case 1: // DOWN
            position[0]++;
            break;
          case 2: // LEFT
            position[1]--;
            break;
          case 3: // RIGHT
            position[1]++;
            break;
        }

        if (!maze[position[0]][position[1]]) player.setPos(position[0], position[1]);
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
                parentgs.lobby, (p1, p2) -> ((Integer) p2.getScore()).compareTo(p1.getScore()));

            // update emit score
            player.addToScore(1);
            parentgs.multicast.SCORE(
                player.getPlayerID(), player.getScore(), position[0], position[1]);

            // new position
            try {
              (new MOVEF(player)).send(outStream);
            } catch (Exception e) {
              Logger.log("[-] Couldn't send message !\n");
              e.printStackTrace();
            }

            // remove ghost
            Logger.log("[!] Caught a ghost!\n");
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
          MOVED m = new MOVED(player);
          // Logger.verbose("> %s : %s\n", player, m);
          m.send(outStream);
        } catch (Exception e) {
          Logger.log("[-] Couldn't send message !\n");
          e.printStackTrace();
        }
      }
    }

    public OutputStream getOutputStream() {
      return outStream;
    }
  }

  /** The main game loop, takes care of score, moves the ghosts around, check collision, etc */
  public synchronized void gameLoop() {
    long lastGhostMove = System.currentTimeMillis();
    long timeNow;
    int[] epl;
    while (ghosts.size() > 0 && lobby.size() > 0) {

      try {
        timeNow = System.currentTimeMillis();
        if (timeNow - lastGhostMove > 5000) {
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
    this.over = true;

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

    try {
      Thread.sleep(2500);
    } catch (InterruptedException e1) {
      Logger.log("[-] Thread sleeping failed\n");
    }

    for (Player p : lobby) {
      try {
        p.TCPSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // notifyAll();
    try {
      MainServer.gameServers[this.id] = null;
    } catch (Exception e) {}
  }

  public void startGame() {
    int[] emplacement;
    for (int i = 0; i < lobby.size() * 3; i++) {
      emplacement = labyrinth.emptyPlace();
      Ghost g = new Ghost(emplacement[0], emplacement[1]);
      ghosts.add(g);
    }

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
        e.printStackTrace();
      }
    }

    for (Player p : lobby) {
      emplacement = labyrinth.emptyPlace();
      p.setPos(emplacement[0], emplacement[1]);
      try {
        (new POSIT(p.getPlayerID(), emplacement[0], emplacement[1]))
            .send(handlers.get(p).getOutputStream());
      } catch (Exception e) {
        Logger.log("[-] Error sending POSIT\n");
      }
    }

    System.out.println(labyrinth);
    gameLoop();
  }

  public class GameStarter extends Thread {
    @Override
    public void run() {
      startGame();
    }
  }

  public void startTheGameIfAllReady() {
    if (playersReady.size() == lobby.size()) {
      new GameStarter().start();
    }
  }

  public boolean sendMessage(String from, String to, String content) {
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
      } catch (SocketException e) {
        Logger.log(
            "[-] Failed to register "
                + regis.getPlayerID()
                + " to game "
                + this.id
                + " at index "
                + this.lobby.size() + "\n");
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

  public void addPlayerReady(START s) {
    playersReady.add(s);
  }

  public MulticastGameServer getMulticast() {
    return multicast;
  }
}
