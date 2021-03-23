/**
 * 
 */
package server;

import java.net.*;
import java.util.*;
import java.io.*;

/**
 * @author jame0019
 *
 */
public class Server {

	/**
	 * 
	 */
	  private DatagramSocket socket;

	  public Server(int port) throws SocketException {
	    System.out.println("Initialising the socket for server..." + port);
	    socket = new DatagramSocket(port);
	  }
	  
	  private void service() throws IOException {
		    System.out.println("Servicing the requests...");
		    while (true) {
		      byte[] buffer = new byte[1000];
		      DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		      socket.receive(request);

		      // Do whatever is required here, process data etc
		      System.out.println("Received: " + new String(request.getData()));

		      // get client ip address and port
		      InetAddress clientAddr = request.getAddress();
		      int clientPort = request.getPort();

		      // prepare the datagram packet to response
		      DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddr, clientPort);
		      socket.send(response);
		    }
		  }

	/**
	 * @param args
	 */
	  public static void main(String[] args){
		    int port = 12345;

		    try {
		      Server server = new Server(port);
		      server.service();
		    } catch (SocketException ex) {
		      System.out.println("Socket error: " + ex.getMessage());
		    } catch (IOException ex) {
		      System.out.println("I/O error: " + ex.getMessage());
		    }
		  }

}
