package ghostlab.messages.clientmessages.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import ghostlab.MainServer;
import ghostlab.messages.clientmessages.MenuMessage;
import ghostlab.messages.servermessages.REGNO;
import ghostlab.messages.servermessages.REGOK;

public class REGIS implements MenuMessage {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!
    private byte gameID;

    static class InvalidRequestException extends Exception {
		public InvalidRequestException(String string) {
			super(string);
		}
	}

    public REGIS(String pId, String p, byte gId) {
        this.playerID = pId;
        this.port = p;
        this.gameID = gId;
    }

    public static REGIS parse(BufferedReader br) throws IOException {
        br.read(); // the space
        String playerID = "";
        String port = "";
        String suffix = "";
        byte gID;

        for (int i = 0; i < 8; i++) // the player id
            playerID += (char) br.read();

        br.read(); // the space

        for (int i = 0; i < 4; i++) // the port
            port += (char) br.read();

        br.read(); // the space

        gID = (byte) br.read();

        for (int i = 0; i < 3; i++) // ***
            suffix += (char) br.read();

        if (!suffix.equals("***"))
            throw new IOException("Invalid message suffix");

        return new REGIS(playerID, port, gID);
    }

    public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws Exception {
                REGNO regno = new REGNO();

                
                byte regGameID = this.getGameID();
                System.out.println(regGameID);
                int regID = Byte.toUnsignedInt(regGameID);
                System.out.println(regID);

                if (ch.ms.getGameServers()[regID] == null) {
                    // throw new InvalidRequestException("Game " + regID + " does not exist.");
                    regno.send(os);
                } else if (ch.ms.getGameServers()[regID].hasStarted()) {
                    // throw new InvalidRequestException(
                    //         "Game " + regID + " is ongoing. Can't join right now.");
                    regno.send(os);
                } else {
                    if (ch.ms.getGameServers()[regID].joinGame(this, ch.socket)) {
                        ch.currPlayerID[0] = this.getPlayerID();
                        REGOK replyRegis = new REGOK((byte) (this.getGameID()));
                        replyRegis.send(os);
                        ch.currentLobby[0] = (byte) regID;
                        ch.currPlayerID[0] = this.getPlayerID();
                    } else {
                        regno.send(os);
                    }
                }
    }

    public String toString() {
        return ("REGIS " + this.playerID + " " + this.port + " " + this.gameID + "***");
    }

    public byte getGameID() {
        return (this.gameID);
    }

    public String getPort() {
        return (this.port);
    }

    public String getPlayerID() {
        return (this.playerID);
    }
}