/**
 * 
 */
package utils;

/**
 * @author c170011
 *
 */
public class RespBody extends Body {
	
	public String errorMessage;

	/**
	 * 
	 */
	public RespBody(String errorMessage) {
		this.errorMessage = errorMessage;
		// TODO Auto-generated constructor stub
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public String toString() {
		String str = "";
		str += String.format("Error message: %s\n", this.getErrorMessage());
		return str;
	}

}
