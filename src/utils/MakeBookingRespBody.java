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

	/**
	 * @param errorMessage
	 */
	public MakeBookingRespBody(String errorMessage, UUID bookingID) {
		super(errorMessage);
		this.bookingID = bookingID;
		// TODO Auto-generated constructor stub
	}
	
	public UUID getBookingID() {
		return this.bookingID;
	}
	
	public String toString() {
		String str = super.toString();
		str += String.format("Booking ID: %s\n", this.getBookingID().toString());
		return str;
	}

}
