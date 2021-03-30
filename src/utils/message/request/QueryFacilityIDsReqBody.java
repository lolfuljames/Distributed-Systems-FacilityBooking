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
	
	public String facilityName;
	
	public QueryFacilityIDsReqBody() {
		super();
	}
	
	public QueryFacilityIDsReqBody(String facilityName) {
		super();
		this.facilityName = facilityName;
	}
	
	public String getFacilityName() {
		return this.facilityName;
	}

	@Override
	public String toString() {
		String str = "";
		str += String.format("Query facility IDs of %s\n", this.getFacilityName());
		return str;
	}
}
