package utils;

public class QueryAvailabilityRespBody extends RespBody {

	public String payload;
	
	public QueryAvailabilityRespBody(String errorMessage, String payload) {
		super(errorMessage);
		this.payload = payload;
		// TODO Auto-generated constructor stub
	}
	
	public String getPayLoad() {
		return this.payload;
	}
	
	public String toString() {
		String str = super.toString();
		str += String.format("Payload: %s\n", this.getPayLoad());
		return str;
	}

}
