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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MainServer {
  static final int MAXGAMES = 256;
  // Be careful when using this, bytes in Java are signed.
  // Use Byte.toUnsignedInt(nbOfGames).
  static byte nbOfGames = 0x00;
  static GameServer[] gameServers = new GameServer[MAXGAMES];

  static class MaximumGameCapacityException extends Exception {}

  static class InvalidRequestException extends Exception {
    public InvalidRequestException(String string) {
      super(string);
    }
  }

  public static void main(String[] args) {
    Logger.log("Starting up server...\n");
    int port = Integer.parseInt(args[0]);

    if (System.getenv("VERBOSE") != null) {
      Logger.setVerbose(true);
    }
    Logger.verbose("Verbose activated\n");

    try {
      ServerSocket socket = new ServerSocket(port);
      Logger.verbose("Started on port %d\n", port);

      while (true) {
        acceptClient(socket);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void acceptClient(ServerSocket servsock) {
    try {
      Socket socket = servsock.accept();
      Logger.log("Accepting "+socket+"\n");
      ClientHandler ch = new ClientHandler(socket);
      ch.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static class ClientHandler extends Thread {
    Socket socket;

    public ClientHandler(Socket socket) {
      this.socket = socket;
    }

    public void run() {
      try {
        handleClient(socket);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void handleClient(Socket client) throws IOException {
    InputStream inStream = client.getInputStream();
    OutputStream outStream = client.getOutputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(outStream));
    Logger.log("Starting ClientHandler for "+client+"\n");
    GAMES welcome = new GAMES((byte)getCurrentAvailableGames().size());
    welcome.send(outStream);

    for (GameServer gs : getCurrentAvailableGames()) {
      OGAME game = new OGAME(gs);
      game.send(outStream);
    }

    parseMainMenuRequests(br, pw, inStream, outStream, client);

    client.close();
  }

  private static void parseMainMenuRequests(
      BufferedReader br, PrintWriter pw, InputStream is, OutputStream os, Socket client)
      throws IOException {
    byte currentLobby = 0;
    String currPlayerID = "";
    REGNO failed = new REGNO();
    DUNNO dunno = new DUNNO();

    // We'll read the first five characters into this, then handle the rest in
    // "MESSAGE" classes constructors, every clientMessage will take a BF

    while (true) {
      String request = "";
      try {
        for (int i = 0; i < 5; i++) {
          request += (char) (br.read());
        }
        System.out.println(request);
        switch (request) {
          case "UNREG":
            for (int i = 0; i < 3; i++) request += (char) (br.read());
            if (currentLobby == 0 && currPlayerID == "") dunno.send(os);
            else {
              if (gameServers[currentLobby].unregister(currPlayerID)) {
                UNROK unrok = new UNROK((byte) currentLobby);
                unrok.send(os);
                if (gameServers[currentLobby].getNbOfPlayers() == 0)
                  gameServers[currentLobby] = null;
                currentLobby = 0;
                currPlayerID = "";

              } else {
                dunno.send(os);
              }
            }
            break;
          case "SIZE?":
            SIZEQ sizeReq = SIZEQ.parse(br);
            byte gIDreq = sizeReq.getGameID();
            GameServer m = gameServers[Byte.toUnsignedInt(gIDreq)];
            if (m == null) dunno.send(os);
            else {
              SIZEA sizeAns = new SIZEA(m);
              sizeAns.send(os);
            }
            break;
          case "LIST?":
            LISTQ listQ = LISTQ.parse(br);
            int gID = Byte.toUnsignedInt(listQ.getGameID());

            if (gameServers[gID] == null) dunno.send(os);
            else {
              LISTA listA = new LISTA(gameServers[gID]);
              listA.send(os);
              ArrayList<Player> lobby = gameServers[gID].getLobby();

              for (Player p : lobby) {
                PLAYR pmsg = new PLAYR(p);
                pmsg.send(os);
                System.out.println(p.getPlayerID());
              }
            }
            break;
          case "NEWPL":
            if (Byte.toUnsignedInt(nbOfGames) >= MAXGAMES - 1) {
              throw new MaximumGameCapacityException();
            } else {
              NEWPL npl = NEWPL.parse(br);
              int id = createNewGame(npl, client);

              if (id == -1) failed.send(os);
              else {
                REGOK replyNewPl = new REGOK((byte) id);
                replyNewPl.send(os);
                currentLobby = (byte) id;
                currPlayerID = npl.getPlayerID();
              }
            }
            break;
          case "REGIS":
            REGIS regis = REGIS.parse(br);
            byte regGameID = regis.getGameID();
            System.out.println(regGameID);
            int regID = Byte.toUnsignedInt(regGameID);
            System.out.println(regID);

            if (gameServers[regID] == null) {
              throw new InvalidRequestException("Game " + regID + " does not exist.");
            }
            if (gameServers[regID].hasStarted()) {
              throw new InvalidRequestException(
                  "Game " + regID + " is ongoing. Can't join right now.");
            } else {
              if (gameServers[regID].joinGame(regis, client)) {
                currPlayerID = regis.getPlayerID();
                REGOK replyRegis = new REGOK((byte) (regis.getGameID()));
                replyRegis.send(os);
                currentLobby = (byte) regID;
                currPlayerID = regis.getPlayerID();
              } else {
                failed.send(os);
              }
            }
            break;
          case "GAME?":
            GAMEQ.parse(br);
            GAMEA gameA = new GAMEA(gameServers);
            gameA.send(os);

            for (GameServer gs : getCurrentAvailableGames()) {
              OGAME game = new OGAME(gs);
              game.send(os);
            }
            break;
          case "START":
            START.parse(br);
            if (currentLobby == 0 && currPlayerID == "")
              throw new InvalidRequestException(
                  "Bad request: " + request + ", player not yet properly registered in a game");
            else {
              MainServer.gameServers[currentLobby].addPlayerReady();
              MainServer.gameServers[currentLobby].startTheGameIfAllReady();
	      // wait the game out
              while (!MainServer.gameServers[currentLobby].isOver()) {}

              HashMap<Socket, Boolean> hs = MainServer.gameServers[currentLobby].getEndedPeacefully();
              //Close client connection
              Boolean ret = hs.get(client);
              if (ret != null && !ret) {
                return;  
              }
            }
          default:
            throw new InvalidRequestException("Bad request: " + request);
        }
      } catch (Exception e) {
        Logger.log("Received bad request " + request + " from: " + client.toString() + "\n");
        e.printStackTrace();
        failed.send(os);
        client.close();
        Logger.log("Drop kicked them into space.\n\n");
        return;
      }
    }
  }

  private static byte getAvailableGameID() {
    byte id = 0;

    for (int i = 0; i < MAXGAMES; i++) {
      if (gameServers[i] == null) return (id);
      id++;
    }

    return ((byte) -1);
  }

  private static int createNewGame(NEWPL npl, Socket client) {
    byte id = getAvailableGameID();
    int index = Byte.toUnsignedInt(id);

    // Creating new multicast address
    if (id != -1) {
      gameServers[index] = new GameServer(id, npl.getPort(), npl.getPlayerID(), client);
      nbOfGames++;
      return (id);
    }

    return (-1);
  }

  private static ArrayList<GameServer> getCurrentAvailableGames() {
    ArrayList<GameServer> games = new ArrayList<GameServer>();

    for (GameServer g : gameServers) {
      if (g != null) {
        if (!g.hasStarted()) games.add(g);
      }
    }

    return games;
  }
}
