package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;

public class DUNNO extends ServerMessage {
  public String toString() {
    return ("DUNNO***");
  }

  public void send(OutputStream os) throws IOException {
    Logger.verbose("< %s", this);
    os.write("DUNNO***".getBytes());
    os.flush();
  }
}
