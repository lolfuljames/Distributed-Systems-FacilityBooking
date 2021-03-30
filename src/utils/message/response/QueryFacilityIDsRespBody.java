package utils.message.response;

import java.util.ArrayList;

public class QueryFacilityIDsRespBody extends RespBody {

	public ArrayList<String> facilityIDs;
	public String errorMessage;
	
	public QueryFacilityIDsRespBody() {
		
	}
	
	public QueryFacilityIDsRespBody(String errorMessage, ArrayList<String> facilityIDs) {
		this.errorMessage = errorMessage;
		this.facilityIDs = facilityIDs;
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> getFacilityIDs() {
		return this.facilityIDs;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public String toString() {
		String str = String.format("Error message: %s\n", this.getErrorMessage());
		String facilityIDs = "";
		for (String facility : this.getFacilityIDs()) {
			facilityIDs += String.format("%s ", facility);
		}
		str += String.format("Payload: %s\n", facilityIDs);
		return str;
	}
	
	public Object getPayload() {
		String str = "";
		for (String facility : this.getFacilityIDs()) {
			str += String.format("%s ", facility);
		}
		return str;
	}

}
