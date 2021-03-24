package utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Deserializer {

	public Deserializer() {
		// TODO Auto-generated constructor stub
	}
	
	public static <T> T deserialize(ByteBuffer buffer, Class<T> clazz) {
		
		/*
		 * https://stackoverflow.com/questions/14034421/how-to-return-the-proper-object-type-after-deserialization
		 */
		
		buffer.clear(); // Set pointer to position 0
		buffer.order(ByteOrder.LITTLE_ENDIAN); // Set Little Indian Byte order
		
		Object obj = read(clazz, buffer);
		
		return (T) obj;
	}
	
	public static <T> Object read(Class<T> clazz, ByteBuffer buffer) {
		if(clazz == Integer.TYPE || clazz == Integer.class) {
			return (Object) readInt(buffer);
		} else if(clazz == UUID.class) {
			return (Object) readUUID(buffer);
		} else if(clazz == String.class) {
			return (Object) readString(buffer);
		} else if(clazz == Enum.class ) {
			return (Object) readEnum(buffer);
		} else {
			return (Object) read(clazz, buffer);
		}
	}
	
	public static int readInt(ByteBuffer buffer) {
		return buffer.getInt();
	}
	
	public static String readString(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte[] byteString = new byte[length]; 

		buffer.get(byteString, 0, length);
		return new String(byteString, StandardCharsets.UTF_8);
	}
	
	public static Enum readEnum(ByteBuffer buffer) {
		int idx = buffer.get();
		if(idx >= Day.values().length) {
			throw new DeserializationException(
				"Having issue deserializing enum: Index out of Enum range"
			);
		}
		return Day.values()[idx];
	}
	
	public static UUID readUUID(ByteBuffer buffer) {
		long high = buffer.getLong();
		long low = buffer.getLong();
		
		return new UUID(high, low);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
