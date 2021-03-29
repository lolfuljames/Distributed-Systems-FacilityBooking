/**
 * 
 */
package utils.message;

import java.util.UUID;

/**
 * @author c170011
 *
 */
public class Header {
	
	public UUID messageID;
	public int opCode;
	
	/**
	 * Indicator of request/response:
	 * 0 -> Request
	 * 1 -> Response
	 */
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

	public UUID getMessageID() {
		return messageID;
	}
	
	public int getMessageType() {
		return this.messageType;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Message ID: %s\n", this.getMessageID().toString());
		str += String.format("OpCode: %d\n", this.getOpCode());
		return str;
	}

}
