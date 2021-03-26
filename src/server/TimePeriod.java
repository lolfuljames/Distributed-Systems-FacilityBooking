/**
 * 
 */
package server;

/**
 * @author c170011
 *
 */
public class TimePeriod {
	
	private Time startTime;
	private Time endTime;

	/**
	 * 
	 */
	public TimePeriod(Time startTime, Time endTime) {
		// TODO Auto-generated constructor stub
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public Time getStartTime() {
		return this.startTime;
	}

	public Time getEndTime() {
		return this.endTime;
	}
	
	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}
	
	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

}
