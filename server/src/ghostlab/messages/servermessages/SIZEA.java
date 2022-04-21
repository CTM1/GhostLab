package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;

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
        os.write("SIZE! ".getBytes());
        os.write(this.id);
        os.write(" ".getBytes());
        os.write(this.h);
        os.write(" ".getBytes());
        os.write(this.w);
        os.write("***".getBytes());
        os.flush();
    }
}