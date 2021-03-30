package utils.message.response;

import java.util.ArrayList;

public class QueryFacilityIDsRespBody extends RespBody {

	public ArrayList<String> facilityIDs;
	
	public QueryFacilityIDsRespBody() {
		
	}
	
	public QueryFacilityIDsRespBody(String errorMessage, ArrayList<String> facilityIDs) {
		super(errorMessage);
		this.facilityIDs = facilityIDs;
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> getFacilityIDs() {
		return this.facilityIDs;
	}
	
	public String toString() {
		String str = super.toString();
		String facilityIDs = "";
		for (String facility : this.getFacilityIDs()) {
			facilityIDs += String.format("%s ", facility);
		}
		str += String.format("Payload: %s\n", facilityIDs);
		return str;
	}

}
