package ghostlab;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class Player {
  int x;
  int y;
  int id;
  int score;
  int UDPport;
  String name;
  String playerIP;
  Socket TCPSocket;

  public Player(int id, String name, String UDPport, Socket TCPSocket) throws SocketException {
    this.name = name;
    this.id = id;
    this.UDPport = Integer.parseInt(UDPport);
    this.TCPSocket = TCPSocket;
    score = 0;

    InetSocketAddress addr = (InetSocketAddress) TCPSocket.getRemoteSocketAddress();
    this.playerIP = addr.getAddress().toString();
  }

  public void propagateMessage(String from, String mess) {
    try {
      Logger.log(String.format("[*] Whisper from %s to %s : %s\n", from, name, mess));
      byte[] content = String.format("MESSP %s %s+++", from, mess).getBytes();
      DatagramSocket socket = new DatagramSocket();
      InetAddress a = this.TCPSocket.getInetAddress();

      DatagramPacket p = new DatagramPacket(content, content.length, a, this.UDPport);
      socket.send(p);
      socket.close();
    } catch (Exception e) {
      Logger.verbose("[-] Error sending private message on UDP\n");
    }
  }

  public String toString() {
    return this.name;
  }

  public String getPlayerID() {
    return (this.name);
  }

  public int getScore() {
    return score;
  }

  public void addToScore(int x) {
    score += x;
  }

  public void setPos(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getUDPport() {
    return this.UDPport;
  }

  public Socket getTCPSocket() {
    return this.TCPSocket;
  }
}
