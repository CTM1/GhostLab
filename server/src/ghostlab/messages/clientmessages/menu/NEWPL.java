package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.REGNO;
import ghostlab.messages.servermessages.REGOK;

public class NEWPL implements MenuMessage {

    private String playerID;
    private String port;

    static class MaximumGameCapacityException extends Exception {}

    public NEWPL(String playerID, String port) throws IOException {
        this.playerID = playerID;
        this.port = port;
    }

    public static NEWPL parse(BufferedReader br) throws IOException {
        br.read(); // the space
        String playerID = "";
        String port = "";
        String suffix = "";

        for (int i = 0; i < 8; i++) // the player id
            playerID += (char) br.read();

        br.read(); // the space

        for (int i = 0; i < 4; i++) // the port
            port += (char) br.read();

        for (int i = 0; i < 3; i++) // ***
            suffix += (char) br.read();

        if (!suffix.equals("***"))
            throw new IOException("Invalid message suffix");

        return new NEWPL(playerID, port);
    }

    public String toString() {
        return "NEWPL " + this.playerID + " " + this.port + "***";
    }

    public String getPlayerID() {
        return playerID;
    }

    public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws Exception {
        REGNO regno = new REGNO();
        if (ch.ms.getCurrentAvailableGames().size() >= 255) {
                throw new MaximumGameCapacityException();
        } else {
            int id = ch.ms.createNewGame(this, ch.socket);

            if (id == -1)
                regno.send(os);
            else {
                REGOK replyNewPl = new REGOK((byte) id);
                replyNewPl.send(os);
                ch.currentLobby[0] = (byte) id;
                ch.currPlayerID[0] = this.getPlayerID();
            }
        }
    }

    public String getPort() {
        return port;
    }
}