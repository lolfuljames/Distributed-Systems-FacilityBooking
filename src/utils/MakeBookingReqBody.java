package utils;

import java.util.UUID;

import server.*;

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
		this.facilityID = facilityID;
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		// TODO Auto-generated constructor stub
	}

}
