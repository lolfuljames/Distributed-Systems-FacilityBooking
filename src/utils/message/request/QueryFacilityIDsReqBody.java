/**
 * 
 */
package utils.message.request;

import java.util.ArrayList;

import server.Day;
import utils.message.Body;

/**
 * @author c170011
 *
 */
public class QueryFacilityIDsReqBody extends Body {
	
	public String facilityType;
	
	public QueryFacilityIDsReqBody() {
		super();
	}
	
	public QueryFacilityIDsReqBody(String facilityType) {
		super();
		this.facilityType = facilityType;
	}
	
	public String getFacilityType() {
		return this.facilityType;
	}

	@Override
	public String toString() {
		String str = "";
		str += String.format("Query facility IDs of %s\n", this.getFacilityType());
		return str;
	}
}
