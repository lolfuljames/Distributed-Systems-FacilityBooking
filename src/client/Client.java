/**
 * 
 */
package client;

import java.io.*;
import java.net.*;
/**
 * @author jame0019
 *
 */
public class Client {

	
	/**
	 * 
	 */
//	public Client() {
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String hostname = "localhost";
	    DatagramSocket socket = null;
	    int port = 12351;
		 try {
		      InetAddress address = InetAddress.getByName(hostname);
		      System.out.println("Address: " + address.getHostAddress());
		      socket = new DatagramSocket();

//		      byte[] m = args[0].getBytes();
		      byte[] m = new String("abcdef").getBytes();
		      DatagramPacket request = new DatagramPacket(m, m.length, address, port);
		      socket.send(request);

		      byte[] buffer = new byte[1000];
		      DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
		      socket.receive(reply);
		      System.out.println("Reply: " + new String(reply.getData()));
		      
		    } catch (SocketTimeoutException ex) {
		      System.out.println("Timeout error: " + ex.getMessage());
		      ex.printStackTrace();
		    } catch (IOException ex) {
		      System.out.println("IO error: " + ex.getMessage());
		      ex.printStackTrace();
		    } finally {
		      if(socket != null)
		        socket.close();
		    }
	}

}
