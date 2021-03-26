package utils;

import java.util.UUID;

public class AmendBookingReqBody extends Body {

	private UUID bookingID;
	private int offset;
	
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
}
