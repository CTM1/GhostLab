package ghostlab.messages.ClientMessages;

import java.io.BufferedReader;

public class REGIS {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!
    private byte gameID;

    public REGIS() {}

    public REGIS(BufferedReader br) {
        //this.playerID = id;
        //this.port = port;
        //this.gameID = m;
    }
}