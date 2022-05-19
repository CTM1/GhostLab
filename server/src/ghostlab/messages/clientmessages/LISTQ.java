package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;

public class LISTQ {
    private byte gameID;

    public LISTQ(byte gId) {
        this.gameID = gId;
    }

    public static LISTQ parse(BufferedReader br) throws IOException {
        br.read(); // the space
        byte gID;

        gID = (byte) br.read();

        for (int i = 0; i < 3; i++)
            br.read();
            
        return new LISTQ(gID);
    }

    public String toString() {
        return("LIST? " + this.gameID + "***");
    }

    public byte getGameID() {
        return(this.gameID);
    }
}