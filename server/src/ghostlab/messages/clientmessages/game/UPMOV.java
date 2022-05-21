package ghostlab.messages.clientmessages.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.GameServer;
import ghostlab.Player;
import ghostlab.messages.clientmessages.GameMessage;

public class UPMOV implements GameMessage {
    int distance = 0;

    public UPMOV(int dist) {
        this.distance = dist;
    }

    public String toString() {
        return String.format("UPMOV %03d***", distance);
    }

    public static UPMOV parse(BufferedReader br) throws IOException {
        int d = MovementMessage.parseDistance(br);
        MovementMessage.getMsgTail(br);
        return new UPMOV(d);
    }

    public void executeRequest(GameServer.PlayerHandler ph, GameServer gs, Player p, OutputStream os) {
        ph.testMoveAndSendBackMOVEF(0, distance);
    }
}
