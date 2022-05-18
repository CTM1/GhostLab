package ghostlab.messages.clientmessages;

import java.io.BufferedReader;
import java.io.IOException;

public class SENDQ {
  private String id;
  private String message;

  public static SENDQ parse(BufferedReader br) throws IOException {
    for (int i = 0; i < 3; i++) br.read();

    char read = '\0';
    StringBuilder id_data = new StringBuilder(128);
    StringBuilder mess_data = new StringBuilder(200);

    for(int i = 0; i < 8; i++) { id_data.append(br.read()); } // read ID

    // FIXME this will break if a player sends a message containing an asterisk
    while(read != '*') {
	read = (char)br.read();
	mess_data.append(br.read());
    }

    return new SENDQ(id_data.toString(), mess_data.toString());
  }

  public SENDQ(String id, String mess) {
	  this.id = id;
	  this.message = mess;
  }

  public String getID() { return id; }
  public String getMessage() { return message; }

  public String toString() {
    return String.format("SEND? %s %s***", id, message);
  }
}
