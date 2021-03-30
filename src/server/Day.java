/**
 * 
 */
package server;

import java.util.ArrayList;

/**
 * @author c170011
 *
 */
public enum Day {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY,
	SUNDAY;
	
	public static ArrayList<Day> getAllDays() {
		ArrayList<Day> days = new ArrayList<Day>();
		days.add(MONDAY);
		days.add(TUESDAY);
		days.add(WEDNESDAY);
		days.add(THURSDAY);
		days.add(FRIDAY);
		days.add(SATURDAY);
		days.add(SUNDAY);
		return days;
	}
}
