package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import ghostlab.GameServer;
import ghostlab.Logger;
import ghostlab.MainServer;
import ghostlab.Player;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.DUNNO;
import ghostlab.messages.servermessages.LISTA;
import ghostlab.messages.servermessages.PLAYR;

public class LISTQ implements MenuMessage {
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

    public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws IOException {
        DUNNO dunno = new DUNNO();

        int gID = Byte.toUnsignedInt(this.getGameID());

        if (ch.ms.getGameServers()[gID] == null) {
            dunno.send(os);
            Logger.log("eee");
        }
            
        else {
            LISTA listA = new LISTA(ch.ms.getGameServers()[gID]);
            listA.send(os);
            ArrayList<Player> lobby = ch.ms.getGameServers()[gID].getLobby();

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