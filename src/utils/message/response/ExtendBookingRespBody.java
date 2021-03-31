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
	
	public UUID getBookingID() {
		return this.bookingID;
	}
	
	public Object getPayload() {
		String str = "";
		if (!this.getErrorMessage().equals("")) {
			str += String.format("Booking extension failed! %s\n", this.getErrorMessage());
		} else {
			str += "Booking has been extended successfully.\n";
			str += String.format("Booking ID: %s\n", this.getBookingID().toString());
		}
		return str;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Error message: %s\n", this.getErrorMessage());
		return str;
	}
}
