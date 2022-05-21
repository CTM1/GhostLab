package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

import ghostlab.Logger;

public class UNROK extends ServerMessage {
  private byte matchID;

  public UNROK(byte m) {
    this.matchID = m;
  }

  public String toString() {
    return ("UNROK [" + Byte.toUnsignedInt(matchID) + "]***");
  }

  @Override
  public void send(OutputStream os) throws IOException {
    Logger.verbose("< %s\n", this);
    os.write("UNROK ".getBytes());
    os.write(matchID);
    os.write("***".getBytes());
    os.flush();
  }
}
