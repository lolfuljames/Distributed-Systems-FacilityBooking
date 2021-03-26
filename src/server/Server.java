/**
 * 
 */
package server;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import utils.*;
import java.rmi.RemoteException;
import java.time.Instant;

/**
 * @author jame0019
 *
 */
public class Server implements CallbackServer{
	
	private DatagramSocket socket;
	private List<MonitorCallback> callbacks;
	  private LinkedHashMap<String, LinkedHashMap<String, Facility>> facilities;
	  private LinkedHashMap<UUID, Booking> bookings;
	  private long time_seconds = Instant.now().getEpochSecond();
	  
	  public Server(int port) throws SocketException {
	    System.out.println("Initialising the socket for server..." + port);
		this.facilities = this.generateFacilities();
		this.bookings = new LinkedHashMap<UUID, Booking>();
//		this.generateRandomBookings();
		this.callbacks = new ArrayList<>();
	    socket = new DatagramSocket(port);
	  }

	private void service() throws IOException, ClassNotFoundException, RemoteException {
		System.out.println("Servicing the requests...");
		while (true) {
			byte[] buffer = new byte[1000];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			socket.receive(request);

			// Do whatever is required here, process data etc
			// deserialization
//			 Message message = deserialize(buffer, Message.class);
			Message message = new Message(new Header(UUID.randomUUID(), 0, 1), new MakeBookingRespBody("", UUID.randomUUID()));
			int opCode = message.getHeader().getOpCode();

		    InetAddress clientAddr = request.getAddress();
		    int clientPort = request.getPort();
		    
			switch (opCode) {
			case 0:
				System.out.println("query");
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
			    handleCallback(request);
			    while (callbacks.size() > 0) {
				    updateMonitorInterval();
				    notifyAllCallbacks("Received with thanks");
			    }
				break;
			}
			System.out.println("Received: " + new String(request.getData()));


			// prepare the datagram packet to response
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddr, clientPort);
			socket.send(response);

			this.testCallback(request);
//			this.testQueryAvailability();
//			this.testMakeBooking();
//			this.testAmendBooking();
		}
	}


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
		  byte[] buffer = "ACK_CALLBACK".getBytes();
//		  byte[] buffer = CallbackStatus.ACK_CALLBACK.getBytes();
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
		  byte[] buffer = "EXPIRED_CALLBACK".getBytes();
//		  byte[] buffer = CallbackStatus.EXPIRED_CALLBACK.getBytes();
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
		 * When a minute has passed since last update, 
		 * Decrease monitor interval and remove expired callbacks.
		 */
	  private void updateMonitorInterval() {
		  long current_time = Instant.now().getEpochSecond();
		  if ((current_time - this.time_seconds) < 10) {
			  return;
		  }
		  this.time_seconds = current_time;
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

	/**
	 * 
	 * @param facilityName - The name of facility.
	 * @param days - The days to be queried.
	 * @return availableTiming - The available timing for the queried facility on days given.
	 * 		{
	 * 			Day1: {
	 * 				facility1: [...],
	 * 				facility2: [...],
	 * 			},
	 * 			Day2: {
	 * 				facility1: [...],
	 * 				facility2: [...]
	 * 			}
	 * 		}
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
	
	private String serviceQueryAvailability(String facilityName, ArrayList<Day> days) {
		String res = "";
		try {
			LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availableTiming = this.queryAvailability(facilityName, days);
			res += String.format("Availability for %s:\n", facilityName);
			for (Entry<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> entry : availableTiming.entrySet()) {
				res += String.format("%s:\n", entry.getKey());
				for (Entry<String, ArrayList<TimePeriod>> e : entry.getValue().entrySet()) {
//					System.out.println(e.getKey());
					for (TimePeriod timePeriod : e.getValue()) {
						res += String.format("%s: %s - %s\n", e.getKey().toString(), timePeriod.getStartTime().toString(), timePeriod.getEndTime().toString());
					}
				}
				res += "--------------------------------------------\n";
			}
		} catch (UnknownFacilityException e) {
			res = String.format("Error! The facility (%s) does not exist.\n", facilityName);
		}
		return res;
	}

	/**
	 * Make booking service
	 * 
	 * @param facilityName - The name of the facility.
	 * @param facilityID - The ID of the facility.
	 * @param day - The day of the booking.
	 * @param startTime - The start time of the booking.
	 * @param endTime - The end time of the booking.
	 * @return uuid - A unique confirmation ID.
	 * @throws UnknownFacilityException - Non-existing facility name.
	 * @throws BookingFailedException - Unacceptable booking time. (Does not include no available time slot)
	 */
	private UUID makeBooking(String facilityName, String facilityID, Day day, Time startTime, Time endTime)
			throws UnknownFacilityException, BookingFailedException {
		if (!this.facilities.containsKey(facilityName)) {
			throw new UnknownFacilityException();
		}
		UUID uuid = null;
		Booking newBooking = new Booking(facilityName, facilityID, day, startTime, endTime);
		boolean success = this.facilities.get(facilityName).get(facilityID).addBooking(newBooking);
		if (success) {
			uuid = newBooking.getUUID();
			this.bookings.put(uuid, newBooking);
		}
		return uuid;
	}

	/**
	 * Booking amendment service
	 * 
	 * @param uuid - the confirmation id for the booking.
	 * @param offset - the offset to be moved for the booking. (in minutes)
	 * @return statusCode:
	 * 		0 -> Successful
	 * 		1 -> Not successful (no available slot for amended booking)
	 * 		2 -> Not successful (amendment is not acceptable, only amendment within the day itself is accepted, i.e. the offset is too large)
	 * 		3 -> Not successful (amendment is not acceptable, only amendment within the operating hours of the facility is accepted)
	 * 		4 -> Booking does not exist
	 */
	private int amendBooking(UUID uuid, int offset) {
		if (!this.bookings.containsKey(uuid)) {
			return 4;
		}

		Booking booking = this.bookings.get(uuid);
		Facility facility = this.facilities.get(booking.getFacilityName()).get(booking.getFacilityID());
		return facility.amendBooking(booking, offset);
	}

	/**
	 * A method to generate facilities.
	 * 
	 * @return facilities - Generated facilities.
	 * {
	 * 		Facility Type 1 : {
	 * 			facility #1: Facility
	 * 		},
	 * 		Facility Type 2 : {
	 * 			facility #1: Facility
	 * 		}
	 * }
	 */
	private LinkedHashMap<String, LinkedHashMap<String, Facility>> generateFacilities() {
		LinkedHashMap<String, LinkedHashMap<String, Facility>> facilities = new LinkedHashMap<String, LinkedHashMap<String, Facility>>();
		facilities.put("Lecture Hall", new LinkedHashMap<String, Facility>());
		facilities.put("Tutorial Room", new LinkedHashMap<String, Facility>());
		facilities.put("Lab", new LinkedHashMap<String, Facility>());
		try {
			for (int i = 1; i < 6; i++) {
				facilities.get("Lecture Hall").put(String.format("LT-%d", i), new Facility("Lecture Hall", String.format("LT-%d", i), new Time(8, 0), new Time(17, 0)));
				facilities.get("Tutorial Room").put(String.format("LT-%d", i),
						new Facility("Tutorial Room", String.format("TR-%d", i), new Time(8, 0), new Time(17, 0)));
				facilities.get("Lab").put(String.format("LT-%d", i), new Facility("Lab", String.format("LAB-%d", i), new Time(8, 0), new Time(17, 0)));
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
				this.makeBooking("Lecture Hall", facilityID, day, new Time(startHour, 0), new Time(endHour, 0));
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
	
	private void testCallback(DatagramPacket request) throws ClassNotFoundException, IOException {
	    handleCallback(request);
	    while (callbacks.size() > 0) {
		    updateMonitorInterval();
		    notifyAllCallbacks("Received with thanks");
	    }
	}
	private void testQueryAvailability() {
		ArrayList<Day> days = new ArrayList<Day>();
		days.add(Day.MONDAY);
		days.add(Day.TUESDAY);
		String facilityName = "Lecture Hall";
//		try {
//			LinkedHashMap<Day, LinkedHashMap<String, ArrayList<TimePeriod>>> availability = queryAvailability(facilityName,
//					days);
//
//
//			System.out.println("Availability for Lecture Hall:");
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
		System.out.println(this.serviceQueryAvailability(facilityName, days));
	}

	private void testMakeBooking() {
		String facilityName = "Lecture Hall";
//		String facilityName = "Lecture Hal";	// Wrong facility name test case
		try {
			this.testQueryAvailability();
			UUID uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(13, 0), new Time(15, 0));
			System.out.println(uuid);	// Expected output: A random uuid
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(8, 0), new Time(9, 0));
			System.out.println(uuid);	// Expected output: A random uuid
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(9, 0), new Time(11, 0));
			System.out.println(uuid);	// Expected output: A random uuid
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(9, 0), new Time(11, 0));
			System.out.println(uuid);	// Expected output: null (No available time slot test case)
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(10, 59), new Time(12, 0));
			System.out.println(uuid);	// Expected output: null (No available time slot test case)
			uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(17, 01), new Time(18, 0));	// Expected output: Exception thrown (Non-operating hours test case)
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
		String facilityName = "Lecture Hall";
		try {
			this.testQueryAvailability();
			UUID uuid = this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(8, 0), new Time(9, 0));
			this.makeBooking(facilityName, "LT-1", Day.MONDAY, new Time(9, 30), new Time(10, 30));
			this.testQueryAvailability();
//			int offset = 30;			// Expected statusCode: 0
//			int offset = 150;			// Expected statusCode: 0
//			int offset = 31;			// Expected statusCode: 1 (No available slot test case)
			int offset = 960;			// Expected statusCode: 2 (Amendment until new day test case)
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
}




















