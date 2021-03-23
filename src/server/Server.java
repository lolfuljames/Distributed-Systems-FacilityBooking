/**
 * 
 */
package server;
import java.net.*;
import java.util.*;
import java.io.*;
import util.*;
import java.rmi.RemoteException;
/**
 * @author jame0019
 *
 */
public class Server implements CallbackServer{

	/**
	 * 
	 */
	  private DatagramSocket socket;
	  private List<Callback> callbacks = new ArrayList<>();

	  public Server(int port) throws SocketException {
	    System.out.println("Initialising the socket for server..." + port);
	    socket = new DatagramSocket(port);
	  }
	  
	  private void service() throws IOException, ClassNotFoundException {
		    System.out.println("Servicing the requests...");
		    while (true) {
		      byte[] buffer = new byte[1000];
		      DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		      socket.receive(request);
		      
	          ByteArrayInputStream baos = new ByteArrayInputStream(buffer);
	          ObjectInputStream oos = new ObjectInputStream(baos);
	          Callback test_callback = (Callback)oos.readObject();
	          
		      addCallback(test_callback);
		      notifyRegistered("Received with thanks");
		      // Do whatever is required here, process data etc
//		      System.out.println("Received: " + new String(request.getData()));

		      // get client ip address and port
		      InetAddress clientAddr = request.getAddress();
		      int clientPort = request.getPort();

		      // prepare the datagram packet to response
		      DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddr, clientPort);
//		      socket.send(response);
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
		    } catch (ClassNotFoundException ex) {
		    	System.out.println("Class not found error: " + ex.getMessage());
		    }
	  }
	  
	  public void getBooking() {}
	  public void addCallback(Callback callback) {
		  callbacks.add(callback);
	  }
	  public void removeCallback(Callback callback) {
		  callbacks.remove(callback);
	  }
	  
	  private void notifyRegistered(String message) {
		  callbacks.forEach(callback -> {
			  try {
				  callback.notify(message);
			  }
			  catch (RemoteException re) {
				  System.out.print("RemoteException occured!");
			  }
		  });
	  }

}
