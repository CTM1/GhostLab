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

import ghostlab.messages.clientmessages.*;
import ghostlab.messages.servermessages.*;

public class MainServer {
    // Be careful when using this, bytes in Java are signed.
    // Use Byte.toUnsignedInt(nbOfGames).
    static byte nbOfGames = 0x00;
    static GameServer[] gameServers = new GameServer[256];
    static class MaximumGameCapacityException extends Exception {}
    static class InvalidRequestException extends Exception {
        public InvalidRequestException(String string) {}
    }

    private static ArrayList<GameServer> getCurrentGames() {
        ArrayList<GameServer> ret = new ArrayList<GameServer>();
        for(int i=0; i<256; i++) {
            if (gameServers[i] != null) {
                ret.add(gameServers[i]);
            }
        }
        return ret;
    }
    
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

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
            } catch(IOException e) {
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

        for (GameServer gs : getCurrentGames()) {
            OGAME game = new OGAME(gs);
            game.send(outStream);
        }

        parseRequests(br, pw, inStream, outStream, (InetSocketAddress) client.getRemoteSocketAddress());
        
        client.close();
    }

    private static void parseRequests(BufferedReader br, PrintWriter pw, InputStream is, OutputStream os, InetSocketAddress playerAddr) {
        int failedTries = 0;

        // We'll read the first five characters into this, then handle the rest in
        // "MESSAGE" classes constructors, every clientMessage will take a BF 
        // (buffered reader boyfriend!1!!!1!!)

        while(true) {
            String request = "";
            try {
                for (int i = 0; i < 5; i++) {
                    request += (char)(br.read());
                }

                switch (request) {
                    case "NEWPL":
                        //idk how we'll handle when games are over but hey yahoo.
                        if (Byte.toUnsignedInt(nbOfGames) >= 255)
                            throw new MaximumGameCapacityException();
                        else {
                            NEWPL npl = NEWPL.parse(br);
                            int id = createNewGame(npl, playerAddr);
                            REGOK reply = new REGOK((byte)id);
                            reply.send(os);
                        }
                        break;
                    case "REGIS":
                        REGIS regis = REGIS.parse(br);
                        byte gID = regis.getGameID();
                        String port = regis.getPort();

                        if (gameServers[gID] == null) {
                            throw new InvalidRequestException("Game " + gID + " does not exist.");
                        }
                        if (gameServers[gID].started) {
                            throw new InvalidRequestException("Game " + gID + " is ongoing.");
                        }
                        else {
                            gameServers[gID].joinGame(regis.getPlayerID());
                            REGOK reply = new REGOK((byte) (regis.getGameID()));
                            reply.send(os);
                        }
                        break;
                    default:
                        throw new InvalidRequestException("REQUEST "+request+ " IS FUCKY");
                }
            } 
            catch (Exception e)
            {
                e.printStackTrace();
                REGNO failed = new REGNO();
                pw.write(failed.toString());

                if (++failedTries == 3);
                    break;
            }
        }
        
    }

    private static int getAvailableGameID() {
        for(int i=0; i<256; i++) {
            if (gameServers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private static int createNewGame(NEWPL npl, InetSocketAddress hostAddr) {
        int id = getAvailableGameID();
        gameServers[id] = new GameServer(id, npl.getPort(), npl.getPlayerID(), hostAddr);
        nbOfGames++;
        return id;
    }
}