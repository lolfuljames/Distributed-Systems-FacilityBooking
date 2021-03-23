/**
 * 
 */
package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * @author c170011
 *
 */
public class Facility {

	private String name;
	private int facilityID;
	private Time earliestTime;
	private Time latestTime;
	private Hashtable<Day, ArrayList<Booking>> bookings;
	/**
	 * 
	 */
	public Facility(String name, int facilityID, Time earliestTime, Time latestTime) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.facilityID = facilityID;
		this.earliestTime = earliestTime;
		this.latestTime = latestTime;
		this.bookings = new Hashtable<Day, ArrayList<Booking>>();
		Arrays.asList(Day.values()).forEach(day -> {
			this.bookings.put(day, new ArrayList<Booking>());
		});
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getFacilityID() {
		return this.facilityID;
	}
	
	public Hashtable<Day, ArrayList<Booking>> getBookings() {
		return this.bookings;
	}
	
	public Hashtable<Day, ArrayList<TimePeriod>> getAvailableTiming(ArrayList<Day> days) {
		Hashtable<Day, ArrayList<TimePeriod>> availableTiming = new Hashtable<Day, ArrayList<TimePeriod>>();
		days.forEach(day -> {
			availableTiming.put(day, this._getAvailableTiming(day));
		});
		
		return availableTiming;
	}
	
	private ArrayList<TimePeriod> _getAvailableTiming(Day day) {
		ArrayList<TimePeriod> availableTiming = new ArrayList<TimePeriod>();
		System.out.println(day);
		ArrayList<Booking> bookings = this.bookings.get(day);
		
		if (bookings.size() < 1) {
			availableTiming.add(new TimePeriod(this.earliestTime, this.latestTime));
		} else {
			TimePeriod bookedPeriod = bookings.get(0).getTimePeriod();
			Time startTime = bookedPeriod.getStartTime();
			if (this.earliestTime != startTime) {
				availableTiming.add(new TimePeriod(this.earliestTime, startTime));			
			}
			Time endTime = bookedPeriod.getEndTime();
			
			for (int i=1; i<bookings.size(); i++) {
				bookedPeriod = bookings.get(i).getTimePeriod();
				startTime = bookedPeriod.getStartTime();
				
				availableTiming.add(new TimePeriod(endTime, startTime));
				
				endTime = bookedPeriod.getEndTime();
			}
			
			if (this.latestTime != endTime) {
				availableTiming.add(new TimePeriod(endTime, this.latestTime));
			}
		}
		
		return availableTiming;
	}

}
