package utils;

import java.util.*;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class Serializer {

	public static ByteBuffer serialize(Object obj, ByteBuffer buffer) {
		
		buffer.clear(); // Set pointer to position 0
		buffer.order(ByteOrder.LITTLE_ENDIAN); // Set Little Indian Byte order

		Class<?> objClass = obj.getClass();
		System.out.println(objClass);
		
		write(obj, buffer);
		
		return buffer;
	}
	
	public static void write(Object obj, ByteBuffer buffer) {
		/*
		 * Recursively write
		 */
		
		if(obj instanceof String) {
			byte[] byteString = ((String) obj).getBytes(StandardCharsets.UTF_8);
			buffer.putInt(byteString.length); // record the length to recover the string
			buffer.put(byteString); // put the bytestring into buffer
		}else if(obj instanceof Integer) {
			buffer.putInt((Integer) obj);
		}else if(obj.getClass().isEnum()) {
			buffer.put((byte) ((Enum<?>) obj).ordinal());
		}else if(obj instanceof Iterable<?>) {
			write(((Iterable<?>) obj), buffer);
		}else if(obj instanceof UUID) {
			write(((UUID) obj), buffer);
		}else {
			System.out.println("Not a valid type");
		}
	}
	
	public static void write(Iterable<?> objects, ByteBuffer buffer) {
		/*
		 * Loop through iterable and serialize recursively.
		 */
		
		objects.forEach(obj -> write(obj, buffer));
		
		
	}
	
	public static void write(UUID uuid, ByteBuffer buffer) {
		/*
		 *  https://stackoverflow.com/questions/17893609/convert-uuid-to-byte-that-works-when-using-uuid-nameuuidfrombytesb
		 */
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
	}

	public static void main(String[] args) {

		ArrayList<Employee> employeeList = new ArrayList<Employee>();
		employeeList.add(new Employee("TJL"));
		employeeList.add(new Employee("ABC"));
		ByteBuffer buf = ByteBuffer.allocate(2048);
//		buf = Serializer.serialize(999, buf);
//		System.out.println(Deserializer.deserialize(buf, Integer.class));
		
		System.out.println("Testing Integer serialization and deserialization");
		buf = Serializer.serialize(123, buf);
		assert 123 == Deserializer.deserialize(buf, Integer.class);
		
		System.out.println("Testing String serialization and deserialization");
		buf = Serializer.serialize("ABCDEF", buf);
		assert "ABCDEF".equals(Deserializer.deserialize(buf, String.class));
		 
		System.out.println("Testing ENUM serialization and deserialization");
		buf = Serializer.serialize(Day.FRIDAY, buf);
		assert Day.FRIDAY == Deserializer.deserialize(buf, Enum.class);		
		
		System.out.println("Testing UUID serialization and deserialization");
		UUID randomUUID = UUID.randomUUID();
		System.out.println(randomUUID);
		buf = Serializer.serialize(randomUUID, buf);
		assert randomUUID.equals(Deserializer.deserialize(buf, UUID.class));
		
//		ArrayList<Integer> numberList = new ArrayList<Integer>();
//
//		numberList.add(1);
//		numberList.add(2);
//		numberList.add(3);
//		buf = Serializer.serialize(numberList, buf);
//		System.out.println(buf);
//		System.out.println(Deserializer.deserialize(buf, Iterable.class));		

		// Method for deserialization of object
//		try {
//		buf = Serializer.serialize(new Employee("TJL"), buf);
			// buf.clear();
//		System.out.println(buf);
//			String object1 = (String)in.readObject();
//			Employee object1 = (Employee)in.readObject();
//			System.out.println(object1.name);
//			assert object1.equals("abcdef");
//		} catch (IOException ex){
//			System.out.println("IOException");
//			ex.printStackTrace();
//		} catch (ClassNotFoundException ex){
//			System.out.println("Class Not Found");
//		}
	}
}
