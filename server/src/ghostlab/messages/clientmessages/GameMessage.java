package ghostlab.messages.clientmessages;


import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Player;

public interface GameMessage {
    public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) throws Exception;
}
