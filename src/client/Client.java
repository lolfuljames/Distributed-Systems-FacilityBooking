/**
 * 
 */
package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import util.MonitorCallback;

/**
 * @author jame0019
 *
 */
public class Client {

	private String serverHostname = "localhost";
	private InetAddress serverAddress;
	private DatagramSocket socket = null;
	private int serverPort = 2222;
	private Scanner scanner = new Scanner(System.in);
	
	/**
	 * 
	 */
//	public Client() {
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	
	private void startClient() throws IOException {
		// TODO Auto-generated method stub
	      serverAddress = InetAddress.getByName(serverHostname);
	      System.out.println("Address: " + serverAddress.getHostAddress());
	      socket = new DatagramSocket();
	      while (true) {
	    	  sendCallback();
	      }
//	      byte[] m = args[0].getBytes();
//	      byte[] m = new String("abcdef").getBytes();
	      
//	      socket.receive(reply);
//	      System.out.println("Reply: " + new String(reply.getData()));
	}

	public void sendCallback() throws IOException {
		  console("What facility type would you like to be notified of? (Enter 0 to exit)");
		  int facilityType = scanner.nextInt(); scanner.nextLine();
		  if (facilityType == 0) {
			  return;
		  }
		  console("Which facility would you like to be notified of? (Enter 0 to exit)");
		  int facilityNumber = scanner.nextInt(); scanner.nextLine();
		  if (facilityNumber == 0) {
			  return;
		  }
		  console("Please enter duration of subscription. (Enter 0 to exit)");
		  int monitorInterval = scanner.nextInt(); scanner.nextLine();
		  if (monitorInterval == 0) {
			  return;
		  }
		  
	      MonitorCallback callback = new MonitorCallback(facilityType, facilityNumber, monitorInterval);
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(baos);
	      oos.writeObject(callback);
	      byte[] m = baos.toByteArray();
	      
	      DatagramPacket request = new DatagramPacket(m, m.length, serverAddress, serverPort);
	      socket.send(request);

	      byte[] buffer = new byte[1000];
	      DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
	      
	      socket.receive(reply);
	      System.out.println("Reply: " + new String(reply.getData()));
	}
	
	public void console(String message) {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n" + message);
	}
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client client = new Client();
		 try {
			 client.startClient();
		      
		    } catch (SocketException ex) {
		      System.out.println("Timeout error: " + ex.getMessage());
		      ex.printStackTrace();
		    } catch (IOException ex) {
		      System.out.println("IO error: " + ex.getMessage());
		      ex.printStackTrace();
		    } finally {
		      if(client.getSocket() != null)
		    	  client.getSocket().close();
		    }
	}
}


