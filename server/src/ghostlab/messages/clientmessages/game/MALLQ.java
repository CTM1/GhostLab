package ghostlab.messages.clientmessages.game;

import ghostlab.GameServer;
import ghostlab.Logger;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public class MALLQ implements GameMessage {
  private String message;

  public MALLQ(String msg) {
    this.message = msg;
  }

  public static MALLQ parse(BufferedReader br) throws IOException {
    br.read();
    char[] buff = new char[200];
    int read = 0;
    while (read <= 200) {
      char c = (char) br.read();
      if (c == '*') {
        br.read();
        br.read();
        break;
      }
      buff[read] = c;
      read++;
    }

    return new MALLQ(new String(buff, 0, read));
  }

  public String toString() {
    return String.format("MALL? %s***", message);
  }

  public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os)
      throws IOException {
    Logger.log("[*] Sending " + message + " from " + p.getPlayerID() + "\n");
    gs.getMulticast().MESSA(p.getPlayerID(), message);

    Logger.verbose("> %s : MALL!***\n", p);
    os.write("MALL!***".getBytes());
    os.flush();
  }
}
