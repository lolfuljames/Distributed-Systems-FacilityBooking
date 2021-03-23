package utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Serializer {

	public static ByteBuffer serialize(Object obj, ByteBuffer buffer) {
		
		buffer.clear(); // Set pointer to position 0
		buffer.order(ByteOrder.LITTLE_ENDIAN); // Set Little Indian Byte order

		try {
			writeObject(obj, buffer);
		} catch (BufferOverflowException ex) {
			System.out.println("Buffer Overflowed, " + new String(ex.getMessage()));
			ex.printStackTrace();
		}
		
		return buffer;
	}
	
	private static void writeObject(Object obj, ByteBuffer buffer) {
		int pos = buffer.position();
		
		// https://stackoverflow.com/questions/2309207/manual-object-serialization-in-java
		// Refer this for why we exclude transient
	
		try {
			if(obj instanceof Serializable && obj != null) {
				writeSerializable(obj, buffer);
			} else {
				Field[] fields = obj.getClass().getFields();
				
			}			
		} catch (IOException ex) {
			System.out.println("IOException: " + new String(ex.getMessage()));
		}

	}
	
	private static void writeSerializable(Object obj, ByteBuffer buffer) throws IOException {
		ByteArrayOutputStream bOS = new ByteArrayOutputStream();
		ObjectOutputStream oOS = new ObjectOutputStream(bOS);
		
		oOS.writeObject(obj); // Convert obj and save to bOS
		oOS.flush();
		oOS.close();
		
		buffer.put(bOS.toByteArray());
	}
}
