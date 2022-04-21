package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class UNROK implements ServerMessage {
    private byte matchID;
    
    public UNROK(byte m) {
        this.matchID = m;    
    }

    public String toString() {
        return ("UNROK " + this.matchID + "***");
    }

    @Override
    public void send(OutputStream os) throws IOException {
        os.write("UNROK ".getBytes());
        os.write(matchID);
        os.write("***".getBytes());
        os.flush();
    }
}