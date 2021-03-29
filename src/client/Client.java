/**
 * 
 */
package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import server.Day;
import server.Time;
import server.TimeErrorException;
import utils.*;

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
	 * Initializes client's socket and enters main menu.
	 * 
	 * @throws IOException - Unable to reach server.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws TimeErrorException 
	 * 
	 */
	private void startClient() throws IOException, IllegalArgumentException, IllegalAccessException, TimeErrorException {
		// TODO Auto-generated method stub
		serverAddress = InetAddress.getByName(serverHostname);
		System.out.println("Address: " + serverAddress.getHostAddress());
		socket = new DatagramSocket();
		String inputStr;
		while (true) {
			console("Welcome to the NTU Facility Booking System!\n" + "0 - Query Facility Availability\n"
					+ "1 - Book Facility\n" + "2 - Amend Existing Bookings\n" + "3 - Monitor Facility Bookings\n"
					+ "Please enter your intended actions: ");
			inputStr = scanner.nextLine();
			int opCode;
			try {
				opCode = Integer.parseInt(inputStr);
			} catch (NumberFormatException ne) {
				console("Invalid action selected! Press enter to continue...");
				scanner.nextLine();
				continue;
			}
			switch (opCode) {
			case 0:
//		    	  queryFacility();
				break;
			case 1:
				Message requestMessage = new Message(new Header(UUID.randomUUID(), 1, 0),
						new MakeBookingReqBody("LT-1", Day.MONDAY, new Time(9, 0), new Time(12, 0)));
				sendMessage(requestMessage, this.serverAddress, this.serverPort);
				DatagramPacket packet = receivePacket();
//		    	  makeBooking();
				break;
			case 2:
//		    	  amendBooking();
				break;
			case 3:
				monitorFacility();
				break;
			default:
				console("Invalid action selected! Press enter to continue...");
				scanner.nextLine();
				break;
			}
		}
//	      byte[] m = args[0].getBytes();
//	      byte[] m = new String("abcdef").getBytes();

//	      socket.receive(reply);
//	      System.out.println("Reply: " + new String(reply.getData()));
	}

	/**
	 * 
	 * Perform monitoring of facility, prompt users for facility input.
	 * 
	 * @throws IOException - Unable to reach server.
	 */
	private void monitorFacility() throws IOException {
		console("What facility type would you like to be notified of? (Enter 0 to exit)");
		String facilityType = scanner.nextLine();
		if (facilityType == "0") {
			return;
		}
		console("Which facility would you like to be notified of? (Enter 0 to exit)");
		String facilityID = scanner.nextLine();
		if (facilityID == "0") {
			return;
		}
		console("Please enter duration of subscription. (Enter 0 to exit)");
		String inputStr = scanner.nextLine();
		int monitorInterval;
		try {
			monitorInterval = Integer.parseInt(inputStr);
		} catch (NumberFormatException ne) {
			console("Invalid duration entered! Default value of 10 minutes is set... (Press enter to continue)");
			scanner.nextLine();
			monitorInterval = 10;
		}

		MonitorCallback callback = new MonitorCallback(facilityType, facilityID, monitorInterval);
//		sendCallback(callback);
	}

	/**
	 * 
	 * Performs sending and acknowledgement checking on callbacks. Waits for
	 * incoming notifications regarding monitored facility, until callback is
	 * expired.
	 * 
	 * @param callback - MonitorCallback object that contains the monitored
	 *                 facility.
	 * @throws IOException - Unable to reach server.
	 */
//	public void sendCallback(MonitorCallback callback) throws IOException {
//		Message message = new Message(new Header(UUID.randomUUID(), 3, 0), new Body())
//		sendMessage(, this.serverAddress, this.serverPort);
//		DatagramPacket test = receiveMessage();
//
////		check for acknowledgment on callback
//		while (!(new String(reply.getData()).trim()).equals("ACK_CALLBACK")) {
////		while (!(new String(reply.getData()).trim()).equals(CallbackStatus.ACK_CALLBACK.name())) {
//			socket.send(request);
//			buffer = new byte[1000];
//			reply = new DatagramPacket(buffer, buffer.length);
//			socket.receive(reply);
//		}
//
////		7 days timeout
//		buffer = new byte[1000];
//		reply = new DatagramPacket(buffer, buffer.length);
//		socket.receive(reply);
//		socket.setSoTimeout(7 * 24 * 60 * 1000);
//		while (!(new String(reply.getData()).trim()).equals("EXPIRED_CALLBACK")) {
////		while (!(new String(reply.getData()).trim()).equals(CallbackStatus.EXPIRED_CALLBACK.name())) {
//			System.out.println("Reply: " + new String(reply.getData()));
//			buffer = new byte[1000];
//			reply = new DatagramPacket(buffer, buffer.length);
//			socket.receive(reply);
//		}
//
//		System.out.println("End of process...");
//		return;
////	}

	public void console(String message) {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n" + message);
	}

	public DatagramSocket getSocket() {
		return socket;
	}
	
	private void sendMessage(Message message, InetAddress clientAddr, int clientPort) throws IOException, IllegalArgumentException, IllegalAccessException {
		ByteBuffer buf = ByteBuffer.allocate(2048);
		buf = Serializer.serialize(message, buf);
		DatagramPacket response = new DatagramPacket(buf.array(), buf.capacity(), clientAddr, clientPort);
		socket.send(response);
	}

	private DatagramPacket receivePacket() throws IOException {
		byte[] buf = new byte[2048];
		DatagramPacket request = new DatagramPacket(buf, buf.length);
		socket.receive(request);
		return request;
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		try {
			while (true) client.startClient();
		} catch (SocketException ex) {
			System.out.println("Timeout error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("IO error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (client.getSocket() != null)
				client.getSocket().close();
		}
	}
}
