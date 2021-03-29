/**
 * 
 */
package utils;

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

}
