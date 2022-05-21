package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Logger;

public class GAMEA extends ServerMessage {
    byte nbOfGames;

    public GAMEA(GameServer[] gs) {
        byte ctr = 0;

        for (GameServer g : gs) {
            if (g != null) {
                if (!g.hasStarted()) {
                    ctr++;
                }
            }
        }

        nbOfGames = ctr;
    }

    public String toString() {
        return ("GAMES [" + Byte.toUnsignedInt(nbOfGames) + "]***");
    }

    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write("GAMES ".getBytes());
        os.write(this.nbOfGames);
        os.write("***".getBytes());
        os.flush();
    }
}