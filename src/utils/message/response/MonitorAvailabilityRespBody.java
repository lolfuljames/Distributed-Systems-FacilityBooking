package utils.message.response;

public class MonitorAvailabilityRespBody extends RespBody {

	private String payload;
	public String errorMessage;

	public MonitorAvailabilityRespBody() {}
	
	public MonitorAvailabilityRespBody(String errorMessage, String payload) {
		this.errorMessage = errorMessage;
		this.setPayload(payload);
		// TODO Auto-generated constructor stub
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	public String toString() {
		String str = String.format("Error message: %s\n", this.getErrorMessage());
		str += String.format("Payload: %s\n", this.getPayload());
		return str;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}
