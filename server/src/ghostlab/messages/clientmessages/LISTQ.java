package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.Player;
import ghostlab.messages.servermessages.DUNNO;
import ghostlab.messages.servermessages.LISTA;
import ghostlab.messages.servermessages.PLAYR;

public class LISTQ implements ClientMessage {
    private Byte gameID;

    public LISTQ(Byte gId) {
        this.gameID = gId;
    }

    public static LISTQ parse(BufferedReader br) throws IOException {
        br.read(); // the space
        Byte gID;

        gID = (byte) br.read();

        for (int i = 0; i < 3; i++)
            br.read();

        return new LISTQ(gID);
    }

    public String toString() {
        return ("LIST? " + this.gameID + "***");
    }

    public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
            String[] currPlayerID, OutputStream os, Socket client, MainServer ms) throws IOException {
        DUNNO dunno = new DUNNO();

        int gID = Byte.toUnsignedInt(this.getGameID());

        if (gameServers[gID] == null)
            dunno.send(os);
        else {
            LISTA listA = new LISTA(gameServers[gID]);
            listA.send(os);
            ArrayList<Player> lobby = gameServers[gID].getLobby();

            for (Player p : lobby) {
                PLAYR pmsg = new PLAYR(p);
                pmsg.send(os);
                System.out.println(p.getPlayerID());
            }
        }
    }

    public Byte getGameID() {
        return (this.gameID);
    }
}