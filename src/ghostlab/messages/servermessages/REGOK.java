package ghostlab.messages.servermessages;


public class REGOK {
    private byte matchID;
    
    public REGOK(byte m) {
        this.matchID = m;    
    }

    public String toString() {
        return ("REGOK " + this.matchID + "***");
    }
}