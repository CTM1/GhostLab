package ghostlab.messages.clientmessages.game;

import ghostlab.GameServer;
import ghostlab.Logger;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;
import ghostlab.messages.servermessages.GLISA;
import ghostlab.messages.servermessages.GPLYR;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public class GLISQ implements GameMessage {

  public static GLISQ parse(BufferedReader br) throws IOException {
    for (int i = 0; i < 3; i++) br.read();
    return new GLISQ();
  }

  public String toString() {
    return "GLIS?***";
  }

  public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os)
      throws IOException {
    GLISA gl = new GLISA(gs.getLobby().size());
    // Logger.verbose("> %s : %s\n", p, gl);
    gl.send(os);

    // send GPLYR
    GPLYR gp;
    for (Player pl : gs.getLobby()) {
      gp = new GPLYR(pl);
      // Logger.verbose("> %s : %s\n", p, gp);
      gp.send(os);
    }
  }
}
