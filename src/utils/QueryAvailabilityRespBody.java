package utils;

public class QueryAvailabilityRespBody extends RespBody {

	public String payload;
	
	public QueryAvailabilityRespBody(String errorMessage, String payload) {
		super(errorMessage);
		this.payload = payload;
		// TODO Auto-generated constructor stub
	}

}
