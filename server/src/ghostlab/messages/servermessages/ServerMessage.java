package ghostlab.messages.servermessages;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class ServerMessage {
    public abstract void send(OutputStream os) throws IOException;

    public ByteBuffer intToBigEndian(int a) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(a);
        return buf;
    }

}
