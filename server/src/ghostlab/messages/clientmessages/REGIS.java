package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

public class REGIS {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!
    private byte gameID;

    public REGIS(String pId, String p, byte gId) {
        this.playerID = pId;
        this.port = p;
        this.gameID = gId;
    }

    public static REGIS parse(BufferedReader br) throws IOException {
        br.read(); // the space
        String playerID = "";
        String port = "";
        String suffix = "";
        byte gID;

        for(int i=0; i<8; i++) //the player id
            playerID += (char) br.read();

        br.read(); // the space

        for(int i=0; i<4; i++) //the port
            port += (char) br.read();

        br.read(); // the space

        gID = (byte) br.read();

        for(int i=0; i<3; i++) //***
            suffix += (char) br.read();

        return new REGIS(playerID, port, gID);
    }

    public String toString() {
        return("REGIS " + this.playerID + " " + this.port + " " + this.gameID + "***");
    }

    public byte getGameID() {
        return(this.gameID);
    }
    
    public String getPort() {
        return(this.port);
    }
    
    public String getPlayerID() {
        return(this.playerID);
    }
}