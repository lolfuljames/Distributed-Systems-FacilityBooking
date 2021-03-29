package utils.message.request;

import java.util.*;
import server.*;
import utils.message.Body;

public class QueryAvailabilityReqBody extends Body {

	public ArrayList<Day> days;
	public String facilityID;
	
	public QueryAvailabilityReqBody() {
		super();
	}
	
	public QueryAvailabilityReqBody(ArrayList<Day> days, String facilityID) {
		// TODO Auto-generated constructor stub
		this.days = days;
		this.facilityID = facilityID;
	}
	
	public ArrayList<Day> getDays() {
		return this.days;
	}
	
	public String getFacilityID() {
		return this.facilityID;
	}
	
	public String toString() {
		String str = "";
		String daysToQuery = "";
		for (Day day : this.getDays()) {
			daysToQuery += String.format("%s ", day.toString());
		}
		str += String.format("Days to query: %s\n", daysToQuery);
		str += String.format("Facility to query: %s\n", this.getFacilityID());
		return str;
	}

}
