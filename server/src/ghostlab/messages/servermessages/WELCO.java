package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class WELCO implements ServerMessage {
  String cnt;

  public WELCO(byte m, char h, char w, int f, String ip, String port) {
    this.cnt =
        String.format(
            "WELCO %c %c %c %d %s %s***", (char) Byte.toUnsignedLong(m), h, w, f, ip, port);
  }

  public void send(OutputStream os) throws IOException {
    os.write(cnt.getBytes());
    System.out.println("TODO");
  }
}
