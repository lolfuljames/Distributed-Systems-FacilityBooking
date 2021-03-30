package utils.message.request;

import java.util.UUID;

import utils.message.Body;

public class CancelBookingReqBody extends Body {

	private UUID bookingID;
	
	public CancelBookingReqBody() {
	}
	
	public CancelBookingReqBody(UUID bookingID) {
		super();
		this.setBookingID(bookingID);
	}

	public UUID getBookingID() {
		return bookingID;
	}

	public void setBookingID(UUID bookingID) {
		this.bookingID = bookingID;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Booking ID to be cancelled: %s\n", this.getBookingID().toString());
		return str;
	}
}
