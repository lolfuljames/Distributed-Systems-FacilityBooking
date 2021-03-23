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
	private int facilityID;
	private TimePeriod timePeriod;

	/**
	 * 
	 */
	public Booking(int facilityID, Time startTime, Time endTime) {
		// TODO Auto-generated constructor stub
		this.uuid = UUID.randomUUID();
		this.facilityID = facilityID;
		this.timePeriod = new TimePeriod(startTime, endTime);
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public int getFacilityID() {
		return this.facilityID;
	}
	
	public TimePeriod getTimePeriod() {
		return this.timePeriod;
	}

}
