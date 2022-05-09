package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Player;

public class GLIS implements ServerMessage {
    private int nbOfPlayers;

    public GLIS(int nbOfPlayers) {
        this.nbOfPlayers = nbOfPlayers;
    }

    public String toString() {
        return String.format("GLIS! %d***", nbOfPlayers);
    }

    public void send(OutputStream os) throws IOException {
        os.write(this.toString().getBytes());
        os.flush();
    }
}
