/**
 * 
 */
package util;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * @author jame0019
 *
 */
public interface CallbackServer extends Remote{

	/**
	 * @return 
	 * 
	 */
	public void getBooking() throws RemoteException;
	public void addCallback(Callback callback) throws RemoteException;
	public void removeCallback(Callback callback) throws RemoteException;
}
