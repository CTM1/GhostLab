package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.GAMEA;
import ghostlab.messages.servermessages.OGAME;

public class GAMEQ implements MenuMessage {

  public static GAMEQ parse(BufferedReader br) throws IOException {
    for (int i = 0; i < 3; i++)
      br.read();

    return new GAMEQ();
  }

  public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
      String[] currPlayerID, OutputStream os, Socket client, MainServer ms) throws Exception {
    GAMEA gameA = new GAMEA(gameServers);
    gameA.send(os);

    for (GameServer gs : ms.getCurrentAvailableGames()) {
      OGAME game = new OGAME(gs);
      game.send(os);
    }
  }

  public String toString() {
    return ("GAME?***");
  }
}
