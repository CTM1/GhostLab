package ghostlab.messages.clientmessages.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;

public class SENDQ implements GameMessage {
  private String id;
  private String message;

  public static SENDQ parse(BufferedReader br) throws IOException {
    br.read(); // space
    String id = "";
    String msg = "";

    for (int i = 0; i < 8; i++) {
      id += (char) br.read();
    } // read ID
    br.read(); // space

    int nread = 0;
    int nasterisk = 0;
    while (nread <= 200) {
      char c = (char) br.read();
      if (c == '*') {
        nasterisk++;
        if (nasterisk == 3)
          break;
        continue;
      } else if (nasterisk > 0) {
        for (int i = 0; i < nasterisk; i++)
          msg += "*";
      }
      msg += c;
    }

    return new SENDQ(id, msg);
  }

  public SENDQ(String id, String mess) {
    this.id = id;
    this.message = mess;
  }

  public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) throws IOException {
    if (gs.sendMessage(p.getPlayerID(), getID(), getMessage())) {
      os.write("SEND!***".getBytes());
    } else {
      os.write("NSEND***".getBytes());
    }
    os.flush();
  }

  public String getID() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public String toString() {
    return String.format("SEND? %s %s***", id, message);
  }
}
