/**
 * 
 */
package server;

import java.util.Comparator;

import server.TimePeriod;

/**
 * @author Poon6
 *
 */
public class BookingComparator implements Comparator<Booking>{

	@Override
	public int compare(Booking b1, Booking b2) {
		// TODO Auto-generated method stub
		return b1.getStartTime().compareTo(b2.getStartTime());
	}

}
