/**
 * 
 */
package server;

import java.net.*;
import java.util.*;
import java.io.*;

/**
 * @author jame0019
 *
 */
public class Server {

	/**
	 * 
	 */
	private DatagramSocket socket;

	private Hashtable<String, Hashtable<Integer, Facility>> facilities;
	private Hashtable<UUID, Booking> bookings;

	public Server(int port) throws SocketException {
		this.facilities = this.generateFacilities();
		this.bookings = new Hashtable<UUID, Booking>();
		this.generateRandomBookings();

		System.out.println("Initialising the socket for server..." + port);
		socket = new DatagramSocket(port);
	}

	private void close() {
		socket.close();
	}

	private void service() throws IOException {
		System.out.println("Servicing the requests...");
		while (true) {
			byte[] buffer = new byte[1000];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			socket.receive(request);

			// Do whatever is required here, process data etc
			System.out.println("Received: " + new String(request.getData()));

			// get client ip address and port
			InetAddress clientAddr = request.getAddress();
			int clientPort = request.getPort();

			// prepare the datagram packet to response
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddr, clientPort);
			socket.send(response);

//			this.testQueryAvailability();
			this.testMakeBooking();
//			this.testAmendBooking();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 12359;
		Server server = null;
		try {
			server = new Server(port);
			server.service();
		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		} finally {
			server.close();
		}
	}

//	  0: {
//			  Monday: [...],
//			  Tuesday: [...]
//    },
//	  3: {
//		  Monday: [...],
//		  Tuesday: [...]
//	  }
	private Hashtable<Day, Hashtable<Integer, ArrayList<TimePeriod>>> queryAvailability(String facilityName,
			ArrayList<Day> days) throws UnknownFacilityException {
		if (!this.facilities.containsKey(facilityName)) {
			throw new UnknownFacilityException();
		}
		Hashtable<Integer, Facility> facilityList = this.facilities.get(facilityName);
		Hashtable<Day, Hashtable<Integer, ArrayList<TimePeriod>>> availableTiming = new Hashtable<Day, Hashtable<Integer, ArrayList<TimePeriod>>>();

		facilityList.forEach((facilityID, facility) -> {
			Hashtable<Day, ArrayList<TimePeriod>> aTime = facility.getAvailableTiming(days);
			aTime.forEach((day, timePeriods) -> {
				if (!availableTiming.containsKey(day)) {
					availableTiming.put(day, new Hashtable<Integer, ArrayList<TimePeriod>>());
				}
				availableTiming.get(day).put(facilityID, timePeriods);
			});
		});

		return availableTiming;
	}

	private UUID makeBooking(String facilityName, int facilityID, Day day, Time startTime, Time endTime)
			throws UnknownFacilityException, BookingFailedException {
		UUID uuid = null;
		Booking newBooking = new Booking(facilityName, facilityID, day, startTime, endTime);
		if (!this.facilities.containsKey(facilityName)) {
			throw new UnknownFacilityException();
		}
		boolean success = this.facilities.get(facilityName).get(facilityID).addBooking(newBooking);
		if (success) {
			uuid = newBooking.getUUID();
			this.bookings.put(uuid, newBooking);
		}
		return uuid;
	}

	/*
	 * Status code:
	 * 0 -> Successful
	 * 1 -> Not successful (no available slot for amended booking)
	 * 2 -> Not successful (amendment is not acceptable, only amendment within the day itself is accepted, i.e. the offset is too large)
	 * 3 -> Not successful (amendment is not acceptable, only amendment within the operating hours of the facility is accepted)
	 * 4 -> Booking does not exist
	 */
	// offset in minutes
	private int amendBooking(UUID uuid, int offset) {
		if (!this.bookings.containsKey(uuid)) {
			return 4;
		}

		Booking booking = this.bookings.get(uuid);
		Facility facility = this.facilities.get(booking.getFacilityName()).get(booking.getFacilityID());
		return facility.amendBooking(booking, offset);
	}

	private Hashtable<String, Hashtable<Integer, Facility>> generateFacilities() {
		Hashtable<String, Hashtable<Integer, Facility>> facilities = new Hashtable<String, Hashtable<Integer, Facility>>();
		facilities.put("Lecture Hall", new Hashtable<Integer, Facility>());
		facilities.put("Tutorial Room", new Hashtable<Integer, Facility>());
		facilities.put("Lab", new Hashtable<Integer, Facility>());
		try {
			for (int i = 0; i < 5; i++) {
				facilities.get("Lecture Hall").put(i, new Facility("Lecture Hall", i, new Time(8, 0), new Time(17, 0)));
				facilities.get("Tutorial Room").put(i + 5,
						new Facility("Tutorial Room", i + 5, new Time(8, 0), new Time(17, 0)));
				facilities.get("Lab").put(i + 10, new Facility("Lab", i + 10, new Time(8, 0), new Time(17, 0)));
			}
		} catch (TimeErrorException e) {
			System.out.println(String.format("The time given is not a valid time (in 24-hour format)."));
		}
		return facilities;
	}

	private void generateRandomBookings() {
		Arrays.asList(Day.values()).forEach(day -> {
			int facilityID = new Random().nextInt(15);
			int startHour = new Random().nextInt(9);
			int endHour = new Random().nextInt(9 - startHour);
			startHour += 8;
			endHour += startHour + 1;
			try {
				if (facilityID < 5) {
					this.makeBooking("Lecture Hall", facilityID, day, new Time(startHour, 0), new Time(endHour, 0));
				} else if (facilityID < 10) {
					this.makeBooking("Tutorial Room", facilityID, day, new Time(startHour, 0), new Time(endHour, 0));
				} else if (facilityID < 10) {
					this.makeBooking("Lab", facilityID, day, new Time(startHour, 0), new Time(endHour, 0));
				}
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

	private void testQueryAvailability() {
		ArrayList<Day> days = new ArrayList<Day>();
		days.add(Day.MONDAY);
		String facilityName = "Lecture Hall";
		try {
			Hashtable<Day, Hashtable<Integer, ArrayList<TimePeriod>>> availability = queryAvailability(facilityName,
					days);

			System.out.println("Availability for Lecture Hall:");
			availability.forEach((day, innerHashtable) -> {
				System.out.println(day + ": ");
				innerHashtable.forEach((facilityID, availableTimePeriods) -> {
					availableTimePeriods.forEach(timePeriod -> {
						System.out.println(facilityID.toString() + ": " + timePeriod.getStartTime().toString() + " - "
								+ timePeriod.getEndTime().toString());
					});
				});
				System.out.println("--------------------------------------------");
			});
		} catch (UnknownFacilityException e) {
			System.out.println(String.format("The facility (%s) does not exist.", facilityName));
		}

	}

	private void testMakeBooking() {
		String facilityName = "Lecture Hall";
//		String facilityName = "Lecture Hal";	// Wrong facility name test case
		try {
			this.testQueryAvailability();
			UUID uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(13, 0), new Time(15, 0));
			System.out.println(uuid);	// Expected output: A random uuid
			uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(8, 0), new Time(9, 0));
			System.out.println(uuid);	// Expected output: A random uuid
			uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(9, 0), new Time(11, 0));
			System.out.println(uuid);	// Expected output: A random uuid
			uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(9, 0), new Time(11, 0));
			System.out.println(uuid);	// Expected output: null (No available time slot test case)
			uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(10, 59), new Time(12, 0));
			System.out.println(uuid);	// Expected output: null (No available time slot test case)
			uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(17, 01), new Time(18, 0));	// Expected output: Exception thrown (Non-operating hours test case)
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
			UUID uuid = this.makeBooking(facilityName, 0, Day.MONDAY, new Time(8, 0), new Time(9, 0));
			this.makeBooking(facilityName, 0, Day.MONDAY, new Time(9, 30), new Time(10, 30));
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




















