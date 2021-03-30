package utils.message.response;

import java.util.ArrayList;

public class QueryFacilityTypesRespBody extends RespBody {

	public ArrayList<String> facilityTypes;
	public String errorMessage;
	
	public QueryFacilityTypesRespBody() {
		
	}
	
	public QueryFacilityTypesRespBody(String errorMessage, ArrayList<String> facilityTypes) {
		this.errorMessage = errorMessage;
		this.facilityTypes = facilityTypes;
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> getFacilityTypes() {
		return this.facilityTypes;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public Object getPayload() {
		String str = "";
		for (String facility : this.getFacilityTypes()) {
			str += String.format("%s ", facility);
		}
		return str;
	}
	
	public String toString() {
		String str = String.format("Error message: %s\n", this.getErrorMessage());
		String facilityTypes = "";
		for (String facility : this.getFacilityTypes()) {
			facilityTypes += String.format("%s ", facility);
		}
		str += String.format("Payload: %s\n", facilityTypes);
		return str;
	}

}
