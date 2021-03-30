package utils.message.response;

import java.util.UUID;

public class CancelBookingRespBody extends RespBody {

	public String errorMessage;
	public UUID bookingID;
	
	public CancelBookingRespBody() {
	}
	
	public CancelBookingRespBody(String errorMessage, UUID bookingID) {
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
