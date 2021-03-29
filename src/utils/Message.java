/**
 * 
 */
package utils;

/**
 * @author c170011
 *
 */
public class Message {
	
	private Header header;
	private Body body;

	/**
	 * 
	 */
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
		str += this.getHeader().toString();
		str += this.getBody().toString();
		return str;
	}

}
