/**
 * 
 */
package util;

import java.rmi.Remote;
import java.io.*;
import java.rmi.RemoteException;
/**
 * @author jame0019
 *
 */
public interface CallbackServer extends Remote {

	/**
	 * @return 
	 * 
	 */
	public void getBooking() throws RemoteException;
	public void addCallback(MonitorCallback callback) throws IOException;
	public void removeCallback(MonitorCallback callback) throws IOException;
}
