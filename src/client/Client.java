/**
 * 
 */
package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
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
	private static boolean DEBUG = false;
	private Random rand = new Random();
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
		ArrayList<String> menuArgs = new ArrayList<String>();
		menuArgs.add("Welcome to the NTU Facility Booking System!");
		menuArgs.add(String.format("%d | Query Facility Availability", Constants.QUERY_AVAILABILITY));
		menuArgs.add(String.format("%d | Book Facility", Constants.MAKE_BOOKING));
		menuArgs.add(String.format("%d | Amend Existing Booking", Constants.AMEND_BOOKING));
		menuArgs.add(String.format("%d | Monitor Facility Bookings", Constants.MONITOR_AVAILABILITY));
		menuArgs.add(String.format("%d | Cancel Existing Booking", Constants.CANCEL_BOOKING));
		menuArgs.add(String.format("%d | Extend Existing Booking", Constants.EXTEND_BOOKING));
		boolean awaitReceiveMessage;
		while (true) {
			awaitReceiveMessage = false;
			menu(menuArgs);
			System.out.println("Please enter your intended actions: ");
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
			case Constants.QUERY_AVAILABILITY:
		    	requestMessage = this.queryFacilityAvailability(args);
		    	if (requestMessage == null) {
					awaitReceiveMessage = false;
		    	} else {
					awaitReceiveMessage = true;
		    	}
				break;
			case Constants.MAKE_BOOKING:
		    	requestMessage = this.makeBooking(args);
		    	if (requestMessage == null) {
					awaitReceiveMessage = false;
		    	} else {
					awaitReceiveMessage = true;
		    	}
				break;
			case Constants.AMEND_BOOKING:
		    	requestMessage = this.amendBooking(args);
		    	if (requestMessage == null) {
					awaitReceiveMessage = false;
		    	} else {
					awaitReceiveMessage = true;
		    	}
				break;
			case Constants.MONITOR_AVAILABILITY:
				this.monitorFacility(args);
				break;
			case Constants.CANCEL_BOOKING:
				requestMessage = this.cancelBooking(args);
		    	if (requestMessage == null) {
					awaitReceiveMessage = false;
		    	} else {
					awaitReceiveMessage = true;
		    	}
				break;
			case Constants.EXTEND_BOOKING:
		    	requestMessage = this.extendBooking(args);
		    	if (requestMessage == null) {
					awaitReceiveMessage = false;
		    	} else {
					awaitReceiveMessage = true;
		    	}
				break;
			default:
				console("Invalid action selected!");
				break;
			}
			if (awaitReceiveMessage) {
				socket.setSoTimeout(Constants.TIMEOUT_MS);
				while (true) {
					try {
						this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
						Message responseMessage = this.receiveMessage();
						RespBody respBody = (RespBody) responseMessage.getBody();
						if (!respBody.getErrorMessage().equals("")) {
							System.out.println(respBody.getErrorMessage());
						} else {
							System.out.println(respBody.getPayload());
							if (opCode == Constants.MAKE_BOOKING) {
								MakeBookingRespBody tempBody = (MakeBookingRespBody) respBody;
								StringSelection stringSelection = new StringSelection(tempBody.getBookingID().toString());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(stringSelection, null);
								System.out.println("BookingID has been copied to clipboard! Please save for future references..");
							}
						}
						break;
					} catch (IOException ex) {
						System.out.println("Request transmission failed, resending...");
					}
				}
			}
			backToMain();
		}
	}
	
	private Message cancelBooking(ArrayList<String> args) throws IllegalArgumentException {
		System.out.println("Please enter the booking ID.");
		UUID bookingID = scanBookingID();
		if (bookingID == null) {
			return null;
		}

		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.CANCEL_BOOKING, Constants.REQUEST),
				new CancelBookingReqBody(bookingID));
		
		return requestMessage;
	}

	private Message amendBooking(ArrayList<String> args) throws IllegalArgumentException {
		System.out.println("Please enter the booking ID.");
		UUID bookingID = scanBookingID();
		if (bookingID == null) {
			return null;
		}
		
		System.out.println("Please enter the offset (in minutes) that you wish to advance/postpone. (negative number to advance, positive number to postpone)");
		int offset = scanInteger();
		if (offset == Integer.MAX_VALUE) {
			return null;
		}

		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.AMEND_BOOKING, Constants.REQUEST),
				new AmendBookingReqBody(bookingID, offset));
		
		return requestMessage;
	}	
	
	private UUID scanBookingID() {
		UUID bookingID;
		try {
			bookingID = UUID.fromString(scanner.nextLine());
		} catch (IllegalArgumentException e) {
			System.out.println("Error! The booking ID does not exist.");
			return null;
		}
		return bookingID;
	}

	private Message extendBooking(ArrayList<String> args) throws IllegalArgumentException {
		System.out.println("Please enter the booking ID.");
		UUID bookingID = scanBookingID();
		if (bookingID == null) {
			return null;
		}
		
		System.out.println("Please enter the duration you wish to extend (in minutes)");
		int offset = scanInteger();
		if (offset == Integer.MAX_VALUE) {
			return null;
		}

		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.EXTEND_BOOKING, Constants.REQUEST),
				new ExtendBookingReqBody(bookingID, offset));
		
		return requestMessage;
	}

	private int scanInteger() {		
		try {
			return Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Error! Offset can only be integer.");
			return Integer.MAX_VALUE;
		}
	}

	private Message makeBooking(ArrayList<String> args) throws IOException, IllegalArgumentException, IllegalAccessException, TimeErrorException {
		args.clear();
		args.add("Please enter the day of interest.");
		Arrays.asList(Day.values()).forEach(day -> {
			args.add(day.toString());
		});
		menu(args);
		
		String dayInput = scanner.nextLine().toUpperCase();
		Day selectedDay;
		try {
			selectedDay = Day.valueOf(dayInput);
		} catch (IllegalArgumentException e) {
			System.out.println("Error! Input is not an accepted day.");
			return null;
		}
		ArrayList<Day> days = new ArrayList<Day>();
		days.add(selectedDay);
		
		ArrayList<String> facilityTypes = this.queryFacilityTypes();
		args.clear();
		args.add("Please choose the facility of interest.");
		for (String facilityType : facilityTypes) {
			args.add(facilityType);
		}
		menu(args);
		
		String facilityType = scanner.nextLine().toUpperCase();
		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.QUERY_AVAILABILITY, Constants.REQUEST),
				new QueryAvailabilityReqBody(days, "", facilityType, false));
		while (true) {
			try {
				this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
				Message responseMessage = this.receiveMessage();				
				QueryAvailabilityRespBody respBody = (QueryAvailabilityRespBody) responseMessage.getBody();
				if (!respBody.getErrorMessage().equals("")) {
					System.out.println(respBody.getErrorMessage());
					return null;
				} else {
					System.out.println(respBody.getPayload());
				}
				break;
			} catch (IOException ex) {
				System.out.println("Request transmission failed, resending...");
			}
		}
		
		System.out.println("Please enter the desired facility ID.");
		String facilityID = scanner.nextLine().toUpperCase();
		
		System.out.println("Please enter start time. (in HH:MM format)");
		String inputTime = scanner.nextLine();
		Pattern timePattern = Pattern.compile("^\\d*:\\d*$");
		
		if (!timePattern.matcher(inputTime).find()) {
			System.out.println("Invalid time format entered!...");
			return null;
		}
		String[] startTimeInput = inputTime.split(":");
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
		
		ArrayList<String> facilityIDs = this.queryFacilityIDs(facilityType);
		if (facilityIDs == null) {
			return null;
		}
		
		args.clear();
		args.add("Please choose the day of interest. (Separate day by a white space if querying for multiple days)");
		Arrays.asList(Day.values()).forEach(day -> {
			args.add(day.toString());
		});
		menu(args);
		
		String[] daysInput = scanner.nextLine().toUpperCase().split(" ");
		ArrayList<Day> days = new ArrayList<Day>();
		try {
			for (String day : daysInput) {
				days.add(Day.valueOf(day));
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Error! Input is not an accepted day.");
			return null;
		}

		args.clear();
		args.add("Please enter the facility ID of interest.");
		for (String facilityID : facilityIDs) {
			args.add(facilityID);
		}
		menu(args);
		
		String facilityID = scanner.nextLine().toUpperCase();
		
		Message requestMessage = new Message(new Header(UUID.randomUUID(), Constants.QUERY_AVAILABILITY, Constants.REQUEST),
				new QueryAvailabilityReqBody(days, facilityID, "", true));
		
		return requestMessage;
		
	}

	private ArrayList<String> queryFacilityIDs(String facilityType) throws IllegalArgumentException, IllegalAccessException, IOException {
		Header header = new Header(UUID.randomUUID(), Constants.QUERY_FACILITY_IDS, Constants.REQUEST);
		Body reqBody = new QueryFacilityIDsReqBody(facilityType);
		Message requestMessage = new Message(header, reqBody);
		ArrayList<String> facilityIDs;
		socket.setSoTimeout(Constants.TIMEOUT_MS);
		while (true) {
			try {
				this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
				Message responseMessage = this.receiveMessage();
				QueryFacilityIDsRespBody respBody = (QueryFacilityIDsRespBody) responseMessage.getBody();
				if (!respBody.getErrorMessage().equals("")) {
					System.out.println(respBody.getErrorMessage());
					facilityIDs = null;
				} else {
					facilityIDs = respBody.getFacilityIDs();
				}
				break;
			} catch (IOException ex) {
				System.out.println("Request transmission failed, resending...");
			}
		}
		return facilityIDs;
	}

	private ArrayList<String> queryFacilityTypes() throws IllegalArgumentException, IllegalAccessException, IOException {
		Header header = new Header(UUID.randomUUID(), Constants.QUERY_FACILITY_TYPES, Constants.REQUEST);
		Body reqBody = new QueryFacilityTypesReqBody();
		Message requestMessage = new Message(header, reqBody);
		ArrayList<String> facilityTypes;
		socket.setSoTimeout(Constants.TIMEOUT_MS);
		while (true) {
			try {
				this.sendMessage(requestMessage, this.serverAddress, this.serverPort);
				Message responseMessage = this.receiveMessage();
				QueryFacilityTypesRespBody respBody = (QueryFacilityTypesRespBody) responseMessage.getBody();
				if (!respBody.getErrorMessage().equals("")) {
					System.out.println(respBody.getErrorMessage());
					facilityTypes = null;
				} else {
					facilityTypes = respBody.getFacilityTypes();
				}
				break;
			} catch (IOException ex) {
				System.out.println("Request transmission failed, resending...");
			}
		}
		return facilityTypes;
	}

	/**
	 * 
	 * Perform monitoring of facility, prompt users for facility input.
	 * 
	 * @throws IOException - Unable to reach server.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void monitorFacility(ArrayList<String> args) throws IOException, IllegalArgumentException, IllegalAccessException {
		
		// obtain user specified facility type
		ArrayList<String> facilityTypes = this.queryFacilityTypes();
		args.add("Please enter the facility of interest. (or enter 0 to exit)");
		for (String facilityType : facilityTypes) {
			args.add(facilityType);
		}
		menu(args);
		String facilityType = scanner.nextLine().toUpperCase();
		args.clear();
		if (facilityType.equals("0")) return;
		
		// obtain user specified facility id
		ArrayList<String> facilityIDs = this.queryFacilityIDs(facilityType);
		args.add("Please enter the facility ID of interest. (or enter 0 to exit)");
		for (String facilityID : facilityIDs) {
			args.add(facilityID);
		}
		menu(args);
		String facilityID = scanner.nextLine().toUpperCase();
		args.clear();
		if (facilityID.equals("0")) return;
		if (!facilityIDs.contains(facilityID)) {
			System.out.print("Invalid facility chosen! ");
			return;
		}
		
		// obtain user specified monitor interval
		console("Please enter duration of subscription. (Enter 0 to exit)");
		String inputStr = scanner.nextLine();
		if (inputStr.equals("0")) return;
		int monitorInterval;
		try {
			monitorInterval = Integer.parseInt(inputStr);
			if (monitorInterval < 0) throw new NumberFormatException("Negative duration");
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
		Header header = new Header(UUID.randomUUID(), Constants.MONITOR_AVAILABILITY, Constants.REQUEST);
		Body body = new MonitorAvailabilityReqBody(callback);
		Message requestMessage = new Message(header, body);
		Message responseMessage;
		String data;
		
		socket.setSoTimeout(Constants.TIMEOUT_MS);
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
				header = new Header(UUID.randomUUID(), Constants.MONITOR_AVAILABILITY, Constants.RESPONSE);
				body = new MonitorAvailabilityRespBody(null, "ACK_CALLBACK");
				responseMessage = new Message(header,body);
				sendMessage(responseMessage, serverAddress, serverPort);
			}
		} catch (IOException ex) {
			console("Monitor Interval has ended... Exiting...");
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
		System.out.println("Press enter to return to menu...");
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
		Double currentLoss = rand.nextDouble();
//		System.out.println("Loss: " + currentLoss);
		if (currentLoss > Constants.PACKET_LOSS_THRESHOLD) socket.send(response);
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
			} catch (SocketException e) {
				System.out.println("Timeout error: " + e.getMessage());
				if (DEBUG) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				System.out.println("IO error: " + e.getMessage());
				if (DEBUG) {
					e.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				if (DEBUG) {
					e.printStackTrace();
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				if (DEBUG) {
					e.printStackTrace();
				}
			} catch (TimeErrorException e) {
				// TODO Auto-generated catch block
				System.out.println("Time error: " + e.getMessage());
				if (DEBUG) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				if (DEBUG) {
					e.printStackTrace();
				}
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
