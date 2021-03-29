package utils;

import java.util.UUID;

public class AmendBookingReqBody extends Body {

	private UUID bookingID;
	private int offset;
	
	public AmendBookingReqBody() {
	}
	
	public AmendBookingReqBody(UUID bookingID, int offset) {
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
		str += String.format("Offset in minute: %d\n", this.getOffset());
		return str;
	}
}
