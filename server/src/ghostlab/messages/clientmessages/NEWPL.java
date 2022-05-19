package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;
import ghostlab.messages.servermessages.REGNO;
import ghostlab.messages.servermessages.REGOK;

public class NEWPL implements ClientMessage {

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

    public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
            String[] currPlayerID, OutputStream os, Socket client, MainServer ms) throws Exception {
        REGNO regno = new REGNO();
        if (Byte.toUnsignedInt(nbOfGames) >= 255) {
                throw new MaximumGameCapacityException();
        } else {
            int id = ms.createNewGame(this, client);

            if (id == -1)
                regno.send(os);
            else {
                REGOK replyNewPl = new REGOK((byte) id);
                replyNewPl.send(os);
                currentLobby[0] = (byte) id;
                currPlayerID[0] = this.getPlayerID();
            }
        }
    }

    public String getPort() {
        return port;
    }
}