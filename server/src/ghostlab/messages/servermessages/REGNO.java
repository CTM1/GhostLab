package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;

public class REGNO extends ServerMessage {
    public String toString() {
        return ("REGNO***");
    }

    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write("REGNO***".getBytes());
        os.flush();
    }
}