package utils;

import java.util.*;
import server.*;

public class QueryAvailabilityReqBody extends Body {

	public ArrayList<Day> days;
	public String facilityName;
	
	public QueryAvailabilityReqBody() {
		super();
	}
	
	public QueryAvailabilityReqBody(ArrayList<Day> days, String facilityName) {
		// TODO Auto-generated constructor stub
		this.days = days;
		this.facilityName = facilityName;
	}

}
