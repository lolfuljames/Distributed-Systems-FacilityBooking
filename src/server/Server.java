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

		      // get client ip address and port
		      InetAddress clientAddr = request.getAddress();
		      int clientPort = request.getPort();
		      
		      System.out.println("Received");

		      handleCallback(request);
		      while (callbacks.size() > 0) {
			      updateMonitorInterval();
			      notifyAllCallbacks("Received with thanks");
		      }
		      // Do whatever is required here, process data etc
//		      System.out.println("Received: " + new String(request.getData()));
		      System.out.println("Waiting for new requests");
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
	  
		/**
		 * 
		 * Main callback handler, logs client's IP and port.
		 * @param request - DatagramPacket sent from client.
		 * @throws IOException - Unable to reach client.
		 */
	  public void handleCallback(DatagramPacket request) throws IOException, ClassNotFoundException {
	      // parse inputstream
		  byte[] data = request.getData();
		  InetAddress clientAddr = request.getAddress();
		  int clientPort = request.getPort();
		  
          ByteArrayInputStream baos = new ByteArrayInputStream(request.getData());
          ObjectInputStream oos = new ObjectInputStream(baos);
          MonitorCallback newCallback = (MonitorCallback) oos.readObject();
          newCallback.setAddress(clientAddr);
          newCallback.setPort(clientPort);
	      addCallback(newCallback);
	  }
	  
	  public void getBooking() {}
	  
		/**
		 * 
		 * Handler to add callback to registered list, sends ACK to client.
		 * @param callback - MonitorCallback object sent from client.
		 * @throws IOException - Unable to reach client.
		 */
	  public void addCallback(MonitorCallback callback) throws IOException {
		  byte[] buffer = CallbackStatus.ACK_CALLBACK.getBytes();
		  DatagramPacket reply = new DatagramPacket(buffer, buffer.length, callback.getAddress(), callback.getPort());
		  socket.send(reply);
		  callbacks.add(callback);
	  }
	  
		/**
		 * 
		 * Handler to remove callback from registered list,
		 * sends EXPIRED_CALLBACK to client.
		 * @param callback - MonitorCallback object to be removed.
		 * @throws IOException - Unable to reach client.
		 */
	  public void removeCallback(MonitorCallback callback) throws IOException {
		  byte[] buffer = CallbackStatus.EXPIRED_CALLBACK.getBytes();
		  DatagramPacket reply = new DatagramPacket(buffer, buffer.length, callback.getAddress(), callback.getPort());
		  socket.send(reply);
		  callbacks.remove(callback);
	  }
	  
		/**
		 * 
		 * Handler for sending messages to client.
		 * @param callback - MonitorCallback object of the registered client.
		 * @param message - message to be sent
		 * @throws IOException - Unable to reach client.
		 */
	  private void notifyCallback(MonitorCallback callback, String message) throws IOException {
			message = message + "\n" + "facility: " + callback.getMonitorFacilityType() + " timeleft: " + callback.getMonitorInterval();
		  	byte[] buffer = message.getBytes();
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length, callback.getAddress(), callback.getPort());
			socket.send(reply);
		};
	
		
		/**
		 * 
		 * Handler for sending messages to all clients.
		 * @param message - message to be sent
		 * @throws IOException - Unable to reach client.
		 */
	  private void notifyAllCallbacks(String message) throws RemoteException {
		  callbacks.forEach(callback -> {
			  try {
				  notifyCallback(callback, message);
			  }
			  catch (RemoteException re) {
				  re.printStackTrace();
			  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  });
	  }
	  
	  
		/**
		 * 
		 * -1 day simulator, removes if days left is 0.
		 */
	  private void updateMonitorInterval() {
		  List<MonitorCallback> expiredCallbacks = new ArrayList<>();
		  callbacks.forEach(callback -> {
			  callback.setMonitorInterval(callback.getMonitorInterval()-1);
			  if (callback.getMonitorInterval() == 0) {
				  expiredCallbacks.add(callback);
			  }
		  });
		  expiredCallbacks.forEach(callback -> {
			  try {
				  removeCallback(callback);
			  }
			  catch (IOException re) {
				  throw new RuntimeException(re);
			  }
		  });
	  }
	  

}
