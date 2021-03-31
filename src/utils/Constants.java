/**
 * 
 */
package utils;

import java.util.UUID;

/**
 * @author c170011
 *
 */
public final class Constants {
	public final static int REQUEST = 0;
	public final static int RESPONSE = 1;
	public final static int QUERY_AVAILABILITY = 0;
	public final static int MAKE_BOOKING = 1;
	public final static int AMEND_BOOKING = 2;
	public final static int MONITOR_AVAILABILITY = 3;
	public final static int CANCEL_BOOKING = 4;
	public final static int EXTEND_BOOKING = 5;
	public final static int QUERY_FACILITY_TYPES = 6;
	public final static int QUERY_FACILITY_IDS = 7;
	public final static UUID INVALID_UUID = new UUID(0, 0);
	public final static int TIMEOUT_MS = 1000;
	public final static int TIMEOUT_MS_CALLBACK = 100;
	public final static int SERVER_DEFAULT_TIMEOUT_MS = 7 * 24 * 60 * 1000;
	public final static double PACKET_LOSS_THRESHOLD_SERVER = 0.5;
	public final static double PACKET_LOSS_THRESHOLD_CLIENT = 0;
	public final static String ACK_CALLBACK = "ACK_CALLBACK";
}
