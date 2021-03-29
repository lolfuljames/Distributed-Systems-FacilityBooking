package utils.message.response;

import java.util.ArrayList;

public class QueryFacilityTypesRespBody extends RespBody {

	public ArrayList<String> facilityTypes;
	
	public QueryFacilityTypesRespBody() {
		
	}
	
	public QueryFacilityTypesRespBody(String errorMessage, ArrayList<String> facilityTypes) {
		super(errorMessage);
		this.facilityTypes = facilityTypes;
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> getFacilityTypes() {
		return this.facilityTypes;
	}
	
	public String toString() {
		String str = super.toString();
		String facilityTypes = "";
		for (String facility : this.getFacilityTypes()) {
			facilityTypes += String.format("%s ", facility);
		}
		str += String.format("Payload: %s\n", facilityTypes);
		return str;
	}

}
