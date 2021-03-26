package utils;

public class MonitorAvailabilityRespBody extends RespBody {

	public String payload;
	
	public MonitorAvailabilityRespBody(String errorMessage, String payload) {
		super(errorMessage);
		this.payload = payload;
		// TODO Auto-generated constructor stub
	}

}
