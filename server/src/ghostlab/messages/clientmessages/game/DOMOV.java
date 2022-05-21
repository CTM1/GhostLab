package ghostlab.messages.clientmessages.game;

import ghostlab.GameServer;
import ghostlab.Logger;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public class DOMOV implements GameMessage {
  int distance = 0;

  public DOMOV(int dist) {
    this.distance = dist;
  }

  public static DOMOV parse(BufferedReader br) throws IOException {
    int d = MovementMessage.parseDistance(br);
    MovementMessage.getMsgTail(br);
    return new DOMOV(d);
  }

  public void executeRequest(
      GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) {
    Logger.verbose("> %s : MOVEF\n", p);
    ph.testMoveAndSendBackMOVEF(1, distance);
  }
}
