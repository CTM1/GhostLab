package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;
import ghostlab.Player;

public class PLAYR extends ServerMessage {
    private String playerID;

    public PLAYR(Player player) {
        this.playerID = player.getPlayerID();
    }

    public String toString() {
        return ("PLAYR " + playerID + "***");
    }

    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write("PLAYR ".getBytes());
        os.write(this.playerID.getBytes());
        os.write("***".getBytes());
        os.flush();
    }
}