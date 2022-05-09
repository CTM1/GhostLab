package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;

public class MovementMessage {
  /** Used by movement message parsers to get the distance. Boilerplate code */
  public static int parseDistance(BufferedReader br) throws IOException {
    br.read(); // space
    String dist = "";
    for (int i = 0; i < 3; i++) {
      dist += (char) br.read();
    }

    int d = Integer.parseInt(dist);
    return d;
  }

  public static void getMsgTail(BufferedReader br) throws IOException {
    for (int i=0; i<3; i++)
      br.read();
  }
}
