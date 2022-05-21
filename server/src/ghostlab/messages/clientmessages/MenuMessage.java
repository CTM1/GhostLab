package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.OutputStream;

import ghostlab.MainServer;

public interface MenuMessage {
    public void executeRequest(BufferedReader br, OutputStream os, MainServer.ClientHandler ch) throws Exception;
}
