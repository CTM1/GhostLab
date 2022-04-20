package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class DUNNO implements ServerMessage {
    public String toString() {
        return ("DUNNO***");
    }

    public void send(OutputStream os) throws IOException {
        os.write("DUNNO***".getBytes());
        os.flush();
    }
}