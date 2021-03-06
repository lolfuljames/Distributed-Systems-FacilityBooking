/**
 * 
 */
package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * @author c170011
 *
 */
public class Facility {

	private String type;
	private String facilityID;
	private Time earliestTime;
	private Time latestTime;
	private LinkedHashMap<Day, ArrayList<Booking>> bookings;

	/**
	 * 
	 */
	public Facility(String type, String facilityID, Time earliestTime, Time latestTime) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.facilityID = facilityID;
		this.earliestTime = earliestTime;
		this.latestTime = latestTime;
		this.bookings = new LinkedHashMap<Day, ArrayList<Booking>>();
		Arrays.asList(Day.values()).forEach(day -> {
			this.bookings.put(day, new ArrayList<Booking>());
		});
	}

	public String getType() {
		return this.type;
	}

	public String getFacilityID() {
		return this.facilityID;
	}

	public LinkedHashMap<Day, ArrayList<Booking>> getBookings() {
		return this.bookings;
	}

	public LinkedHashMap<Day, ArrayList<TimePeriod>> getAvailableTiming(ArrayList<Day> days) {
		LinkedHashMap<Day, ArrayList<TimePeriod>> availableTiming = new LinkedHashMap<Day, ArrayList<TimePeriod>>();
		days.forEach(day -> {
			availableTiming.put(day, this._getAvailableTiming(day));
		});

		return availableTiming;
	}

	private ArrayList<TimePeriod> _getAvailableTiming(Day day) {
		ArrayList<TimePeriod> availableTiming = new ArrayList<TimePeriod>();
		ArrayList<Booking> bookings = this.bookings.get(day);

		// if no existing bookings, just send whole day as available
		if (bookings.size() < 1) {
			availableTiming.add(new TimePeriod(this.earliestTime, this.latestTime));
		} else {
			// start - booking1 start || booking1 end - booking2 start || ... || booking_n end - end of day
			TimePeriod bookedPeriod = bookings.get(0).getTimePeriod();
			Time startTime = bookedPeriod.getStartTime();
			if (!this.earliestTime.equals(startTime)) {
				availableTiming.add(new TimePeriod(this.earliestTime, startTime));
			}
			Time endTime = bookedPeriod.getEndTime();

			for (int i = 1; i < bookings.size(); i++) {
				bookedPeriod = bookings.get(i).getTimePeriod();
				startTime = bookedPeriod.getStartTime();

				if (endTime.compareTo(startTime) != 0) {
					availableTiming.add(new TimePeriod(endTime, startTime));			
				}

				endTime = bookedPeriod.getEndTime();
			}

			if (!this.latestTime.equals(endTime)) {
				availableTiming.add(new TimePeriod(endTime, this.latestTime));
			}
		}

		return availableTiming;
	}

	private void sortBookings(ArrayList<Booking> bookings) {
		Collections.sort(bookings, new BookingComparator());
	}

	private boolean isBookable(Booking booking) throws BookingFailedException {
		// Check for unacceptable booking hours
		if (booking.getStartTime().compareTo(this.earliestTime) < 0) {
			throw new BookingFailedException("Start time is earlier than the facility opening hour.");
		} else if (booking.getEndTime().compareTo(this.latestTime) > 0) {
			throw new BookingFailedException("End time is later than the facility closing hour.");
		}
		
		ArrayList<Booking> bookings = this.bookings.get(booking.getDay());
		boolean startTimeSettled = false;
		
		// If no existing bookings, return true
		if (bookings.size() < 1) {
			return true;
		}

		// Check for the earliest and latest available time slot
		int firstBookingIdx = 0;
		int lastBookingIdx = bookings.size() - 1;
		if (bookings.get(firstBookingIdx) == booking) {
			if (bookings.size() < 2) {
				return true;
			} else {
				firstBookingIdx += 1;
			}
		} else if (bookings.get(lastBookingIdx) == booking) {
			if (bookings.size() < 2) {
				return true;
			} else {
				lastBookingIdx -= 1;
			}
		}
		if (booking.getEndTime().compareTo(bookings.get(firstBookingIdx).getStartTime()) <= 0) {
			return true;
		} else if (booking.getStartTime().compareTo(bookings.get(lastBookingIdx).getEndTime()) >= 0) {
			return true;
		}

		// Check for available slots in between existing bookings
		for (Booking bookedSlot : bookings) {
			if (bookedSlot == booking) {
				continue;
			}
			if (startTimeSettled) {
				if (booking.getEndTime().compareTo(bookedSlot.getStartTime()) <= 0) {
					return true;
				} else {
					if (booking.getStartTime().compareTo(bookedSlot.getEndTime()) < 0) {
						startTimeSettled = false;
					}
				}
			} else {
				if (booking.getStartTime().compareTo(bookedSlot.getEndTime()) >= 0) {
					startTimeSettled = true;
				} else {
					// Can't find a slot in between existing bookings
					return false;
				}
			}
		}

		return false;
	}

	public boolean addBooking(Booking booking) throws BookingFailedException {

		if (!this.isBookable(booking)) {
			return false;
		}

		this.bookings.get(booking.getDay()).add(booking);
		this.sortBookings(this.bookings.get(booking.getDay()));
		return true;
	}
	
	/* Status code:
	 * 0 -> Successful
	 * 1 -> Not successful (no available slot for amended booking)
	 * 2 -> Not successful (amendment is not acceptable, only amendment within the day itself is accepted, i.e. the offset is too large)
	 * 3 -> Not successful (amendment is not acceptable, only amendment within the operating hours of the facility is accepted)
	*/ 
	public int amendBooking(Booking booking, int offset) {
		int statusCode;
		boolean amendable = false;
		Time originalStartTime = booking.getStartTime();
		Time originalEndTime = booking.getEndTime();
		Time amendedStartTime = null;
		Time amendedEndTime = null;
		try {
			// shift timings earlier
			if (offset < 0) {
				amendedStartTime = booking.getStartTime().add(offset);
				amendedEndTime = booking.getEndTime().add(offset);
				booking.setStartTime(amendedStartTime);
				booking.setEndTime(amendedEndTime);
				amendable = this.isBookable(booking);
			} else {
				// shift timings later
				amendedStartTime = booking.getStartTime().add(offset);
				amendedEndTime = booking.getEndTime().add(offset);
				booking.setStartTime(amendedStartTime);
				booking.setEndTime(amendedEndTime);
				amendable = this.isBookable(booking);
			}
		} catch (TimeErrorException e) {
			booking.setStartTime(originalStartTime);
			booking.setEndTime(originalEndTime);
			return 2;
		} catch (BookingFailedException e) {
			booking.setStartTime(originalStartTime);
			booking.setEndTime(originalEndTime);
			return 3;
		}
		
		if (!amendable) {
			booking.setStartTime(originalStartTime);
			booking.setEndTime(originalEndTime);
			statusCode = 1;
		} else {
			this.sortBookings(this.bookings.get(booking.getDay()));
			statusCode = 0;
		}
		return statusCode;
	}
	
	public int extendBooking(Booking booking, int minutesToExtend) {
		int statusCode;
		boolean amendable = false;
		Time originalStartTime = booking.getStartTime();
		Time originalEndTime = booking.getEndTime();
		Time amendedStartTime = booking.getStartTime();
		Time amendedEndTime;
		try {
			amendedEndTime = booking.getEndTime().add(minutesToExtend);
			booking.setStartTime(amendedStartTime);
			booking.setEndTime(amendedEndTime);
			amendable = this.isBookable(booking);
		} catch (TimeErrorException e) {
			booking.setStartTime(originalStartTime);
			booking.setEndTime(originalEndTime);
			return 2;
		} catch (BookingFailedException e) {
			booking.setStartTime(originalStartTime);
			booking.setEndTime(originalEndTime);
			return 3;
		}
		if (!amendable) {
			booking.setStartTime(originalStartTime);
			booking.setEndTime(originalEndTime);
			statusCode = 1;
		} else {
			statusCode = 0;
		}
		return statusCode;
	}

	public void cancelBooking(Booking booking) {
		this.bookings.get(booking.getDay()).remove(booking);
	}

}
