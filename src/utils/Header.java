/**
 * 
 */
package utils;

import java.util.UUID;

/**
 * @author c170011
 *
 */
public class Header {
	
	public UUID messageID;
	public int opCode;
	public int messageType;

	/**
	 * 
	 */
	public Header() {	
	}
	
	public Header(UUID messageID, int opCode, int messageType) {
		this.messageID = messageID;
		this.opCode = opCode;
		this.messageType = messageType;
		// TODO Auto-generated constructor stub
	}
	
	public int getOpCode() {
		return this.opCode;
	}

}
