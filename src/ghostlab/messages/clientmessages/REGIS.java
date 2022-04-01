package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.InputStream;

public class REGIS {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!
    private byte gameID;

    public REGIS() {}

    public REGIS(InputStream in) {
        //this.playerID = id;
        //this.port = port;
        //this.gameID = m;

    }
}