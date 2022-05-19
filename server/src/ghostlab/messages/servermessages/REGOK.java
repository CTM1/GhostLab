package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class REGOK extends ServerMessage {
    private byte matchID;
    
    public REGOK(byte m) {
        this.matchID = m;    
    }

    public String toString() {
        return ("REGOK " + this.matchID + "***");
    }

    @Override
    public void send(OutputStream os) throws IOException {
        os.write("REGOK ".getBytes());
        os.write(matchID);
        os.write("***".getBytes());
        os.flush();
    }
}