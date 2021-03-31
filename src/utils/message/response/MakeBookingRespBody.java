/**
 * 
 */
package utils.message.response;

import java.util.UUID;

/**
 * @author c170011
 *
 */
public class MakeBookingRespBody extends RespBody {
	
	public UUID bookingID;
	public String errorMessage;
	
	public MakeBookingRespBody() {
		super();
	}
	
	/**
	 * @param errorMessage
	 */
	public MakeBookingRespBody(String errorMessage, UUID bookingID) {
		this.bookingID = bookingID;
		this.errorMessage = errorMessage;
		// TODO Auto-generated constructor stub
	}
	
	public UUID getBookingID() {
		return this.bookingID;
	}
	
	public String toString() {
		String str = String.format("Error message: %s\n", this.getErrorMessage());
		str += String.format("Booking ID: %s\n", this.getBookingID().toString());
		return str;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public Object getPayload() {
		String str = "";
		if (!this.getErrorMessage().equals("")) {
			str += String.format("Booking failed! %s\n", this.getErrorMessage());
		} else {
			str += "Booking made successfully.\n";
			str += String.format("Booking ID: %s\n", this.getBookingID().toString());
		}
		return str;
	}

}
