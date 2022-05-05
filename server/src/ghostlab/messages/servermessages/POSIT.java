package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class POSIT implements ServerMessage {
  String cnt;

  public POSIT(String id, int x, int y) {
    this.cnt =
        String.format(
            "POSIT %s %d %d***", id, x, y);
  }

  public void send(OutputStream os) throws IOException {
    os.write(cnt.getBytes());
  }
}
