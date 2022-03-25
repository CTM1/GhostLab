import java.io.PrintWriter;

public class GAMES {
    private byte n;

    public GAMES(byte n) {
        this.n = n;
    }

    public String toString() {
        return("GAMES " + Byte.toUnsignedInt(this.n) + "***");
    }
    
    public void send(PrintWriter pw) {
        pw.write("GAMES ");
        pw.print(this.n);
        pw.write("***");
        pw.flush();
    }
}