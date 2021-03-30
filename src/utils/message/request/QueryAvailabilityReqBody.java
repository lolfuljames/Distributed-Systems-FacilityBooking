package utils.message.request;

import java.util.*;
import server.*;
import utils.message.Body;

public class QueryAvailabilityReqBody extends Body {

	public ArrayList<Day> days;
	public String facilityID;
	public String facilityType;
	public boolean IDBased;
	
	public QueryAvailabilityReqBody() {
		super();
	}
	
	public QueryAvailabilityReqBody(ArrayList<Day> days, String facilityID, String facilityType, boolean IDBased) {
		// TODO Auto-generated constructor stub
		this.days = days;
		this.facilityID = facilityID;
		this.facilityType = facilityType;
		this.IDBased = IDBased;
	}
	
	public ArrayList<Day> getDays() {
		return this.days;
	}
	
	public String getFacilityID() {
		return this.facilityID;
	}
	
	public String getFacilityType() {
		return this.facilityType;
	}
	
	public boolean getIDBased() {
		return this.IDBased;
	}
	
	public String toString() {
		String str = "";
		String daysToQuery = "";
		for (Day day : this.getDays()) {
			daysToQuery += String.format("%s ", day.toString());
		}
		str += String.format("Days to query: %s\n", daysToQuery);
		if (this.IDBased) {
			str += String.format("Facility to query: %s\n", this.getFacilityID());
		} else {
			str += String.format("Facility to query: %s\n", this.getFacilityType());			
		}
		return str;
	}

}
