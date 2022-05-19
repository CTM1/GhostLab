package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;

public class SENDQ implements ClientMessage {
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

  public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
      String[] currPlayerID, OutputStream os, Socket client, MainServer ms) {
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
