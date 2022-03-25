import java.io.PrintWriter;

public class REGOK {
    private byte matchID;
    
    public REGOK(byte m) {
        this.matchID = m;    
    }

    public String toString() {
        return ("REGOK " + this.matchID + "***");
    }
}