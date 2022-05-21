package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

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

  public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws Exception {
    GAMEA gameA = new GAMEA(ch.ms.getGameServers());
    gameA.send(os);

    for (GameServer gs : ch.ms.getCurrentAvailableGames()) {
      OGAME game = new OGAME(gs);
      game.send(os);
    }
  }

  public String toString() {
    return ("GAME?***");
  }
}
