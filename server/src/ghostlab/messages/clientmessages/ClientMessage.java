package ghostlab.messages.clientmessages;

import java.io.BufferedReader;

import java.io.OutputStream;
import java.net.Socket;

import ghostlab.GameServer;
import ghostlab.MainServer;

public interface ClientMessage {
    public void executeRequest(Byte nbOfGames, BufferedReader br, GameServer[] gameServers, Byte[] currentLobby,
            String[] currPlayerID, OutputStream os, Socket client, MainServer ms) throws Exception;
}
