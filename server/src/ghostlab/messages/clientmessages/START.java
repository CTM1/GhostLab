package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import ghostlab.GameServer;
import ghostlab.Logger;
import ghostlab.MainServer;

public class START implements ClientMessage {

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

  public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
      String[] currPlayerID, OutputStream os, Socket client, MainServer ms) throws Exception {
    if (currentLobby[0] == 0 && currPlayerID[0] == "")
      throw new InvalidRequestException(
          "Player not yet properly registered in a game");
    else {
      ms.getGameServers()[currentLobby[0]].addPlayerReady();
      ms.getGameServers()[currentLobby[0]].startTheGameIfAllReady();
      // wait the game out
      while (!ms.getGameServers()[currentLobby[0]].isOver())
        ;

      HashMap<Socket, Boolean> hs = ms.getGameServers()[currentLobby[0]].getEndedPeacefully();
      // Close client connection
      Logger.log("BBBBB " + hs + "\n");
      Boolean ret = hs.get(client);
      if (ret != null && !ret) {
        return;
      }
    }
  }

  public String toString() {
    return ("START***");
  }
}
