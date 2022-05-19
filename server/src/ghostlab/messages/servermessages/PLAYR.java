package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

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
        os.write("PLAYR ".getBytes());
        os.write(this.playerID.getBytes());
        os.write("***".getBytes());
        os.flush();
    }
}