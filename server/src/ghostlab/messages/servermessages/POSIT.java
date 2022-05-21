package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;

public class POSIT extends ServerMessage {
  String playerID;
  String x;
  String y;

  public POSIT(String id, int x, int y) {
    this.playerID = id;
    this.x = String.format("%03d", x);
    this.y = String.format("%03d", y);
  }

  public void send(OutputStream os) throws IOException {
    Logger.verbose("< POSIT %s %s %s***\n", this.playerID, this.x, this.y);
    os.write(String.format("POSIT %s %s %s***", this.playerID, this.x, this.y).getBytes());
    os.flush();
  }
}
