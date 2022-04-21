package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

public class START {

    public static START parse(BufferedReader br) throws IOException {
        for (int i = 0; i < 3; i++)
            br.read();

        return new START();
    }

    public String toString() {
        return("START***");
    }
}