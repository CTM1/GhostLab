package ghostlab.messages.clientmessages.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.messages.clientmessages.GameMessage;
import ghostlab.messages.servermessages.GLISA;
import ghostlab.messages.servermessages.GPLYR;
import ghostlab.GameServer;
import ghostlab.Player;

public class GLISQ implements GameMessage {

    public static GLISQ parse(BufferedReader br) throws IOException {
        for (int i = 0; i < 3; i++) br.read();
        return new GLISQ();
    }

    public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) throws IOException {
        (new GLISA(gs.getLobby().size())).send(os);

        // send GPLYR
        for (Player pl : gs.getLobby()) {
            (new GPLYR(pl)).send(os);
        }
    }
    
}
