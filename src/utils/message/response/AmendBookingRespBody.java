package utils.message.response;

import java.util.UUID;

public class AmendBookingRespBody extends RespBody {

	public String errorMessage;
	public UUID bookingID;
	
	public AmendBookingRespBody() {
	}
	
	public AmendBookingRespBody(String errorMessage, UUID bookingID) {
		this.errorMessage = errorMessage;
		this.bookingID = bookingID;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public Object getPayload() {
		return this.bookingID;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Error message: %s\n", this.getErrorMessage());
		return str;
	}
}
