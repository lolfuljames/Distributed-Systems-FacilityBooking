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
	  
	  private Hashtable<String, ArrayList<Facility>> facilities;
	  private ArrayList<Booking> bookings;

	  public Server(int port) throws SocketException {
		  this.facilities = new Hashtable<String, ArrayList<Facility>>();
		  this.bookings = new ArrayList<Booking>();
		  this.facilities.put("Lecture Hall", new ArrayList<Facility>());
		  this.facilities.put("Tutorial Room", new ArrayList<Facility>());
		  this.facilities.put("Lab", new ArrayList<Facility>());
		  for (int i=0; i<5; i++) {
			  this.facilities.get("Lecture Hall").add(new Facility("Lecture Hall", i, new Time()));
			  this.facilities.get("Tutorial Room").add(new Facility("Tutorial Room", i+5));
			  this.facilities.get("Lab").add(new Facility("Lab", i+10));
		  }
		  
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
		      
		      ArrayList<Day> days = new ArrayList<Day>();
		      days.add(Day.MONDAY);
		      Hashtable<Integer, Hashtable<Day, ArrayList<TimePeriod>>> availability = queryAvailability("Lecture Hall", days);
		      
		      System.out.println("lab");
		      availability.forEach((facilityID, innerHashtable) -> {
		    	  innerHashtable.forEach((day, availableTimePeriods) -> {
		    		  availableTimePeriods.forEach(timePeriod -> {
		    			  System.out.println(timePeriod);
		    			  System.out.println(facilityID.toString() + ": " + day.toString() + timePeriod.getStartTime().toString() + timePeriod.getEndTime().toString());
		    		  });
		    	  });
		      });
		    }
		  }

	/**
	 * @param args
	 */
	  public static void main(String[] args){
		    int port = 12351;
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
	  private Hashtable<Integer, Hashtable<Day, ArrayList<TimePeriod>>> queryAvailability(String facilityName, ArrayList<Day> days) {
		  ArrayList<Facility> facilityList = this.facilities.get(facilityName);
		  Hashtable<Integer, Hashtable<Day, ArrayList<TimePeriod>>> availableTiming = 
				  new Hashtable<Integer, Hashtable<Day, ArrayList<TimePeriod>>>();
		  
		  facilityList.forEach((facility) -> {
			  Hashtable<Day, ArrayList<TimePeriod>> aTime = facility.getAvailableTiming(days);
			  System.out.println(aTime.size());
			  availableTiming.put(facility.getFacilityID(), aTime);
		  });
		  
		  return availableTiming;
	  }

}
