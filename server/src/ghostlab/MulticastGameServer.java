package ghostlab;

/**
 * This class is used to emit all multicasted messages a GameServer should send
 *
 * @since 21.04.2022
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastGameServer {
  InetAddress groupeIP;
  int port;
  MulticastSocket socket;

  public MulticastGameServer(InetAddress addr, int udpPort) throws Exception {
    this.port = udpPort;
    this.groupeIP = addr;

    socket = new MulticastSocket(this.port);
    socket.setTimeToLive(15);
  }

  /* emit a message */
  private void emit(String mess) throws Exception {
    byte[] content;
    DatagramPacket message;

    Logger.verbose("MULTICAST> %s\n", mess);

    content = mess.getBytes();
    message = new DatagramPacket(content, content.length, groupeIP, port);
    socket.send(message);
  }

  public void GHOST(int x, int y) {
    try {
      emit(String.format("GHOST %03d %03d+++", x, y));
    } catch (Exception e) {
      Logger.log("Failed to send GHOST on Multicast");
    }
  }

  public void SCORE(String id, int p, int x, int y) {
    try {
      emit(String.format("SCORE %s %04d %03d %03d+++", id, p, x, y));
    } catch (Exception e) {
      Logger.log("Failed to send SCORE on Multicast");
    }
  }

  public void MESSA(String id, String mess) {
    try {
      emit(String.format("MESSA %s %s+++", id, mess));
    } catch (Exception e) {
      Logger.log("Failed to send MESSA on Multicast");
    }
  }

  public void ENDGA(String id, int p) {
    try {
      emit(String.format("ENDGA %s %04d+++", id, p));
    } catch (Exception e) {
      Logger.log("Failed to send ENDGA on Multicast");
    }
  }

  public int getPort() {
    return port;
  }
}
