package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;

public class NEWPL {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!

    public NEWPL(BufferedReader br) throws IOException {
        //this.playerID = i;
        //this.port = p;
        br.read(); // the space
        this.playerID = "";
        this.port = "";
        String suffix = "";

        for(int i=0; i<8; i++) //the player id
            this.playerID += (char)br.read();

        br.read(); // the space

        for(int i=0; i<4; i++) //the port
            this.port += (char)br.read();

        for(int i=0; i<3; i++) //***
            suffix += (char)br.read();
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getPort() {
        return port;
    }
}