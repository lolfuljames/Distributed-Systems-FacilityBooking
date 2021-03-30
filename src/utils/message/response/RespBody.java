/**
 * 
 */
package utils.message.response;

import utils.message.Body;

/**
 * @author c170011
 *
 */
public abstract class RespBody extends Body {
	public abstract String getErrorMessage();
	public abstract Object getPayload();
}
