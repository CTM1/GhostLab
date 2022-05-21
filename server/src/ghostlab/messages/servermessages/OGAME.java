package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Logger;
public class OGAME extends ServerMessage {
    private byte id;
    private byte nbOfPlayers;

    public OGAME(GameServer gs) {
        this.id = gs.getGameId();
        this.nbOfPlayers = gs.getNbOfPlayers();
    }

    public String toString() {
        return ("OGAME [" + Byte.toUnsignedInt(id) + "] [" + Byte.toUnsignedInt(nbOfPlayers) + "]***");
    }

    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write("OGAME ".getBytes());
        os.write(this.id);
        os.write(" ".getBytes());
        os.write(this.nbOfPlayers);
        os.write("***".getBytes());
        os.flush();
    }
}
