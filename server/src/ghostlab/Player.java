package ghostlab;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Socket;

public class Player {
    int x;
    int y;
    int id;
    int UDPport;
    String name;
    String playerIP;
    Socket TCPSocket;
    DatagramSocket UDPsocket;
    
    public Player(int id, String name, String UDPport, Socket TCPSocket) throws SocketException {
        this.name = name;
        this.id = id;
        this.UDPport = Integer.parseInt(UDPport);
        this.TCPSocket = TCPSocket;
        
        try {
            this.UDPsocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        InetSocketAddress addr = (InetSocketAddress) TCPSocket.getRemoteSocketAddress();
        this.playerIP = addr.getAddress().toString();
        this.UDPsocket = new DatagramSocket(Integer.parseInt(UDPport));
    }

    public String getPlayerID() {
        return (this.name);
    }
}
