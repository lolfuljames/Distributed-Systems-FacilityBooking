package utils;

import java.util.*;
import server.*;

public class QueryAvailabilityReqBody extends Body {

	public ArrayList<Day> days;
	public String facilityName;
	
	public QueryAvailabilityReqBody(ArrayList<Day> days, String facilityName) {
		// TODO Auto-generated constructor stub
		this.days = days;
		this.facilityName = facilityName;
	}
	
	public ArrayList<Day> getDays() {
		return this.days;
	}
	
	public String getFacilityName() {
		return this.facilityName;
	}
	
	public String toString() {
		String str = "";
		String daysToQuery = "";
		for (Day day : this.getDays()) {
			daysToQuery += String.format("%s ", day.toString());
		}
		str += String.format("Days to query: %s\n", daysToQuery);
		str += String.format("Facility to query: %s\n", this.getFacilityName());
		return str;
	}

}
