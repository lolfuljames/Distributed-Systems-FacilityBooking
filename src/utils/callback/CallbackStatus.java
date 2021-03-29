package utils.callback;

public enum CallbackStatus {
	ACK_CALLBACK,
	EXPIRED_CALLBACK;
	
	public byte[] getBytes() {
		return this.name().getBytes();
	}
}