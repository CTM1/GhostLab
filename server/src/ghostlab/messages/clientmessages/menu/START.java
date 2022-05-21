package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;

public class START implements MenuMessage {

  static class InvalidRequestException extends Exception {
		public InvalidRequestException(String string) {
			super(string);
		}
	}

  public static START parse(BufferedReader br) throws IOException {
    for (int i = 0; i < 3; i++)
      br.read();

    return new START();
  }

  public synchronized void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws Exception {
    if (ch.currentLobby[0] == 0 && ch.currPlayerID[0] == "")
      throw new InvalidRequestException(
          "Player not yet properly registered in a game");
    else {
      GameServer gs = ch.ms.getGameServers()[ch.currentLobby[0]];
      gs.addPlayerReady(this);
      gs.startTheGameIfAllReady();
      ch.shouldStop = true;
    }
  }

  public String toString() {
    return ("START***");
  }
}
