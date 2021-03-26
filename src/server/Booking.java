/**
 * 
 */
package server;

import java.util.UUID;

/**
 * @author c170011
 *
 */
public class Booking {
	
	private UUID uuid;
	private String facilityName;
	private String facilityID;
	private Day day;
	private TimePeriod timePeriod;

	/**
	 * 
	 */
	public Booking(String facilityName, String facilityID, Day day, Time startTime, Time endTime) throws BookingFailedException {

		// Allow single day booking only
		if (startTime.compareTo(endTime) > 0) {
			throw new BookingFailedException("Failed to construct Booking object. Only single day booking is allowed.");
		}
		if (startTime.compareTo(endTime) == 0) {
			throw new BookingFailedException("Failed to construct Booking object. Start time and end time are the same.");
		}
		
		this.uuid = UUID.randomUUID();
		this.facilityName = facilityName;
		this.facilityID = facilityID;
		this.day = day;
		this.timePeriod = new TimePeriod(startTime, endTime);
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public String getFacilityName() {
		return this.facilityName;
	}
	
	public String getFacilityID() {
		return this.facilityID;
	}
	
	public Day getDay() {
		return this.day;
	}
	
	public TimePeriod getTimePeriod() {
		return this.timePeriod;
	}
	
	public Time getStartTime() {
		return this.timePeriod.getStartTime();
	}
	
	public Time getEndTime() {
		return this.timePeriod.getEndTime();
	}
	
	public void setStartTime(Time startTime) {
		this.timePeriod.setStartTime(startTime);
	}
	
	public void setEndTime(Time endTime) {
		this.timePeriod.setEndTime(endTime);
	}

}
