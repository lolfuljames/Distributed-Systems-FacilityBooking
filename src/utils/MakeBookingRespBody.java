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
	
	public UUID bookingID;

	public MakeBookingRespBody() {
		super();
	}
	
	/**
	 * @param errorMessage
	 */
	public MakeBookingRespBody(String errorMessage, UUID bookingID) {
		super(errorMessage);
		this.bookingID = bookingID;
		// TODO Auto-generated constructor stub
	}

}
