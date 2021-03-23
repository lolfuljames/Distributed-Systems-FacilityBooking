/**
 * 
 */
package util;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * @author jame0019
 *
 */
public class MonitorCallback implements Remote, Serializable{

	private int monitorFacilityType;
	private int monitorFacilityNumber;
	private int monitorInterval;
	/**
	 * @return 
	 * 
	 */
	
	public MonitorCallback(int monitorFacilityType, int monitorFacilityNumber, int monitorInterval) {
		this.monitorFacilityType = monitorFacilityType;
		this.monitorFacilityNumber = monitorFacilityNumber;
		this.monitorInterval = monitorInterval;
	}
	
	public void notify(String message) throws RemoteException {
		System.out.println("Hello callback called: " + message);
		System.out.println("Monitored Facility: " + this.monitorFacilityType + '-' + this.monitorFacilityNumber);
		System.out.println("Remaining days: " + this.monitorInterval);
	};
	
	public int getMonitorFacilityType() {
		return monitorFacilityType;
	}

	public int getMonitorFacilityNumber() {
		return monitorFacilityNumber;
	}

	public int getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(int monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	public static final long serialVersionUID = 42L;
}
