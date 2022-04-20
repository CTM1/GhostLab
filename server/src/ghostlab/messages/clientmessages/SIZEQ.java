package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

public class SIZEQ {
    private byte gameID;

    public SIZEQ(byte gId) {
        this.gameID = gId;
    }

    public static SIZEQ parse(BufferedReader br) throws IOException {
        br.read(); // the space
        byte gID;

        gID = (byte) br.read();

        return new SIZEQ(gID);
    }

    public String toString() {
        return("SIZEQ " + this.gameID + "***");
    }

    public byte getGameID() {
        return(this.gameID);
    }
}