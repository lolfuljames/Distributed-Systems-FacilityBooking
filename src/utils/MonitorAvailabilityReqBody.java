package utils;

import java.util.*;
import server.*;

public class MonitorAvailabilityReqBody extends Body {

	public MonitorCallback monitorCallback;
	
	public MonitorAvailabilityReqBody(MonitorCallback monitorCallback) {
		// TODO Auto-generated constructor stub
		this.monitorCallback = monitorCallback;
	}

}
