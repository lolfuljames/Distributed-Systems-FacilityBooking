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
	
	public UUID getBookingID() {
		return this.bookingID;
	}
	
	public Object getPayload() {
		String str = "";
		if (!this.getErrorMessage().equals("")) {
			str += String.format("Booking cancellation failed! %s\n", this.getErrorMessage());
		} else {
			str += "Booking is cancelled successfully.\n";
		}
		return str;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Error message: %s\n", this.getErrorMessage());
		return str;
	}
}
