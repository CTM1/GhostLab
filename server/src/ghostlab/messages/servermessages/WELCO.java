package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

public class WELCO implements ServerMessage {
  String cnt;
  byte m;
  char h;
  char w;
  byte f;
  String ip;
  String port;

  public WELCO(byte m, char h, char w, byte f, String ip, String port) {
    this.m = m;
    this.h = h;
    this.w = w;
    this.f = f;
    this.ip = ip;
    this.port = port;
  }

  public void send(OutputStream os) throws IOException {
    os.write("WELCO ".getBytes());
    os.write(m);
    os.write(" ".getBytes());

    byte[] h = new byte[2];
    h[0] = (byte)(this.h & 0xFF);
    h[1] = (byte)((this.h >> 8) & 0xFF);
    os.write(h);
    os.write(" ".getBytes());

    byte[] w = new byte[2];
    w[0] = (byte)(this.w & 0xFF);
    w[1] = (byte)((this.w >> 8) & 0xFF);
    os.write(w);
    os.write(" ".getBytes());

    os.write(f);
    os.write(" ".getBytes());
    os.write(ip.substring(1, ip.length()).getBytes());
    os.write(" ".getBytes());
    os.write(port.getBytes());
    os.write("***".getBytes());
  }
}
