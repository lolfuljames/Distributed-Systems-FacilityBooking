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
import utils.message.request.*;
import utils.message.response.*;
import utils.callback.MonitorCallback;
import utils.message.Body;
import utils.message.Header;
import utils.message.Message;
import utils.serialize.Deserializer;
import utils.serialize.Serializer;

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
	private InetAddress clientAddress;
	private int clientPort;
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
		clientAddress = InetAddress.getLocalHost();
		clientPort = socket.getLocalPort();
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
			Message requestMessage;
			switch (opCode) {
			case 0:
				String[] args = {"Please choose the facility of interest.", "LT", "TR", "LAB"};
				menu(args);
				String facilityName = scanner.nextLine().toUpperCase();
//				String[] 
//				args = {"Please choose the day of interest. (Case sensitive)", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
				ArrayList<Day> days = new ArrayList<Day>();
				days.add(Day.MONDAY);
				
				requestMessage = new Message(new Header(UUID.randomUUID(), Constants.QUERY_AVAILABILITY, Constants.REQUEST),
						new QueryAvailabilityReqBody(days, facilityName));
//		    	  queryFacility();
				break;
			case 1:
				String facilityID = null;
				Day day = Day.MONDAY;
				Time startTime = new Time(9, 0);
				Time endTime = new Time(12, 0);
				
				requestMessage = new Message(new Header(UUID.randomUUID(), Constants.MAKE_BOOKING, Constants.REQUEST),
						new MakeBookingReqBody("LT-1", Day.MONDAY, startTime, endTime));
				sendMessage(requestMessage, this.serverAddress, this.serverPort);
//				DatagramPacket packet = receivePacket();
//		    	  makeBooking();
				break;
			case 2:
				UUID bookingID = UUID.randomUUID();
				int offset = 0;
				
				requestMessage = new Message(new Header(UUID.randomUUID(), Constants.AMEND_BOOKING, Constants.REQUEST),
						new AmendBookingReqBody(bookingID, offset));
//		    	  amendBooking();
				break;
			case 3:
				MonitorCallback callback = new MonitorCallback("LT-1", 5, InetAddress.getByName("google.com"), 2);
				
				requestMessage = new Message(new Header(UUID.randomUUID(), Constants.MONITOR_AVAILABILITY, Constants.REQUEST),
						new MonitorAvailabilityReqBody(callback));
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
		
//		TODO send query 
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
		
		
		MonitorCallback callback = new MonitorCallback(facilityID, monitorInterval, this.clientAddress, this.clientPort);
//		registerCallback(callback);
	}

	/**
	 * 
	 * Performs registering and acknowledgement checking on callbacks. Waits for
	 * incoming notifications regarding monitored facility, until callback is
	 * expired.
	 * 
	 * @param callback - MonitorCallback object that contains the monitored
	 *                 facility.
	 * @throws IOException - Unable to reach server.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public void registerCallback(MonitorCallback callback) throws IOException, IllegalArgumentException, IllegalAccessException {
		Header header = new Header(UUID.randomUUID(), 3, 0);
		Body body = new MonitorAvailabilityReqBody(callback);
		Message requestMessage = new Message(header, body);
		Message responseMessage;
		String data;
		
		socket.setSoTimeout(1000);
//		one second timeout for ack
//		check for acknowledgment on callback
		while (true) {
			sendMessage(requestMessage, serverAddress, serverPort);
			try {
				responseMessage = receiveMessage();
				data = ((MonitorAvailabilityRespBody) responseMessage.getBody()).getPayload();
				if (data.equals("ACK_CALLBACK")) break;
			} catch (IOException ex) {
				sendMessage(requestMessage, serverAddress, serverPort);
			}
		}

		socket.setSoTimeout(7 * 24 * 60 * 1000);
//		7 days timeout
		responseMessage = receiveMessage();
		data = ((MonitorAvailabilityRespBody) responseMessage.getBody()).getPayload();
		while (!data.equals("EXPIRED_CALLBACK")) {
			responseMessage = receiveMessage();
			data = ((MonitorAvailabilityRespBody) responseMessage.getBody()).getPayload();
		}


		System.out.println("End of process...");
		return;
	}
	
	public void menu(String[] args) {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n");
		System.out.println(args[0]);
		for (int i=1; i<args.length; i++) {
			System.out.println(String.format(" - %s", args[i]));
		}
	}

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

	private Message receiveMessage() throws IOException {
		byte[] buf = new byte[2048];
		DatagramPacket request = new DatagramPacket(buf, buf.length);
		socket.receive(request);
		byte[] byteBuffer = request.getData();
		ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);
		Message requestMessage = Deserializer.deserialize(buffer, Message.class);
		return requestMessage;
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
