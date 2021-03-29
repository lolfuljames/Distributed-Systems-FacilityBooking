package utils.message.request;

import java.util.UUID;

import utils.message.Body;

public class ExtendBookingReqBody extends Body {

	private UUID bookingID;
	private int offset;
	
	public ExtendBookingReqBody() {
	}
	
	public ExtendBookingReqBody(UUID bookingID, int offset) {
		super();
		this.setBookingID(bookingID);
		this.setOffset(offset);
		// TODO Auto-generated constructor stub
	}

	public UUID getBookingID() {
		return bookingID;
	}

	public void setBookingID(UUID bookingID) {
		this.bookingID = bookingID;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Booking ID: %s\n", this.getBookingID().toString());
		str += String.format("Extended for: %d minutes\n", this.getOffset());
		return str;
	}
}
