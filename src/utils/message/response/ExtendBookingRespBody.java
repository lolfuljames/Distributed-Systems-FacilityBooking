package utils.message.response;

import java.util.UUID;

public class ExtendBookingRespBody extends RespBody {

	public String errorMessage;
	public UUID bookingID;
	
	public ExtendBookingRespBody() {
	}
	
	public ExtendBookingRespBody(String errorMessage, UUID bookingID) {
		this.errorMessage = errorMessage;
		this.bookingID = bookingID;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Error message: %s\n", this.getErrorMessage());
		return str;
	}
	
	public Object getPayload() {
		return this.bookingID;
	}
}
