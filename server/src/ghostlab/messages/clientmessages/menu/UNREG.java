package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.*;

public class UNREG implements MenuMessage {

    public static UNREG parse(BufferedReader br) throws IOException {
        for (int i = 0; i < 3; i++)
            br.read();

        return new UNREG();
    }

    public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws IOException {
        DUNNO dunno = new DUNNO();
        if (ch.currentLobby[0] == 0 && ch.currPlayerID[0] == "") {
            dunno.send(os);
        }
        else {
            if (ch.ms.getGameServers()[ch.currentLobby[0]].unregister(ch.currPlayerID[0])) {
                UNROK unrok = new UNROK((Byte) ch.currentLobby[0]);
                unrok.send(os);
                if (ch.ms.getGameServers()[ch.currentLobby[0]].getNbOfPlayers() == 0) {
                    ch.ms.getGameServers()[ch.currentLobby[0]] = null;
                    ch.currentLobby[0] = 0;
                    ch.currPlayerID[0] = "";
                } else {
                    ch.ms.getGameServers()[ch.currentLobby[0]].startTheGameIfAllReady();
                }
            }
        }
    }

    public String toString() {
        return ("UNREG***");
    }
}