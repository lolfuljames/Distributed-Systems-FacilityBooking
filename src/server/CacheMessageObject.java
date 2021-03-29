package server;

import utils.*;
import java.time.Instant;

public class CacheMessageObject {
	
	// messages are only stored for 2 minutes
	private static final long MESSAGE_TTL = 120;
	private Message message;
	private long lastAccessedTime;
	
	public CacheMessageObject(Message message, long lastAccessedTime) throws TimeErrorException {
		// TODO Auto-generated constructor stub
	}

	public Message getMessage() {
		updateLastAccessedTime();
		return message;
	}
	
	private void updateLastAccessedTime() {
		lastAccessedTime = Instant.now().getEpochSecond();
	}
	
	public boolean isExpiredMessage() {
		boolean isExpired = (Instant.now().getEpochSecond() - lastAccessedTime) > MESSAGE_TTL;
		return isExpired;
	}
}
