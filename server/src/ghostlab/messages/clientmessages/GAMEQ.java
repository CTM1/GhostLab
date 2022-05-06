package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;

public class GAMEQ {

  public static GAMEQ parse(BufferedReader br) throws IOException {
    for (int i = 0; i < 3; i++) br.read();

    return new GAMEQ();
  }

  public String toString() {
    return ("GAME?***");
  }
}
