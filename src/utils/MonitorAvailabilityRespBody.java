package utils;

public class MonitorAvailabilityRespBody extends RespBody {

	private String payload;
	
	public MonitorAvailabilityRespBody(String errorMessage, String payload) {
		super(errorMessage);
		this.setPayload(payload);
		// TODO Auto-generated constructor stub
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

}