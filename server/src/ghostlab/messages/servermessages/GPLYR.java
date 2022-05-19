package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Player;

public class GPLYR extends ServerMessage {
    private Player p;

    public GPLYR(Player p) {
        this.p = p;
    }

    public String toString() {
        return String.format("GPLYR %s %03d %03d %04d***", p.getPlayerID(), p.getX(), p.getY(), p.getScore());
    }

    public void send(OutputStream os) throws IOException {
        os.write(this.toString().getBytes());
        os.flush();
    }
}
