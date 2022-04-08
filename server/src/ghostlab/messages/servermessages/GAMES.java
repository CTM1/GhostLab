package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class GAMES implements ServerMessage {
    private byte n;

    public GAMES(byte n) {
        this.n = n;
    }

    public String toString() {
        return("GAMES " + Byte.toUnsignedInt(this.n) + "***");
    }
    
    public void send(OutputStream os) throws IOException {
        os.write("GAMES ".getBytes());
        os.write(this.n);
        os.write("***".getBytes());
        os.flush();
    }
}