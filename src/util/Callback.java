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
public class Callback implements Remote, Serializable{

	/**
	 * 
	 */
	public void notify(String message) throws RemoteException {
		System.out.println("Hello callback called: " + message);
	};
	
	public static final long serialVersionUID = 42L;
}
