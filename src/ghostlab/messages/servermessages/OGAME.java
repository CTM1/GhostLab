package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
public class OGAME implements ServerMessage {
    private byte id;
    private byte nbOfPlayers;

    public OGAME(GameServer gs) {
        this.id = gs.getId();
        this.nbOfPlayers = gs.getNbOfPlayers();
    }

    public String toString() {
        return ("OGAME " + id + " " + nbOfPlayers + "***");
    }

    public void send(OutputStream os) throws IOException {
        os.write("OGAME ".getBytes());
        os.write(this.id);
        os.write(" ".getBytes());
        os.write(this.nbOfPlayers);
        os.write("***".getBytes());
        os.flush();
    }
}