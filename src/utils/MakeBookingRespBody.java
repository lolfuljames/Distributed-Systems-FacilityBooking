/**
 * 
 */
package utils;

import java.util.UUID;

/**
 * @author c170011
 *
 */
public class MakeBookingRespBody extends RespBody {
	
	public UUID payload;

	/**
	 * @param errorMessage
	 */
	public MakeBookingRespBody(String errorMessage, UUID bookingID) {
		super(errorMessage);
		this.payload = bookingID;
		// TODO Auto-generated constructor stub
	}

}
