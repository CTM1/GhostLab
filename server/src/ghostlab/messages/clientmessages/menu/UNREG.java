package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.*;

public class UNREG implements MenuMessage {

    public static UNREG parse(BufferedReader br) throws IOException {
        for (int i = 0; i < 3; i++)
            br.read();

        return new UNREG();
    }

    public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
            String[] currPlayerID, OutputStream os, Socket client, MainServer ms) throws IOException {
        DUNNO dunno = new DUNNO();
            if (currentLobby[0] == 0 && currPlayerID[0] == "")
                dunno.send(os);
            else {
                if (gameServers[currentLobby[0]].unregister(currPlayerID[0])) {
                    UNROK unrok = new UNROK((Byte) currentLobby[0]);
                    unrok.send(os);
                    if (gameServers[currentLobby[0]].getNbOfPlayers() == 0) {
                        gameServers[currentLobby[0]] = null;
                        currentLobby[0] = 0;
                        currPlayerID[0] = "";

                    } else
                        dunno.send(os);
                }
            }
    }

    public String toString() {
        return ("UNREG***");
    }
}