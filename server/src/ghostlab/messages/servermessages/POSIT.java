package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class POSIT implements ServerMessage {
  String playerID;
  String x;
  String y;

  public POSIT(String id, int x, int y) {
    this.playerID = id;
    this.x = String.format("%03d", x);
    this.y = String.format("%03d", y);
  }

  public void send(OutputStream os) throws IOException {
    os.write(String.format("POSIT %s %s %s***", this.playerID, this.x, this.y).getBytes());
    os.flush();
  }
}
