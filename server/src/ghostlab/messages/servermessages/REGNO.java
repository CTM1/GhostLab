package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class REGNO extends ServerMessage {
    public String toString() {
        return ("REGNO***");
    }

    public void send(OutputStream os) throws IOException {
        os.write("REGNO***".getBytes());
        os.flush();
    }
}