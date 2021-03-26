package utils;

import java.util.UUID;

public class AmendBookingReqBody extends Body {

	public UUID bookingID;
	public int offset;
	
	public AmendBookingReqBody() {
	}
	
	public AmendBookingReqBody(UUID bookingID, int offset) {
		super();
		this.bookingID = bookingID;
		this.offset = offset;
		// TODO Auto-generated constructor stub
	}
}
