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
    
    public Player(int id, String name, String UDPport, Socket TCPSocket) throws SocketException {
        this.name = name;
        this.id = id;
        this.UDPport = Integer.parseInt(UDPport);
        this.TCPSocket = TCPSocket;
        
        InetSocketAddress addr = (InetSocketAddress) TCPSocket.getRemoteSocketAddress();
        this.playerIP = addr.getAddress().toString();
    }

    public String getPlayerID() {
        return (this.name);
    }

    public void setPos(int x, int y) {
	this.x = x;
	this.y = y;
    }
}
