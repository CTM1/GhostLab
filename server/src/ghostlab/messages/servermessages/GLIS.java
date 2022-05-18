package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Player;

public class GLIS implements ServerMessage {
    private byte nbOfPlayers;

    public GLIS(int nbOfPlayers) {
        this.nbOfPlayers = (byte)nbOfPlayers;
    }

    public String toString() {
        return String.format("GLIS! %d***", Byte.toUnsignedInt(nbOfPlayers));
    }

    public void send(OutputStream os) throws IOException {
        os.write("GLIS! ".getBytes());
        os.write(nbOfPlayers);
        os.write("***".getBytes());
        os.flush();
    }
}
