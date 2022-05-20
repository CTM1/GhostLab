package ghostlab.messages.clientmessages.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;

public class LEMOV implements GameMessage {
    int distance = 0;

    public LEMOV(int dist) {
        this.distance = dist;
    }

    public static LEMOV parse(BufferedReader br) throws IOException {
        int d = MovementMessage.parseDistance(br);
        MovementMessage.getMsgTail(br);
        return new LEMOV(d);
    }

    public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) {
        ph.testMoveAndSendBackMOVEF(2, distance);
    }
}
