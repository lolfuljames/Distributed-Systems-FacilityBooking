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
	public Time(Day day, int hour, int minute) {
		// TODO Auto-generated constructor stub
//		this.day = day;
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
		
//		if (this.getDay() != t.getDay()) {
//			return false;
//		}
		if (this.getHour() != t.getHour()) {
			return false;
		}
		if (this.getMinute() != t.getMinute()) {
			return false;
		}
		return true;
	}

}
