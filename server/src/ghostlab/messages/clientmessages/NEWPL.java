package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;

public class NEWPL {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!

    public NEWPL(String playerID, String port) throws IOException {
        this.playerID = playerID;
        this.port = port;
    }

    public static NEWPL parse(BufferedReader br) throws IOException {
        br.read(); // the space
        String playerID = "";
        String port = "";
        String suffix = "";

        for(int i=0; i<8; i++) //the player id
            playerID += (char)br.read();

        br.read(); // the space

        for(int i=0; i<4; i++) //the port
            port += (char)br.read();

        for(int i=0; i<3; i++) //***
            suffix += (char)br.read();

        return new NEWPL(playerID, port);
    }

    public String toString() {
        return "NEWPL "+this.playerID+" "+this.port+"***";
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getPort() {
        return port;
    }
}