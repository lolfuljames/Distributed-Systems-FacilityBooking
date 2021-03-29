package utils;

import java.util.*;
import server.*;

public class MonitorAvailabilityReqBody extends Body {

	private MonitorCallback monitorCallback;
	
	public MonitorAvailabilityReqBody() {}

	public MonitorAvailabilityReqBody(MonitorCallback monitorCallback) {
		// TODO Auto-generated constructor stub
		this.setMonitorCallback(monitorCallback);
	}

	public MonitorCallback getMonitorCallback() {
		return monitorCallback;
	}

	public void setMonitorCallback(MonitorCallback monitorCallback) {
		this.monitorCallback = monitorCallback;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Monitor Callback: %s\n", this.getMonitorCallback().toString());
		return str;
	}

}
