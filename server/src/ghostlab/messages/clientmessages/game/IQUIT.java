package ghostlab.messages.clientmessages.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Logger;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;

public class IQUIT implements GameMessage {

    public static IQUIT parse(BufferedReader br) throws IOException {
        for(int i=0; i<3; i++) br.read();
        return new IQUIT();
    }

    public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) throws IOException {
        Logger.log("Got IQUIT from " + p.getPlayerID()+"\n");
        os.write("GOBYE***".getBytes());
        os.flush();

        //TODO : manage player removal from gameserver, stop playerhandler, close socket, etc
        gs.getLobby().remove(p);
        p.getTCPSocket().close();
        ph.shouldQuit = true;
    }

}


