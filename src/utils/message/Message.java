/**
 * 
 */
package utils.message;

/**
 * @author c170011
 *
 */
public class Message {
	
	public Header header;
	public Body body;

	/**
	 * 
	 */
	public Message() {
		
	}
	
	public Message(Header header, Body body) {
		this.setHeader(header);
		this.setBody(body);
		// TODO Auto-generated constructor stub
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}
	
	public String toString() {
		String str = "";
		str += "Header:\n";
		str += this.getHeader().toString();
		str += "--------------------------------------------\n";
		str += "Body:\n";
		str += this.getBody().toString();
		str += "--------------------------------------------\n";
		return str;
	}

}
