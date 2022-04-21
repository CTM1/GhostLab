package ghostlab;

/**
 * This class is used to emit all multicasted messages
 * a GameServer should send
 *
 * @since 21.04.2022
 */

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;

public class MulticastGameServer {
	InetAddress groupeIP;
	MulticastSocket socket;
	int port;

	public MulticastGameServer(String hostUDPport) throws Exception{
		// TODO convertir et niquer des races

		socket = new MulticastSocket();
		socket.setTimeToLive(15);
	}

	public void GHOST(int x, int y) {
		try {
			emit(String.format("GHOST %03d %03d+++", x, y));
		} catch (Exception e){
			Logger.log("NTM");
		}
	}

	public void SCORE(int id, int p, int x, int y) {
		try {
			emit(String.format("SCORE %08d %04d %03d %03d+++", id, p, x, y));
		} catch (Exception e){
			Logger.log("NTM");
		}
	}

	public void MESSA(int id, String mess) {
		try {
			emit(String.format("MESSA %08d %s+++", id, mess));
		} catch (Exception e){
			Logger.log("NTM");
		}
	}

	public void ENDGA(int id, int p) {
		try {
			emit(String.format("MESSA %08d %04d+++", id, p));
		} catch (Exception e){
			Logger.log("NTM");
		}
	}
	/* emit a message */
	private void emit(String mess) throws Exception {
		byte[] content;
		DatagramPacket message;
	
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 
		(new DataOutputStream(output)).writeUTF(mess); 
		content = output.toByteArray();
		message = new DatagramPacket(content, content.length, groupeIP, port);
		socket.send(message);
	}
}
