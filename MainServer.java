import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class MainServer {

    private static void handleClient(Socket client) throws IOException {
        InputStream inStream = client.getInputStream();
        OutputStream outStream = client.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(outStream));
        
        client.close();
    }

    private static class ClientHandler extends Thread {
        Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                handleClient(socket);
            } catch(IOException e) {
                e.printStackTrace();
            }
            
        }
    }

    private static void acceptClient(ServerSocket servsock) {
        try {
            Socket socket = servsock.accept();
            ClientHandler ch = new ClientHandler(socket);
            ch.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try {
            ServerSocket socket = new ServerSocket(port);

            while(true) {
                acceptClient(socket);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
}