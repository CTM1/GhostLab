import java.io.PrintWriter;

public class OGAME {
    private byte id;
    private byte nbOfPlayers;

    public OGAME(GameServer gs) {
        this.id = gs.getId();
        this.nbOfPlayers = gs.getNbOfPlayers();
    }

    public String toString() {
        return ("OGAME " + id + " " + nbOfPlayers + "***");
    }

    public void send(PrintWriter pw) {
        pw.write("OGAME ");
        pw.print(this.id);
        pw.flush();
    }
}