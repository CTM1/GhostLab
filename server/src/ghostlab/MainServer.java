package ghostlab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.lang.Integer;

import ghostlab.messages.clientmessages.*;
import ghostlab.messages.servermessages.*;


public class MainServer {
    static final int MAXGAMES = 256;
    // Be careful when using this, bytes in Java are signed.
    // Use Byte.toUnsignedInt(nbOfGames).
    static byte nbOfGames = 0x00;
    static GameServer[] gameServers = new GameServer[MAXGAMES];
    static class MaximumGameCapacityException extends Exception {}
    static class InvalidRequestException extends Exception {
        public InvalidRequestException(String string) { super(string); }
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

            while(true) {
                acceptClient(socket);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void acceptClient(ServerSocket servsock) {
        try {
            Socket socket = servsock.accept();
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
            }   catch(IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void handleClient(Socket client) throws IOException {
        InputStream inStream = client.getInputStream();
        OutputStream outStream = client.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(outStream));
        
        GAMES welcome = new GAMES(nbOfGames);
        welcome.send(outStream);

        for (GameServer gs : getCurrentAvailableGames()) {
            OGAME game = new OGAME(gs);
            game.send(outStream);
        }

        parseMainMenuRequests(br, pw, inStream, outStream, client);
        
        client.close();
    }

    private static void parseMainMenuRequests(BufferedReader br, PrintWriter pw, 
                                                InputStream is, OutputStream os,
                                                Socket client) throws IOException {
        int failedTries = 0;
        byte currentLobby = 0;
        String currPlayerID = "";
        REGNO failed = new REGNO();
        DUNNO dunno = new DUNNO();

        // We'll read the first five characters into this, then handle the rest in
        // "MESSAGE" classes constructors, every clientMessage will take a BF 

        while(true) {
            String request = "";
            try {
                for (int i = 0; i < 5; i++) {
                    request += (char)(br.read());
                }
                System.out.println(request);
                switch (request) {
                    case "UNREG":
                        if (currentLobby == 0 && currPlayerID == "")
                            dunno.send(os);
                        else {
                            if (gameServers[currentLobby].unregister(currPlayerID)) {
                                UNROK unrok = new UNROK((byte) currentLobby);
                                unrok.send(os);
                                currentLobby = 0;
                                currPlayerID = "";
                            }
                            else {
                                dunno.send(os);
                            }
                        }
                    break;
                    case "SIZE?":
                        SIZEQ sizeReq = SIZEQ.parse(br);
                        byte gIDreq = sizeReq.getGameID();
                        GameServer m = gameServers[Byte.toUnsignedInt(gIDreq)];
                        if (m == null)
                            dunno.send(os);
                        else {
                            SIZEA sizeAns = new SIZEA(m);
                            sizeAns.send(os);
                        }
                    break;
                    case "LIST?":
                        LISTQ listQ = LISTQ.parse(br);
                        int gID = Byte.toUnsignedInt(listQ.getGameID());
                        
                        if (gameServers[gID] == null)
                            dunno.send(os);
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
                        }
                        else {
                            NEWPL npl = NEWPL.parse(br);
                            int id = createNewGame(npl, client);
                            
                            if (id == -1)
                                failed.send(os);
                            else { 
                                REGOK replyNewPl = new REGOK((byte)id);
                                replyNewPl.send(os);
                            }
                        }
                    break;
                    case "REGIS":
                        REGIS regis = REGIS.parse(br);
                        byte regGameID = regis.getGameID();
                        System.out.println(regGameID);
                        int regID = Byte.toUnsignedInt(regGameID);
                        System.out.println(regID);
                        String port = regis.getPort();

                        if (gameServers[regID] == null) {
                            throw new InvalidRequestException("Game " + regID 
                                                        + " does not exist.");
                        }
                        if (gameServers[regID].hasStarted()) {
                            throw new InvalidRequestException("Game " + regID 
                                        + " is ongoing. Can't join right now.");      
                        }
                        else {
                            if (gameServers[regID].joinGame(regis, client)) {
                                currPlayerID = regis.getPlayerID();
                                REGOK replyRegis = 
                                    new REGOK((byte) (regis.getGameID()));
                                replyRegis.send(os);
                            }
                            else {
                                failed.send(os);
                            }
                        }
                    break;
                    case "GAME?":
                        GAMEQ gameQ = GAMEQ.parse(br);
                        GAMEA gameA = new GAMEA(gameServers);
                        gameA.send(os);

                        for (GameServer gs : getCurrentAvailableGames()) {
                            OGAME game = new OGAME(gs);
                            game.send(os);
                        }
                    break;
                    case "START":
                        START start = START.parse(br);
                        if (currentLobby == 0 && currPlayerID == "")
                            throw new InvalidRequestException("Bad request: " + request + ", player not yet properly registered in a game");
                        else {
                            /* warn currentLobby of start message so it can start using the TCP Socket */
                            //while (!gs.isOver()) {} 
                        }
                    default:
                        throw new InvalidRequestException("Bad request: " + request);
                }
            } 
            catch (Exception e) {
                e.printStackTrace();
                failed.send(os);

                if (++failedTries == 3) {
                    System.out.println("Too many bad requests from " + 
                            client.toString() + ", closing their connection.");                   
                    client.close();
                    return;
                }
            }
        }
    }

    private static byte getAvailableGameID() {
        byte id = 0;
        
        for (int i = 0; i < MAXGAMES; i++) {
            if (gameServers[i] == null)
                return (id);
            id++;
        }

        return ((byte) -1);
    }

    private static int createNewGame(NEWPL npl, Socket client) {
        byte id = getAvailableGameID();
        int index = Byte.toUnsignedInt(id);

        if (id != -1) {
            gameServers[index] = new GameServer(id, npl.getPort(), npl.getPlayerID(), client);
            nbOfGames++;
            return (id);
        }

        return (-1);
    }

    private static ArrayList<GameServer> getCurrentGames() {
        ArrayList<GameServer> games = new ArrayList<GameServer>();
        
        for (GameServer g : gameServers) {
            if (g != null) {
                games.add(g);
            }
        }

        return games;
    }

    private static ArrayList<GameServer> getCurrentAvailableGames() {
        ArrayList<GameServer> games = new ArrayList<GameServer>();
        
        for (GameServer g : gameServers) {
            if (g != null) {
                if (!g.hasStarted())
                    games.add(g);
            }
        }

        return games;
    }
}
