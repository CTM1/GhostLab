package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import ghostlab.GameServer;
import ghostlab.LabyrInterface;

public class SIZEA implements ServerMessage {
    private byte id;
    private char h;
    private char w;

    public SIZEA(GameServer gs) {
        this.id = gs.getId();
        LabyrInterface l = gs.getLabyrinth();
        this.w = l.getWidth();
        this.h = l.getHeight();
    }

    public String toString() {
        return ("SIZE! " + this.id + " " +
                this.h + " " + this.w + "***");
    }

    public void send(OutputStream os) throws IOException {
        // System.out.println("Sending size "+this.h+"*"+this.w+" for game "+this.id);
        os.write("SIZE! ".getBytes());
        os.write(this.id);
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
        os.write("***".getBytes());
        os.flush();
    }
}