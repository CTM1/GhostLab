package ghostlab;

import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class Player {
    int x;
    int y;
    int id;
    int UDPport;
    String name;
    String playerIP;
    InetSocketAddress addr;
    DatagramSocket UDPsocket;

    public Player(String name, String UDPport, InetSocketAddress playerAddr)
    {
        this.name = name;
        this.UDPport = Integer.parseInt(UDPport);
        
        try {
            this.UDPsocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String playerIP = playerAddr.getAddress().getHostAddress();
    }

    public String getPlayerID() {
        return (this.name);
    }
}
