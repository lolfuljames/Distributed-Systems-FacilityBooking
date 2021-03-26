package utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.io.*;
import server.*;

public class Deserializer {

	public Deserializer() {
		// TODO Auto-generated constructor stub
	}

	public static <T> T deserialize(ByteBuffer buffer, T t) {

		/*
		 * https://stackoverflow.com/questions/14034421/how-to-return-the-proper-object-
		 * type-after-deserialization
		 */

		System.out.println("Deserializing using anonymous class...");
		buffer.clear(); // Set pointer to position 0
		buffer.order(ByteOrder.LITTLE_ENDIAN); // Set Little Indian Byte order

		Object obj = null;
		try {
			Class<?> clazz = t.getClass();
			obj = read(clazz.getGenericSuperclass(), buffer);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (T) obj;
	}

	/*
	 * 
	 * https://stackoverflow.com/questions/14034421/how-to-return-the-proper-object-
	 * type-after-deserialization
	 */
	public static <T> T deserialize(ByteBuffer buffer, Class<T> clazz) {
		
		
		System.out.println("Deserializing using clazz: " + clazz);
		buffer.clear(); // Set pointer to position 0
		buffer.order(ByteOrder.LITTLE_ENDIAN); // Set Little Indian Byte order
		
		Object obj = null;
		try {
			obj = read(clazz, buffer);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (T) obj;
	}

	/*
	 * Called for the deserialization of a generic class, only works for ArrayList<Integer or etc>
	 *  
	 */
	private static <T> Object read(Type genericSuperclass, ByteBuffer buffer)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		

		ParameterizedType pType = (ParameterizedType) genericSuperclass;

//		System.out.println("Generic: " + genericSuperclass);
//		System.out.print("Raw type: " + pType.getRawType() + " - ");
//		System.out.println("Type args: " + pType.getActualTypeArguments()[0]); // return the Parameter
																				// <Integer><String><Object> etc..

		Object returnObj = read((Class) pType.getRawType(), buffer);

		int length = readInt(buffer);

		for (int i = 0; i < length; i++) {
			Object newObj = read((Class<?>) pType.getActualTypeArguments()[0], buffer);
			((List) returnObj).add(newObj);
		}

		return (T) returnObj;
	}

	/*
	 * Called for the deserialization of a non-generic class
	 */
	public static <T> Object read(Class<T> clazz, ByteBuffer buffer)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
//		System.out.println("Reading class...");

		if (clazz == Integer.TYPE || clazz == Integer.class) {
			return (Object) readInt(buffer);
		} else if (clazz == UUID.class) {
			return (Object) readUUID(buffer);
		} else if (clazz == String.class) {
			return (Object) readString(buffer);
		} else if (clazz.isEnum() || clazz == Enum.class) {
			return (Object) readEnum(buffer);
		} else {
			Object obj = clazz.newInstance();
			for (Field field : clazz.getFields()) {
				System.out.println("Setting field: " + field.getName());
				int modifiers = field.getModifiers();
				if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && Modifier.isPublic(modifiers)) {
					Type type = field.getGenericType();
					System.out.print("Field: " + field.getName() + "\t");
					System.out.println("Generic Type: " + type);
					if(type == Body.class) {
						field.set(obj, readBody(obj, buffer));
					} else {
						read(type, field, obj, buffer);						
					}
				}
			}
			return (Object) obj;
		}
	}

	
	/*
	 * Called for each field of the class, deserialize data 
	 * based on the class field to set the value of field.
	 * 
	 * Works when attributes of the class is ArrayList<Integer>
	 * Also works for Integer, UUID, String, Enum
	 */
	public static void read(Type type, Field field, Object obj, ByteBuffer buffer)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		
//		System.out.println("Reading type...");
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
//			System.out.print("Raw type: " + pType.getRawType() + " - ");
//			System.out.println("Type args: " + pType.getActualTypeArguments()[0]); 
			Object returnObj = read((Class) pType.getRawType(), buffer);
			int length = readInt(buffer);
			for (int i = 0; i < length; i++) {
				Object newObj = read((Class<?>) pType.getActualTypeArguments()[0], buffer);
//				System.out.println("Adding to list: " + newObj);
				((List) returnObj).add(newObj);
			}

			field.set(obj, returnObj);
		} else if (type == Integer.TYPE || type == Integer.class) {
			int temp = readInt(buffer);
			System.out.println("Read Integer: " + temp);
			field.set(obj, temp);
//			field.set(obj, readInt(buffer));
		} else if (type == UUID.class) {
			UUID temp = readUUID(buffer);
			System.out.println("Read UUID: " + temp);
			field.set(obj, temp);
//			field.set(obj, readUUID(buffer));
		} else if (type == String.class) {
			String temp = readString(buffer);
			System.out.println("Read String: " + temp);
			field.set(obj, temp);
//			field.set(obj, readString(buffer));
		} else if (type == Enum.class) {
			field.set(obj, readEnum(buffer));
		} else {
			field.set(obj, read(field.getType(), buffer));
		}
	}
	
	public static Body readBody(Object obj, ByteBuffer buffer) {

		int opCode = ((Message) obj).getHeader().getOpCode();
		int messageType = ((Message) obj).getHeader().messageType;
		
		if(messageType == 0) {
			switch(opCode) {
			case 0:
				
			}
		} else {
			
		}
		return null;
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

	public static Enum<?> readEnum(ByteBuffer buffer) {
		int idx = buffer.get();
		if (idx >= Day.values().length) {
			throw new DeserializationException("Having issue deserializing enum: Index out of Enum range");
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
