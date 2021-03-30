package utils.message.response;

public class QueryAvailabilityRespBody extends RespBody {

	public String payload;
	public String errorMessage;
	
	public QueryAvailabilityRespBody() {
		
	}
	
	public QueryAvailabilityRespBody(String errorMessage, String payload) {
		this.errorMessage = errorMessage;
		this.payload = payload;
		// TODO Auto-generated constructor stub
	}
	
	public Object getPayload() {
		return this.payload;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public String toString() {
		String str = String.format("Error message: %s\n", this.getErrorMessage());
		str += String.format("Payload: %s\n", this.getPayload());
		return str;
	}

}
