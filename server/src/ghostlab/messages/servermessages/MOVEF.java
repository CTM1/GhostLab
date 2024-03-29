package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;
import ghostlab.Player;

public class MOVEF extends ServerMessage {
    private String content;

    public MOVEF(Player p) {
      content = String.format("MOVEF %03d %03d %04d***", p.getX(), p.getY(), p.getScore());
    }

    public String toString() {
        return content;
    }

    public void send(OutputStream os) throws IOException {
        Logger.verbose("< %s\n", this);
        os.write(content.getBytes());
        os.flush();
    }
}
