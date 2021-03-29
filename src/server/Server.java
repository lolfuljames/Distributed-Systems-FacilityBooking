/**
 * 
 */
package server;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import utils.*;
import utils.callback.CallbackServer;
import utils.callback.MonitorCallback;
import utils.message.request.*;
import utils.message.response.*;
import utils.message.Body;
import utils.message.Header;
import utils.message.Message;
import utils.serialize.Deserializer;
import utils.serialize.Serializer;

import java.rmi.RemoteException;
import java.time.Instant;

/**
 * @author jame0019
 *
 */
public class Server implements CallbackServer {

	private DatagramSocket socket;
	private List<MonitorCallback> callbacks;
	private LinkedHashMap<String, LinkedHashMap<String, Facility>> facilities;
	private LinkedHashMap<UUID, Booking> bookings;
	private long lastUpdatedSeconds = Instant.now().getEpochSecond();
	private long lastClearedCacheSeconds = Instant.now().getEpochSecond();
	private Scanner scanner = new Scanner(System.in);
	private int semanticMode;
	private Hashtable<UUID, CacheMessageObject> messageCache;

	public Server(int port) throws SocketException {
		System.out.println("Initialising the socket for server..." + port);
		this.facilities = this.generateFacilities();
		this.bookings = new LinkedHashMap<UUID, Booking>();
//		this.generateRandomBookings();
		this.callbacks = new ArrayList<>();
		socket = new DatagramSocket(port);
		messageCache = new Hashtable<UUID, CacheMessageObject>();
	}

	private void service() throws IOException, ClassNotFoundException, RemoteException, TimeErrorException, IllegalArgumentException, IllegalAccessException {
		String inputStr;
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\nWelcome to the NTU Facility Booking Service!\n"
				+ "0 - at-most-once\n" + "1 - at-least-once\n" + "Please enter your preferred sementic mode: ");
		inputStr = scanner.nextLine();
		try {
			semanticMode = Integer.parseInt(inputStr);
		} catch (NumberFormatException ne) {
			System.out.println("Invalid semantic selected! Press enter to continue...");
			scanner.nextLine();
			return;
		}
		System.out.println("Servicing the requests...");
		while (true) {
			DatagramPacket request = receivePacket();
			if (semanticMode == 0) {
				updateCacheTTL();
			}
			updateMonitorInterval();
//			Message requestMessage = Deserializer.deserialize(request.getData(), Message.class);
//
//			Message requestMessage = new Message(new Header(UUID.randomUUID(), 0, 1),
//					new MakeBookingRespBody("", UUID.randomUUID()));
			byte[] byteBuffer = request.getData();
			ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);
			Message requestMessage = Deserializer.deserialize(buffer, Message.class);
			int opCode = requestMessage.getHeader().getOpCode();
			UUID messageID = requestMessage.getHeader().getMessageID();

			Message responseMessage = null;
			Body reqBody = requestMessage.getBody();
			Body respBody = null;
			Header header = new Header(messageID, opCode, Constants.RESPONSE);

			InetAddress clientAddr = request.getAddress();
			int clientPort = request.getPort();
			
//			duplicated client request detected
			if (this.semanticMode == 0 && messageCache.contains(messageID)) {
				Message cachedMessage = messageCache.get(messageID).getMessage();
				sendMessage(cachedMessage, clientAddr, clientPort);
				continue;
			}

			switch (opCode) {
			case Constants.QUERY_AVAILABILITY:
				respBody = this.handleQueryAvailability((QueryAvailabilityReqBody) reqBody);
				break;
			case Constants.MAKE_BOOKING:
				respBody = this.handleMakeBooking((MakeBookingReqBody) reqBody);
				break;
			case Constants.AMEND_BOOKING:
				respBody = handleAmendBooking((AmendBookingReqBody) reqBody);

				break;
			case Constants.MONITOR_AVAILABILITY:
				respBody = handleCallback((MonitorAvailabilityReqBody) reqBody);
//			    while (callbacks.size() > 0) {
//				    updateMonitorInterval();
//				    notifyAllCallbacks("Received with thanks");
//			    }
				break;
			case Constants.QUERY_FACILITY_TYPES:
				respBody = this.handleQueryFacilityTypes();
				break;
			}

			responseMessage = new Message(header, respBody);
			CacheMessageObject cacheObject = new CacheMessageObject(responseMessage, Instant.now().getEpochSecond());
			if (semanticMode == 0) {
				messageCache.put(messageID, cacheObject);
			}

//			buf = Serializer.serialize(responseMessage, buf);
//			server.send(buf);
//			System.out.println("Received: " + new String(request.getData()));

//          Legacy code
//			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddr, clientPort);
//			socket.send(response);
			// prepare the datagram packet to response
			sendMessage(responseMessage, clientAddr, clientPort);

//			this.testCallback(request);
//			this.testQueryAvailability();
//			this.testMakeBooking();
//			this.testAmendBooking();
		}
	}

	/**
	 * 
	 * For every 10 seconds, clears cache for expired messages
	 * 
	 */
	private void updateCacheTTL() {
		long current_time = Instant.now().getEpochSecond();
		if ((current_time - this.lastClearedCacheSeconds) < 10) {
			return;
		}
		this.lastClearedCacheSeconds = current_time;
		List<UUID> expiredMessageIDs = new ArrayList<UUID>();
		
		messageCache.forEach((messageID, cacheObject) -> {
			if (cacheObject.isExpiredMessage()) expiredMessageIDs.add(messageID);
		});
		
		expiredMessageIDs.forEach(messageID -> messageCache.remove(messageID));
	}

	public static void main(String[] args) {
		int port = 2222;

		try {
			Server server = new Server(port);
			while (true) {
				server.service();
			}
		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.out.println("Class not found error: " + ex.getMessage());
		} catch (TimeErrorException ex) {
			// TODO Auto-generated catch block\
			System.out.println("TimeErrorException occured: " + ex.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Main callback handler, logs client's IP and port.
	 * 
	 * @param request - DatagramPacket sent from client.
	 * @throws IOException - Unable to reach client.
	 * @return respBody - Response Body with message:ACK error:null.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public RespBody handleCallback(MonitorAvailabilityReqBody reqBody)
			throws IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		String payload = "ACK_CALLBACK";
		String errorMessage = null;
		MonitorCallback newCallback = (MonitorCallback) reqBody.getMonitorCallback();
		addCallback(newCallback);

		RespBody respBody = new MonitorAvailabilityRespBody(errorMessage, payload);
		return respBody;
	}

	public RespBody handleAmendBooking(AmendBookingReqBody reqBody) {
		UUID bookingID = reqBody.getBookingID();
		int offset = reqBody.getOffset();
		int statusCode = amendBooking(bookingID, offset);
		String errorMessage;
		if (statusCode == 0) {
			errorMessage = null;
		} else if (statusCode == 1) {
			errorMessage = "The timeslot is not available!";
		} else if (statusCode == 2) {
			errorMessage = "The timeslot spans across two different days!";
		} else if (statusCode == 3) {
			errorMessage = "The timeslot exceeds our operating hours!";
		} else if (statusCode == 4) {
			errorMessage = "Invalid UUID";
		} else
			errorMessage = "Unexpected Error Occured!";

		RespBody respBody = new AmendBookingRespBody(errorMessage);
		return respBody;
	}

	public void getBooking() {
	}

	/**
	 * 
	 * Handler to add callback to registered list
	 * 
	 * @param callback - MonitorCallback object sent from client.
	 */
	public void addCallback(MonitorCallback callback) {
		callbacks.add(callback);
	}

	/**
	 * 
	 * Handler to remove callback from registered list
	 * 
	 * @param callback - MonitorCallback object to be removed.\
	 */
	public void removeCallback(MonitorCallback callback) {
		callbacks.remove(callback);
	}

	/**
	 * 
	 * Handler for sending messages to client.
	 * 
	 * @param callback - MonitorCallback object of the registered client.
	 * @param message  - message to be sent
	 * @throws IOException - Unable to reach client. TODO - uncomment sendMessage
	 *                     when serializer is ready.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void notifyCallback(MonitorCallback callback, String message) throws IOException, IllegalArgumentException, IllegalAccessException {
		Body respBody;
		Header header;
		Message respMessage;
		Message ackMessage;
		String data;

		message = message + "\n Monitoring will end in " + callback.getMonitorInterval() + " minutes. ";
		respBody = new MonitorAvailabilityRespBody(null, message);
		header = new Header(UUID.randomUUID(), Constants.MONITOR_AVAILABILITY, Constants.RESPONSE);
		respMessage = new Message(header, respBody);
		this.socket.setSoTimeout(200);
		while (true) {
			sendMessage(respMessage, callback.getAddress(), callback.getPort());
			try {
				ackMessage = receiveMessage();
				data = ((MonitorAvailabilityRespBody) ackMessage.getBody()).getPayload();
				if (data.equals("ACK_CALLBACK")) break;
			} catch (IOException ex) {
				System.out.println(String.format("ACK_CALLBACK not received from %s, Sending notification again...", callback.getAddress().toString()));
			}
		}
		this.socket.setSoTimeout(7 * 24 * 60 * 1000);
	};

	/**
	 * 
	 * Handler for sending messages to all clients.
	 * 
	 * @param message - message to be sent
	 * @throws IOException - Unable to reach client.
	 */
	private void notifyAllCallbacks(Facility facility) {
		String message = "";
		ArrayList<Day> allDays = new ArrayList<Day>(List.of(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY));
		LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availableTiming;
		try {
			availableTiming = this.queryAvailability(facility.getName(), allDays);
		} catch (UnknownFacilityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Notifying callback of unknown facility.. This error should never happen..");
			return;
		}
		message += String.format("Availability for %s:\n", facility.getName());
		message += _convertTimingsToString(availableTiming);
		
		final String callbackMessage = message;
		callbacks.forEach(callback -> {
			if (callback.getMonitorFacilityID().equals(facility.getFacilityID())) {
				try {
					notifyCallback(callback, callbackMessage);
				} catch (RemoteException re) {
					re.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * When a minute has passed since last update, Decrease monitor interval and
	 * remove expired callbacks.
	 */
	private void updateMonitorInterval() {
		long current_time = Instant.now().getEpochSecond();
		if ((current_time - this.lastUpdatedSeconds) < 10) {
			return;
		}
		this.lastUpdatedSeconds = current_time;
		List<MonitorCallback> expiredCallbacks = new ArrayList<>();
		callbacks.forEach(callback -> {
			callback.setMonitorInterval(callback.getMonitorInterval() - 1);
			if (callback.getMonitorInterval() == 0) {
				expiredCallbacks.add(callback);
			}
		});
		expiredCallbacks.forEach(callback -> {
			removeCallback(callback);
		});
	}

	/**
	 * 
	 * @param facilityName - The name of facility.
	 * @param days         - The days to be queried.
	 * @return availableTiming - The available timing for the queried facility on
	 *         days given. { Day1: { facility1: [...], facility2: [...], }, Day2: {
	 *         facility1: [...], facility2: [...] } }
	 * 
	 * @throws UnknownFacilityException - Non-existing facility name.
	 */
	private LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> queryAvailability(String facilityName,
			ArrayList<Day> days) throws UnknownFacilityException {
		if (!this.facilities.containsKey(facilityName)) {
			throw new UnknownFacilityException();
		}
		LinkedHashMap<String, Facility> facilityList = this.facilities.get(facilityName);
		LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availableTiming = new LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>>();

		facilityList.forEach((facilityID, facility) -> {
			LinkedHashMap<Day, ArrayList<TimePeriod>> aTime = facility.getAvailableTiming(days);
			aTime.forEach((day, timePeriods) -> {
				if (!availableTiming.containsKey(day)) {
					availableTiming.put(day, new LinkedHashMap<String, ArrayList<TimePeriod>>());
				}
				availableTiming.get(day).put(facilityID, timePeriods);
			});
		});

		return availableTiming;
	}

	private RespBody handleMakeBooking(MakeBookingReqBody reqBody) {
		String facilityID = reqBody.getFacilityID();
		String facilityName = facilityID.split("-")[0];
		Day day = reqBody.getDay();
		Time startTime = reqBody.getStartTime();
		Time endTime = reqBody.getEndTime();

		UUID bookingID = null;
		String errorMessage = null;
		try {
			bookingID = this.makeBooking(facilityName, facilityID, day, startTime, endTime);
		} catch (UnknownFacilityException e) {
			errorMessage = String.format("The facility (%s) does not exist.", facilityName);
		} catch (BookingFailedException e) {
			errorMessage = e.getMessage();
		}

		RespBody respBody = new MakeBookingRespBody(errorMessage, bookingID);
		return respBody;
	}
	
	private RespBody handleQueryFacilityTypes() {
		ArrayList<String> facilityTypes = this.getFacilityTypes();
		RespBody respBody = new QueryFacilityTypesRespBody(null, facilityTypes);
		return respBody;
	}

	private RespBody handleQueryAvailability(QueryAvailabilityReqBody reqBody) {
		ArrayList<Day> days = reqBody.getDays();
		String facilityName = reqBody.getFacilityName();
		String res = "";
		String errorMessage = null;
		try {
			LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availableTiming = this
					.queryAvailability(facilityName, days);
			res += String.format("Availability for %s:\n", facilityName);
			res += this._convertTimingsToString(availableTiming);
		} catch (UnknownFacilityException e) {
			errorMessage = String.format("Error! The facility (%s) does not exist.\n", facilityName);
		}

		RespBody respBody = new QueryAvailabilityRespBody(errorMessage, res);
		return respBody;
	}
	
	private String _convertTimingsToString(LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availableTiming) {
		String res = "";
		for (Entry<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> entry : availableTiming.entrySet()) {
			res += String.format("%s:\n", entry.getKey());
			for (Entry<String, ArrayList<TimePeriod>> e : entry.getValue().entrySet()) {
				for (TimePeriod timePeriod : e.getValue()) {
					res += String.format("%s: %s - %s\n", e.getKey().toString(),
							timePeriod.getStartTime().toString(), timePeriod.getEndTime().toString());
				}
			}
			res += "--------------------------------------------\n";
		}
		return res;
	}

	/**
	 * Make booking service
	 * 
	 * @param facilityName - The name of the facility.
	 * @param facilityID   - The ID of the facility.
	 * @param day          - The day of the booking.
	 * @param startTime    - The start time of the booking.
	 * @param endTime      - The end time of the booking.
	 * @return uuid - A unique confirmation ID.
	 * @throws UnknownFacilityException - Non-existing facility name.
	 * @throws BookingFailedException   - Unacceptable booking time. (Does not
	 *                                  include no available time slot)
	 */
	private UUID makeBooking(String facilityName, String facilityID, Day day, Time startTime, Time endTime)
			throws UnknownFacilityException, BookingFailedException {
		if (!this.facilities.containsKey(facilityName)) {
			throw new UnknownFacilityException();
		}
		UUID uuid = null;
		Booking newBooking = new Booking(facilityName, facilityID, day, startTime, endTime);
		Facility facility = this.facilities.get(facilityName).get(facilityID);
		boolean success = facility.addBooking(newBooking);
		if (success) {
			uuid = newBooking.getUUID();
			this.bookings.put(uuid, newBooking);
			notifyAllCallbacks(facility);
		}
		return uuid;
	}

	/**
	 * Booking amendment service
	 * 
	 * @param bookingID - the confirmation id for the booking.
	 * @param offset    - the offset to be moved for the booking. (in minutes)
	 * @return statusCode: 0 -> Successful 1 -> Not successful (no available slot
	 *         for amended booking) 2 -> Not successful (amendment is not
	 *         acceptable, only amendment within the day itself is accepted, i.e.
	 *         the offset is too large) 3 -> Not successful (amendment is not
	 *         acceptable, only amendment within the operating hours of the facility
	 *         is accepted) 4 -> Booking does not exist
	 */
	private int amendBooking(UUID bookingID, int offset) {
		if (!this.bookings.containsKey(bookingID)) {
			return 4;
		}

		Booking booking = this.bookings.get(bookingID);
		Facility facility = this.facilities.get(booking.getFacilityName()).get(booking.getFacilityID());
		int statusCode = facility.amendBooking(booking, offset);
		if (statusCode == 0) {
			notifyAllCallbacks(facility);
		}
		return statusCode;
	}
	
	private ArrayList<String> getFacilityTypes() {
		ArrayList<String> facilityTypes = new ArrayList<String>();
		this.facilities.forEach((facility, temp) -> {
			facilityTypes.add(facility);
		});
		return facilityTypes;
	}

	/**
	 * A method to generate facilities.
	 * 
	 * @return facilities - Generated facilities. { Facility Type 1 : { facility #1:
	 *         Facility }, Facility Type 2 : { facility #1: Facility } }
	 */
	private LinkedHashMap<String, LinkedHashMap<String, Facility>> generateFacilities() {
		LinkedHashMap<String, LinkedHashMap<String, Facility>> facilities = new LinkedHashMap<String, LinkedHashMap<String, Facility>>();
		facilities.put("LT", new LinkedHashMap<String, Facility>());
		facilities.put("TR", new LinkedHashMap<String, Facility>());
		facilities.put("LAB", new LinkedHashMap<String, Facility>());
		try {
			for (int i = 1; i < 6; i++) {
				facilities.get("LT").put(String.format("LT-%d", i),
						new Facility("LT", String.format("LT-%d", i), new Time(8, 0), new Time(17, 0)));
				facilities.get("TR").put(String.format("LT-%d", i),
						new Facility("TR", String.format("TR-%d", i), new Time(8, 0), new Time(17, 0)));
				facilities.get("LAB").put(String.format("LT-%d", i),
						new Facility("LAB", String.format("LAB-%d", i), new Time(8, 0), new Time(17, 0)));
			}
		} catch (TimeErrorException e) {
			System.out.println(String.format("The time given is not a valid time (in 24-hour format)."));
		}
		return facilities;
	}

	/**
	 * A method to generate 1 random bookings on each day for a random facility ID.
	 */
	private void generateRandomBookings() {
		Arrays.asList(Day.values()).forEach(day -> {
			String facilityID = String.format("LT-%d", new Random().nextInt(5) + 1);

			int startHour = new Random().nextInt(9);
			int endHour = new Random().nextInt(9 - startHour);
			startHour += 8;
			endHour += startHour + 1;
			try {
				this.makeBooking("LT", facilityID, day, new Time(startHour, 0), new Time(endHour, 0));
			} catch (TimeErrorException e) {
				System.out.println(String.format("The time given is not a valid time (in 24-hour format)."));
			} catch (BookingFailedException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			} catch (UnknownFacilityException e) {
				e.printStackTrace();
			}
		});
	}

	private String res = "";

//	private void testCallback(DatagramPacket request) throws ClassNotFoundException, IOException {
//	    handleCallback(request);
//	    while (callbacks.size() > 0) {
//		    updateMonitorInterval();
//		    notifyAllCallbacks("Received with thanks");
//	    }
//	}

	private void testQueryAvailability() {
		ArrayList<Day> days = new ArrayList<Day>();
		days.add(Day.MONDAY);
		days.add(Day.TUESDAY);
		String facilityName = "LT";
//		try {
//			LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availability = queryAvailability(facilityName,
//					days);
//
//
//			System.out.println("Availability for LT:");
//			availability.forEach((day, innerHashtable) -> {
//				this.res += "";
//				System.out.println(day + ": ");
//				innerHashtable.forEach((facilityID, availableTimePeriods) -> {
//					availableTimePeriods.forEach(timePeriod -> {
//						System.out.println(facilityID.toString() + ": " + timePeriod.getStartTime().toString() + " - "
//								+ timePeriod.getEndTime().toString());
//					});
//				});
//				System.out.println("--------------------------------------------");
//			});
//		} catch (UnknownFacilityException e) {
//			System.out.println(String.format("The facility (%s) does not exist.", facilityName));
//		}
//		System.out.println(this.serviceQueryAvailability(facilityName, days));
	}

	private void testMakeBooking() {
		String facilityName = "LT";
//		String facilityName = "Lecture Hal";	// Wrong facility name test case
		try {
			this.testQueryAvailability();
			UUID uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(13, 0), new Time(15, 0));
			System.out.println(uuid); // Expected output: A random uuid
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(8, 0), new Time(9, 0));
			System.out.println(uuid); // Expected output: A random uuid
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(9, 0), new Time(11, 0));
			System.out.println(uuid); // Expected output: A random uuid
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(9, 0), new Time(11, 0));
			System.out.println(uuid); // Expected output: null (No available time slot test case)
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(10, 59), new Time(12, 0));
			System.out.println(uuid); // Expected output: null (No available time slot test case)
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(17, 01), new Time(18, 0)); // Expected
																											// output:
																											// Exception
																											// thrown
																											// (Non-operating
																											// hours
																											// test
																											// case)
			this.testQueryAvailability();
		} catch (TimeErrorException e) {
			System.out.println(String.format("The time given is not a valid time (in 24-hour format)."));
		} catch (BookingFailedException e) {
			System.out.println(e.getMessage());
		} catch (UnknownFacilityException e) {
			System.out.println(String.format("The facility (%s) does not exist.", facilityName));
		}
	}

	private void testAmendBooking() {
		String facilityName = "LT";
		try {
			this.testQueryAvailability();
			UUID uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(8, 0), new Time(9, 0));
			this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(9, 30), new Time(10, 30));
			this.testQueryAvailability();
//			int offset = 30;			// Expected statusCode: 0
//			int offset = 150;			// Expected statusCode: 0
//			int offset = 31;			// Expected statusCode: 1 (No available slot test case)
			int offset = 960; // Expected statusCode: 2 (Amendment until new day test case)
//			int offset = 900;			// Expected statusCode: 3 (Amendment until non-operating hours test case)
//			uuid = UUID.randomUUID();	// Expected statusCode: 4 (Wrong uuid test case)
			int statusCode = this.amendBooking(uuid, offset);
			System.out.println(statusCode);
			this.testQueryAvailability();
		} catch (TimeErrorException e) {
			System.out.println(String.format("The time given is not a valid time (in 24-hour format)."));
		} catch (BookingFailedException e) {
			System.out.println(e.getMessage());
		} catch (UnknownFacilityException e) {
			System.out.println(String.format("The facility (%s) does not exist.", facilityName));
		}
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
	
	// to only be used when expecting a specific reply
	private Message receiveMessage() throws IOException {
		byte[] buf = new byte[2048];
		DatagramPacket request = new DatagramPacket(buf, buf.length);
		socket.receive(request);
		byte[] byteBuffer = request.getData();
		ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);
		Message requestMessage = Deserializer.deserialize(buffer, Message.class);
		return requestMessage;
	}
}
