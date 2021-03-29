/**
 * 
 */
package utils;

import java.net.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * @author jame0019
 *
 */
public class MonitorCallback implements Remote, Serializable {
	private String monitorFacilityID;
	private int monitorInterval;
	private InetAddress address;
	private int port;
	/**
	 * @return 
	 * 
	 */
	public MonitorCallback() {
		
	}
	
	public MonitorCallback( String monitorFacilityID, int monitorInterval, InetAddress address, int port) {
		this.monitorFacilityID = monitorFacilityID;
		this.monitorInterval = monitorInterval;
		this.address = address;
		this.port = port;
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
	
	public String toString() {
		String str = "";
		str += String.format("Monitoring callback requested by %s (IP) at port %d\n", this.getAddress().toString(), this.getPort());
		str += String.format("Monitoring %s for %d minutes\n", this.getMonitorFacilityID(), this.getMonitorInterval());
		return str;
	}
}
