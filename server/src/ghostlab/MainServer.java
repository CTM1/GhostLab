package ghostlab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.net.ServerSocket;

import ghostlab.messages.clientmessages.*;
import ghostlab.messages.servermessages.*;

public class MainServer {
    // Be careful when using this, bytes in Java are signed.
    // Use Byte.toUnsignedInt(nbOfGames).
    // Great, fucking A. toodledoo.
    static byte nbOfGames = 0x00;
    static ArrayList<GameServer> gameServers = new ArrayList<GameServer>();
    static class MaximumGameCapacityException extends Exception {}
    static class InvalidRequestException extends Exception {
        public InvalidRequestException(String string) {
        }
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
        // CITIZEN citizen = new CITIZEN();

        // citizen.send(pw);
        welcome.send(outStream);

        for (GameServer gs : gameServers) {
            OGAME game = new OGAME(gs);
            game.send(outStream);
        }

        parseRequests(br, pw);
        
        client.close();
    }

    private static void parseRequests(BufferedReader br, PrintWriter pw) {
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
                        //TODO use GameServer length instead of this...
                        //idk how we'll handle when games are over but hey yahoo.
                        if (Byte.toUnsignedInt(nbOfGames) >= 255)
                            throw new MaximumGameCapacityException();
                        else {
                            NEWPL npl = NEWPL.parse(br);
                            nbOfGames++;
                        }
                        break;
                    case "REGIS":
                        // REGOK msg = new REGOK(m);
                        // msg.send(pw);
                        //joinGame(?);
                        break;
                    default:
                        throw new InvalidRequestException("REQUEST "+request+ " IS FUCKY");
                }

                /* Reflection here might be interesting, or not, idk.
                (getting Constructor by name instead of a Class by name can work?)
                I tried it and a switch seemed simpler
                */
            } 
            catch (Exception e)
            {
                e.printStackTrace();
                REGNO failed = new REGNO();
                pw.write(failed.toString());

                if (failedTries++ == 3);
                    break;
            }
        }
        
    }
}