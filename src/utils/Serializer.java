package utils;

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.io.*;

import server.*;

public class Serializer {

	public static ByteBuffer serialize(Object obj, ByteBuffer buffer)
			throws IllegalArgumentException, IllegalAccessException {

		buffer.clear(); // Set pointer to position 0
		buffer.order(ByteOrder.LITTLE_ENDIAN); // Set Little Indian Byte order

		write(obj, buffer);

		return buffer;
	}

	public static void write(Object obj, ByteBuffer buffer) throws IllegalArgumentException, IllegalAccessException {
		/*
		 * Recursively write
		 */
		System.out.println("Serializing " + obj.getClass());
		if (obj instanceof String) {
			byte[] byteString = ((String) obj).getBytes(StandardCharsets.UTF_8);
			buffer.putInt(byteString.length); // record the length to recover the string
			buffer.put(byteString); // put the bytestring into buffer
		} else if (obj instanceof Double) {
			buffer.putDouble((Double) obj);
		} else if (obj instanceof Boolean) {
			buffer.put((byte) (((Boolean) obj) ? 1 : 0));
		} else if (obj instanceof Integer) {
			buffer.putInt((Integer) obj);
		} else if (obj.getClass().isEnum()) {
			buffer.put((byte) ((Enum<?>) obj).ordinal());
		} else if (obj instanceof List<?>) {
			buffer.putInt(((List<?>) obj).size());
			write(((List<?>) obj), buffer);
		} else if (obj instanceof UUID) {
			write(((UUID) obj), buffer);
		} else {
			System.out.println(obj);
			Class<?> objClass = obj.getClass();
			Field[] fields = objClass.getFields();
			for (Field field : fields) {
				int modifiers = field.getModifiers();
				if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && Modifier.isPublic(modifiers)) {
					write((Object) field.get(obj), buffer);
				}
			}
		}
	}

	public static void write(Iterable<?> objects, ByteBuffer buffer) {
		/*
		 * Loop through iterable and serialize recursively.
		 */

		objects.forEach(obj -> {
			try {
				write(obj, buffer);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

	public static void write(UUID uuid, ByteBuffer buffer) {
		/*
		 * https://stackoverflow.com/questions/17893609/convert-uuid-to-byte-that-works-
		 * when-using-uuid-nameuuidfrombytesb
		 */
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
	}

	public static void main(String[] args) {

		ByteBuffer buf = ByteBuffer.allocate(2048);

		System.out.println("Testing Integer serialization and deserialization");
		try {
			buf = Serializer.serialize(123, buf);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assert 123 == Deserializer.deserialize(buf, Integer.class);

		System.out.println("Testing String serialization and deserialization");
		try {
			buf = Serializer.serialize("ABCDEF", buf);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assert "ABCDEF".equals(Deserializer.deserialize(buf, String.class));

		System.out.println("Testing ENUM serialization and deserialization");
		try {
			buf = Serializer.serialize(Day.FRIDAY, buf);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assert Day.FRIDAY == Deserializer.deserialize(buf, Enum.class);

		System.out.println("Testing UUID serialization and deserialization");
		UUID randomUUID = UUID.randomUUID();
		System.out.println(randomUUID);
		try {
			buf = Serializer.serialize(randomUUID, buf);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assert randomUUID.equals(Deserializer.deserialize(buf, UUID.class));

//		System.out.println("Testing Class serialization and deserialization");
//		Employee emp = new Employee("TJL");
//		try {
//			buf = Serializer.serialize(emp, buf);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println(emp.numbers);
//		Employee output = Deserializer.deserialize(buf, Employee.class);
//		System.out.println("Complete deserializing");
//		System.out.println(output.name);
//		System.out.println(output.designation);
//		System.out.println(output.numbers);
//
//		System.out.println("Testing company");
//		Company company = new Company();
//		try {
//			Serializer.serialize(company, buf);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Company outCom = Deserializer.deserialize(buf, Company.class);
//		System.out.println(outCom.employees);
//
//		for (Employee employeez : outCom.employees) {
//			System.out.println(employeez.name);
//		}
//
//		ArrayList<Employee> numberList = new ArrayList<Employee>();
//
//		numberList.add(new Employee("TJL"));
//		numberList.add(new Employee("ABC"));
//		numberList.add(new Employee("DOGE"));
//		try {
//			buf = Serializer.serialize(numberList, buf);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(buf);
//		System.out.println(Deserializer.deserialize(buf, new ArrayList<Employee>() {
//		}));
//		for (Employee employeez : numberList) {
//			System.out.println(employeez.name);
//		}
//
//		ArrayList<Day> days = new ArrayList<Day>();
//		days.add(Day.FRIDAY);
//		days.add(Day.MONDAY);
//		try {
//			buf = Serializer.serialize(days, buf);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(buf);
//		System.out.println(Deserializer.deserialize(buf, new ArrayList<Day>() {
//		}));
//		for (Day day : days) {
//			System.out.println(day);
//		}
//
//		ArrayList<String> facilities = new ArrayList<String>();
//		facilities.add("LT");
//		facilities.add("TR");
//		try {
//			buf = Serializer.serialize(facilities, buf);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(buf);
//		System.out.println(Deserializer.deserialize(buf, new ArrayList<String>() {
//		}));
		
	}
}
