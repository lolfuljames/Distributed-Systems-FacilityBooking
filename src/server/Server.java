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
	  private List<MonitorCallback> callbacks = new ArrayList<>();

	  public Server(int port) throws SocketException {
	    System.out.println("Initialising the socket for server..." + port);
	    socket = new DatagramSocket(port);
	  }
	  
	  private void service() throws IOException, ClassNotFoundException, RemoteException {
		    System.out.println("Servicing the requests...");
		    while (true) {
		      byte[] buffer = new byte[1000];
		      DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		      socket.receive(request);
		      
	          ByteArrayInputStream baos = new ByteArrayInputStream(buffer);
	          ObjectInputStream oos = new ObjectInputStream(baos);
	          MonitorCallback test_callback = (MonitorCallback)oos.readObject();
	          
		      addCallback(test_callback);
		      notifyRegistered("Received with thanks");
		      // Do whatever is required here, process data etc
//		      System.out.println("Received: " + new String(request.getData()));
		      updateMonitorInterval();
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
		    int port = 2222;

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
	  public void addCallback(MonitorCallback callback) {
		  callbacks.add(callback);
	  }
	  public void removeCallback(MonitorCallback callback) {
		  callbacks.remove(callback);
	  }
	  
	  private void notifyRegistered(String message) throws RemoteException {
		  callbacks.forEach(callback -> {
			  try {
				  callback.notify(message);
			  }
			  catch (RemoteException re) {
				  re.printStackTrace();
			  }
		  });
	  }
	  
	  private void updateMonitorInterval() {
		  List<MonitorCallback> expiredCallbacks = new ArrayList<>();
		  callbacks.forEach(callback -> {
			  callback.setMonitorInterval(callback.getMonitorInterval()-1);
			  if (callback.getMonitorInterval() == 0) {
				  expiredCallbacks.add(callback);
			  }
		  });
		  expiredCallbacks.forEach(callback -> callbacks.remove(callback))
		  ;
	  }

}
