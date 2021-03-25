/**
 * 
 */
package server;

/**
 * @author c170011
 *
 */
public class Time {
	
//	private Day day;
	private int hour;
	private int minute;

	/**
	 * 
	 */
	public Time(int hour, int minute) throws TimeErrorException {
		// TODO Auto-generated constructor stub
		if (hour > 24 || hour < 0 || minute > 60 || minute < 0) {
			throw new TimeErrorException("Failed to construct Time object. Time is invalid in 24-hour format.");
		}
		this.hour = hour;
		this.minute = minute;
	}
	
//	public Day getDay() {
//		return this.day;
//	}
	
	public int getHour() {
		return this.hour;
	}
	
	public int getMinute() {
		return this.minute;
	}
	
	public Time convertOffsetToTime(int offset) {
		int hour = offset / 60;
		int minute = offset % 60;
		Time res = null;
		try {
			res = new Time(hour, minute);
		} catch (TimeErrorException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public Time add(int offset) throws TimeErrorException {
		if (offset < 0) {
			Time _offset = this.convertOffsetToTime(-offset);
			return this.subtract(_offset);
		} else {
			Time _offset = this.convertOffsetToTime(offset);
			return this.add(_offset);
		}
	}
	
	// Allows only single day addition
	// i.e. if the resultant time is the next day, TimeErrorException is thrown
	public Time add(Time t) throws TimeErrorException {
		int hour = this.getHour() + t.getHour() + (this.getMinute() + t.getMinute()) / 60;
		int minute = (this.getMinute() + t.getMinute()) % 60;
		return new Time(hour, minute);
	}
	
	// Allows only single day subtraction
	// i.e. if the resultant time is the previous day, TimeErrorException is thrown
	public Time subtract(Time t) throws TimeErrorException {
		int hour = this.getHour() - t.getHour();
		int minute = this.getMinute() - t.getMinute();
		if (minute < 0) {
			minute = 60 + minute;
			hour = hour - 1;
		}
		return new Time(hour, minute);
	}
	
	@Override
	public String toString() {
		String hour = String.valueOf(this.getHour());
		if (this.getHour() < 10) {
			hour = '0' + hour;
		}
		String minute = String.valueOf(this.getMinute());
		if (this.getMinute() < 10) {
			minute = '0' + minute;
		}
		String time = hour + ":" + minute;
		return time;
//		return this.day.toString() + " " + time;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof Time)) {
			return false;
		}
		
		Time t = (Time) o;
		
		if (this.getHour() != t.getHour()) {
			return false;
		}
		if (this.getMinute() != t.getMinute()) {
			return false;
		}
		return true;
	}
	
	public int compareTo(Time t) {
		if (t == this) {
			return 0;
		}
		
		if (this.getHour() > t.getHour()) {
			return 1;
		} else if (this.getHour() < t.getHour()) {
			return -1;
		} else {
			if (this.getMinute() > t.getMinute()) {
				return 1;
			} else if (this.getMinute() < t.getMinute()) {
				return -1;
			}
		}
		
		 return 0;
	}

//	public static void main(String[] args) {
//		try {
//			Time a = new Time(3, 22);
//			Time b = new Time(1, 10);
//			int offset = -203;
//			Time c = a.add(offset);
//			System.out.println(c);
//		} catch (TimeErrorException e) {
//			e.printStackTrace();
//		}
//	}
}
