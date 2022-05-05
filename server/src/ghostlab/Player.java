package ghostlab;

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
}
