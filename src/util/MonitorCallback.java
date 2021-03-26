/**
 * 
 */
package util;

import java.net.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * @author jame0019
 *
 */
public class MonitorCallback implements Remote, Serializable {

	private String monitorFacilityType;
	private String monitorFacilityID;
	private int monitorInterval;
	private InetAddress address;
	private int port;
	/**
	 * @return 
	 * 
	 */
	
	public MonitorCallback(String facilityType, String monitorFacilityID, int monitorInterval) {
		this.monitorFacilityType = facilityType;
		this.monitorFacilityID = monitorFacilityID;
		this.monitorInterval = monitorInterval;
	}
	
	public String getMonitorFacilityType() {
		return monitorFacilityType;
	}

	public String getMonitorFacilityID() {
		return monitorFacilityID;
	}

	public int getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(int monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static final long serialVersionUID = 42L;
}
