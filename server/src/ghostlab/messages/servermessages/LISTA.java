package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;

public class LISTA implements ServerMessage {
    private byte id;
    private byte nbOfPlayers;

    public LISTA(GameServer gs) {
        this.id = gs.getGameId();
        this.nbOfPlayers = gs.getNbOfPlayers();
    }

    public String toString() {
        return ("LIST! " + id + " " + nbOfPlayers + "***");
    }

    public void send(OutputStream os) throws IOException {
        os.write("LIST! ".getBytes());
        os.write(this.id);
        os.write(" ".getBytes());
        os.write(this.nbOfPlayers);
        os.write("***".getBytes());
        os.flush();
    }
}
