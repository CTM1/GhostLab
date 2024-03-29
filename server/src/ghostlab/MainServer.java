package ghostlab;

import ghostlab.messages.clientmessages.menu.NEWPL;
import ghostlab.messages.servermessages.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MainServer {
  static final int MAXGAMES = 256;
  // Be careful when using this, bytes in Java are signed.
  // Use Byte.toUnsignedInt(nbOfGames).
  static Byte nbOfGames = 0x00;
  static GameServer[] gameServers = new GameServer[MAXGAMES];

  static class MaximumGameCapacityException extends Exception {}

  static class InvalidRequestException extends Exception {
    public InvalidRequestException(String string) {
      super(string);
    }
  }

  public static void main(String[] args) {
    Logger.log("[*] Starting up server...\n");
    int port = Integer.parseInt(args[0]);

    if (System.getenv("VERBOSE") != null) {
      Logger.setVerbose(true);
    }
    Logger.verbose("Verbose activated\n");

    try {
      ServerSocket socket = new ServerSocket(port);
      Logger.verbose("Started on port %d\n", port);

      while (true) {
        MainServer ms = new MainServer();
        ms.acceptClient(socket);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void acceptClient(ServerSocket servsock) {
    try {
      Socket socket = servsock.accept();
      Logger.log("[+] Accepting " + socket + "\n");
      ClientHandler ch = new ClientHandler(socket, this);
      ch.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public class ClientHandler extends Thread {
    public Socket socket;
    public MainServer ms;
    public String[] currPlayerID = {""};
    public Byte[] currentLobby = {0x00};
    public boolean shouldStop = false;

    public ClientHandler(Socket socket, MainServer ms) {
      this.socket = socket;
      this.ms = ms;
    }

    public void run() {
      try {
        handleClient(socket, ms);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void handleClient(Socket client, MainServer ms) throws IOException {
      InputStream inStream = client.getInputStream();
      OutputStream outStream = client.getOutputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(outStream));
      Logger.log("[*] Starting ClientHandler for " + client + "\n");

      GAMES welcome = new GAMES((byte) getCurrentAvailableGames().size());
      welcome.send(outStream);

      for (GameServer gs : getCurrentAvailableGames()) {
        OGAME game = new OGAME(gs);
        game.send(outStream);
      }

      parseMainMenuRequests(br, pw, inStream, outStream, client, ms);
    }

    private void parseMainMenuRequests(
        BufferedReader br,
        PrintWriter pw,
        InputStream is,
        OutputStream os,
        Socket client,
        MainServer ms)
        throws IOException {
      DUNNO dunno = new DUNNO();
      String[] messages = {"UNREG", "SIZEQ", "LISTQ", "NEWPL", "REGIS", "GAMEQ", "START"};
      String[] gameMessages = {
        "GLISQ", "RIMOV", "LEMOV", "UPMOV", "DOMOV", "MALLQ", "SENDQ", "IQUIT"
      };

      while (true) {
        if (shouldStop) break;
        String request = "";
        try {
          for (int i = 0; i < 5; i++) {
            request += (char) (br.read());
          }
          request = request.replace("?", "Q");

          if (Arrays.asList(gameMessages).contains(request)) {
            Logger.log("[!] Ignoring game message.\n");
            while (br.read() != '*')
              ;
            br.read();
            br.read();
            dunno.send(os);
            continue;
          }

          if (Arrays.asList(messages).contains(request)) {
            Class<?> c = Class.forName("ghostlab.messages.clientmessages.menu." + request);
            Method parse = c.getMethod("parse", BufferedReader.class);
            Method exec =
                c.getMethod(
                    "executeRequest",
                    BufferedReader.class,
                    OutputStream.class,
                    MainServer.ClientHandler.class);
            Method toString = c.getMethod("toString");

            Object reqObj = parse.invoke(null, br);

            String res = (String) toString.invoke(reqObj);
	    	    Logger.verbose("> (MS) (%s:%s) : %s\n", client.getInetAddress(), client.getPort(), res);

            exec.invoke(reqObj, br, os, this);
          } else {
            throw new InvalidRequestException(request);
          }

        } catch (Exception e) {
          Logger.log("[-] Received bad request " + request + " from: " + client.toString() + "\n");
          // e.printStackTrace();
          dunno.send(os);
          client.close();
          Logger.log("[*] %s was dropped.\n", client);
          return;
        }
      }
    }
  }

  private byte getAvailableGameID() {
    byte id = 0;

    for (int i = 0; i < MAXGAMES; i++) {
      if (gameServers[i] == null) return (id);
      id++;
    }

    return ((byte) -1);
  }

  public int createNewGame(NEWPL npl, Socket client) {
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

  public ArrayList<GameServer> getCurrentAvailableGames() {
    ArrayList<GameServer> games = new ArrayList<GameServer>();

    for (GameServer g : gameServers) {
      if (g != null) {
        if (!g.hasStarted()) games.add(g);
      }
    }

    return games;
  }

  public GameServer[] getGameServers() {
    return gameServers;
  }
}
