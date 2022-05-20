package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.DUNNO;
import ghostlab.messages.servermessages.SIZEA;

public class SIZEQ implements MenuMessage {
    private Byte gameID;

    public SIZEQ(Byte gId) {
        this.gameID = gId;
    }

    public static SIZEQ parse(BufferedReader br) throws IOException {
        br.read(); // the space
        Byte gID;

        gID = (byte) br.read();

        for (int i = 0; i < 3; i++)
            br.read();

        return new SIZEQ(gID);
    }

    public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws IOException {
        DUNNO dunno = new DUNNO();

        
        Byte gIDreq = this.getGameID();
        GameServer m = ch.ms.getGameServers()[Byte.toUnsignedInt(gIDreq)];
        if (m == null)
            dunno.send(os);
        else {
            SIZEA sizeAns = new SIZEA(m);
            sizeAns.send(os);
        }
    }

    public String toString() {
        return ("SIZEQ " + this.gameID + "***");
    }

    public Byte getGameID() {
        return (this.gameID);
    }
}