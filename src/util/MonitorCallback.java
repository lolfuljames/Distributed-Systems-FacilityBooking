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
public class MonitorCallback implements Remote, Serializable{

	private int monitorFacilityType;
	private int monitorFacilityNumber;
	private int monitorInterval;
	private InetAddress address;
	private int port;
	/**
	 * @return 
	 * 
	 */
	
	public MonitorCallback(int monitorFacilityType, int monitorFacilityNumber, int monitorInterval) {
		this.monitorFacilityType = monitorFacilityType;
		this.monitorFacilityNumber = monitorFacilityNumber;
		this.monitorInterval = monitorInterval;
	}
	
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
