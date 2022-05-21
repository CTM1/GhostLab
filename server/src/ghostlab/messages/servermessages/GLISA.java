package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;


public class GLISA extends ServerMessage {
    private byte nbOfPlayers;

    public GLISA(int nbOfPlayers) {
        this.nbOfPlayers = (byte)nbOfPlayers;
    }

    public String toString() {
        return String.format("GLIS! [%d]***", Byte.toUnsignedInt(nbOfPlayers));
    }

    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write("GLIS! ".getBytes());
        os.write(nbOfPlayers);
        os.write("***".getBytes());
        os.flush();
    }
}
