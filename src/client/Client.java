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
import java.time.Instant;

/**
 * @author jame0019
 *
 */
public class Client {

	private String serverHostname = "localhost";
	private InetAddress serverAddress;
	private DatagramSocket socket = null;
	private int serverPort = 2222;
	private static Scanner scanner = new Scanner(System.in);
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
	 * @throws InterruptedException 
	 * 
	 */
	private void startClient() throws IOException, IllegalArgumentException, IllegalAccessException, TimeErrorException, InterruptedException {
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
			Message requestMessage = null;
			ArrayList<String> args = new ArrayList<String>();
			switch (opCode) {
			case 0:
		    	requestMessage = this.queryFacilityAvailability(args);
				break;
			case 1:
		    	requestMessage = this.makeBooking(args);
				break;
			case 2:
		    	requestMessage = this.amendBooking(args);
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
			this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
			Message responseMessage = this.receiveMessage();
			System.out.println(responseMessage);
			backToMain();
		}
	}
	
	private Message amendBooking(ArrayList<String> args) throws IllegalArgumentException {
		System.out.println("Please enter the booking ID.");
		UUID bookingID = UUID.fromString(scanner.nextLine());
		
		System.out.println("Please enter the offset (in minutes) that you wish to advance/postpone. (negative number to advance, positive number to postpone)");
		int offset = Integer.parseInt(scanner.nextLine());

		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.AMEND_BOOKING, Constants.REQUEST),
				new AmendBookingReqBody(bookingID, offset));
		
		return requestMessage;
	}

	private Message makeBooking(ArrayList<String> args) throws IOException, IllegalArgumentException, IllegalAccessException, TimeErrorException {
		ArrayList<String> facilityTypes = this.queryFacilityTypes();
		args.add("Please choose the facility of interest.");
		for (String facilityType : facilityTypes) {
			args.add(facilityType);
		}
		menu(args);
		
		String facilityName = scanner.nextLine().toUpperCase();
		args.clear();
		args.add("Please enter the day of interest.");
		Arrays.asList(Day.values()).forEach(day -> {
			args.add(day.toString());
		});
		menu(args);
		
		String dayInput = scanner.nextLine().toUpperCase();
		Day selectedDay = Day.valueOf(dayInput);
		ArrayList<Day> days = new ArrayList<Day>();
		days.add(selectedDay);
		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.QUERY_AVAILABILITY, Constants.REQUEST),
				new QueryAvailabilityReqBody(days, "", facilityName, false));
		this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
		Message responseMessage = this.receiveMessage();
		QueryAvailabilityRespBody respBody = (QueryAvailabilityRespBody) responseMessage.getBody();
		if (respBody.getErrorMessage() != null) {
			System.out.println(respBody.getErrorMessage());
		} else {
			System.out.println(respBody.getPayLoad());
		}
		
		System.out.println("Please enter the desired facility ID.");
		String facilityID = scanner.nextLine().toUpperCase();
		
		System.out.println("Please enter start time. (in HH:MM format)");
		String[] startTimeInput = scanner.nextLine().split(":");
		Time startTime = new Time(Integer.parseInt(startTimeInput[0]), Integer.parseInt(startTimeInput[1]));
		
		System.out.println("Please enter end time. (in HH:MM format)");
		String[] endTimeInput = scanner.nextLine().split(":");
		Time endTime = new Time(Integer.parseInt(endTimeInput[0]), Integer.parseInt(endTimeInput[1]));

		requestMessage = new Message(new Header(UUID.randomUUID(), Constants.MAKE_BOOKING, Constants.REQUEST),
				new MakeBookingReqBody(facilityID, selectedDay, startTime, endTime));
		
		return requestMessage;
	}

	private Message queryFacilityAvailability(ArrayList<String> args) throws IllegalArgumentException, IllegalAccessException, IOException {
		ArrayList<String> facilityTypes = this.queryFacilityTypes();
		args.add("Please enter the facility of interest.");
		for (String facilityType : facilityTypes) {
			args.add(facilityType);
		}
		menu(args);
		
		String facilityType = scanner.nextLine().toUpperCase();
		args.clear();
		ArrayList<String> facilityIDs = this.queryFacilityIDs(facilityType);
		args.add("Please enter the facility ID of interest.");
		for (String facilityID : facilityIDs) {
			args.add(facilityID);
		}
		menu(args);
		
		String facilityID = scanner.nextLine().toUpperCase();
		args.clear();
		args.add("Please choose the day of interest. (Separate day by a white space if querying for multiple days)");
		Arrays.asList(Day.values()).forEach(day -> {
			args.add(day.toString());
		});
		menu(args);
		
		String[] daysInput = scanner.nextLine().toUpperCase().split(" ");
		ArrayList<Day> days = new ArrayList<Day>();
		for (String day : daysInput) {
			days.add(Day.valueOf(day));
		}
		
		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.QUERY_AVAILABILITY, Constants.REQUEST),
				new QueryAvailabilityReqBody(days, facilityID, "", true));
		
		return requestMessage;
		
	}

	private ArrayList<String> queryFacilityIDs(String facilityType) throws IllegalArgumentException, IllegalAccessException, IOException {
		Header header = new Header(UUID.randomUUID(), Constants.QUERY_FACILITY_IDS, Constants.REQUEST);
		Body reqBody = new QueryFacilityIDsReqBody(facilityType);
		Message requestMessage = new Message(header, reqBody);
		this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
		Message responseMessage = this.receiveMessage();
		QueryFacilityIDsRespBody respBody = (QueryFacilityIDsRespBody) responseMessage.getBody();
		return respBody.getFacilityIDs();
	}

	private ArrayList<String> queryFacilityTypes() throws IllegalArgumentException, IllegalAccessException, IOException {
		Header header = new Header(UUID.randomUUID(), Constants.QUERY_FACILITY_TYPES, Constants.REQUEST);
		Body reqBody = new QueryFacilityTypesReqBody();
		Message requestMessage = new Message(header, reqBody);
		this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
		Message responseMessage = this.receiveMessage();
		QueryFacilityTypesRespBody respBody = (QueryFacilityTypesRespBody) responseMessage.getBody();
		return respBody.getFacilityTypes();
	}

	/**
	 * 
	 * Perform monitoring of facility, prompt users for facility input.
	 * 
	 * @throws IOException - Unable to reach server.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void monitorFacility() throws IOException, IllegalArgumentException, IllegalAccessException {
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
		registerCallback(callback);
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
//		one second timeout for ACK, check for ACK on callback
		while (true) {
			sendMessage(requestMessage, serverAddress, serverPort);
			try {
				responseMessage = receiveMessage();
				data = ((MonitorAvailabilityRespBody) responseMessage.getBody()).getPayload();
				if (data.equals("ACK_CALLBACK")) break;
			} catch (IOException ex) {
				System.out.println("ACK_CALLBACK not received! Requesting again...");
			}
		}

		long callbackEndTime = Instant.now().getEpochSecond() + callback.getMonitorInterval()*60;
		
		try {
			while (true) {
//				set to timeout until callback ends
				socket.setSoTimeout((int) ((callbackEndTime - Instant.now().getEpochSecond()) * 1000));
				responseMessage = receiveMessage();
				data = ((MonitorAvailabilityRespBody) responseMessage.getBody()).getPayload();
				System.out.println(data);
			}
		} catch (IOException ex) {
			console("Monitor Interval has ended... Exiting... (press Enter to continue)");
			scanner.nextLine();
		}
		return;
	}
	
	public void menu(ArrayList<String> args) {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n");
		System.out.println(args.get(0));
		for (int i=1; i<args.size(); i++) {
			System.out.println(String.format(" - %s", args.get(i)));
		}
	}
	
	public static void backToMain() throws InterruptedException {
		Thread.sleep(500);
		System.out.println("Press enter to return to continue...");
		scanner.nextLine();
	}

	public void console(String message) {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + message);
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
		while (true) {
			try {
				client.startClient();
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
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (client.getSocket() != null)
					client.getSocket().close();
			}
			try {
				backToMain();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
