package ghostlab.messages.ClientMessages;

import java.io.BufferedReader;

public class NEWPL {
    private String playerID; // 8 chars or fuck off!
    private String port; // 4 chars or fuck off!

    public NEWPL() {}

    public NEWPL(BufferedReader br) {
        //this.playerID = i;
        //this.port = p;
    }
}