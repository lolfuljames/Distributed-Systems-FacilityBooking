package utils.message.request;

import java.util.UUID;

import server.*;
import utils.message.Body;

public class MakeBookingReqBody extends Body {
	
	public String facilityID;
	public Day day;
	public Time startTime;
	public Time endTime;

	public MakeBookingReqBody() {
		super();
	}
	
	public MakeBookingReqBody(String facilityID, Day day, Time startTime, Time endTime) {
		super();
		this.setFacilityID(facilityID);
		this.setDay(day);
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the facilityID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * @param facilityID the facilityID to set
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * @return the day
	 */
	public Day getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(Day day) {
		this.day = day;
	}

	/**
	 * @return the startTime
	 */
	public Time getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Time getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Facility ID: %s\n", this.getFacilityID());
		str += String.format("Day of booking: %s\n", this.getDay().toString());
		str += String.format("Start time: %s\n", this.getStartTime().toString());
		str += String.format("End time: %s\n", this.getEndTime().toString());
		return str;
	}

}
