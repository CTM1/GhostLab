package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;

public class REGOK extends ServerMessage {
    private byte matchID;
    
    public REGOK(byte m) {
        this.matchID = m;    
    }

    public String toString() {
        return ("REGOK [" + Byte.toUnsignedInt(matchID) + "]***");
    }

    @Override
    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write("REGOK ".getBytes());
        os.write(matchID);
        os.write("***".getBytes());
        os.flush();
    }
}