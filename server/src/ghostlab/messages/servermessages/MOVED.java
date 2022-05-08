package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Player;

public class MOVED implements ServerMessage {
    private String content;

    public MOVED(Player p) {
      content = String.format("MOVE! %03d %03d***", p.getX(), p.getY());
    }

    public String toString() {
        return content;
    }

    public void send(OutputStream os) throws IOException {
        os.write(content.getBytes());
        os.flush();
    }
}
