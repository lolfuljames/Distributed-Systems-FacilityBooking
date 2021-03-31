package utils.serialize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.io.*;
import server.*;
import utils.Constants;
import utils.message.Body;
import utils.message.Message;
import utils.message.request.*;
import utils.message.response.*;

public class Deserializer {

	public Deserializer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Deserialize a generic class object. e.g. ArrayList<Integer>
	 * 
	 * @param <T>
	 * @param buffer
	 * @param t
	 * @return
	 */
	public static <T> T deserialize(ByteBuffer buffer, T t) {

//		System.out.println("Deserializing using anonymous class...");
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
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (T) obj;
	}


	/**
	 * Deserialize a non-generic class object.
	 * https://stackoverflow.com/questions/14034421/how-to-return-the-proper-object-
	 * type-after-deserialization
	 * 
	 * @param <T>
	 * @param buffer
	 * @param clazz
	 * @return
	 */
	public static <T> T deserialize(ByteBuffer buffer, Class<T> clazz) {
		
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
		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (T) obj;
	}


	/**
	 * Called for the deserialization of a generic class, works for ArrayList<Integer or etc>
	 * @param <T>
	 * @param genericSuperclass
	 * @param buffer
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws UnknownHostException
	 */
	private static <T> Object read(Type genericSuperclass, ByteBuffer buffer)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, UnknownHostException {
		

		ParameterizedType pType = (ParameterizedType) genericSuperclass;

		Object returnObj = read((Class) pType.getRawType(), buffer);
		int length = readInt(buffer);

		for (int i = 0; i < length; i++) {
			Object newObj = read((Class<?>) pType.getActualTypeArguments()[0], buffer);
			((List) returnObj).add(newObj);
		}

		return (T) returnObj;
	}

	/**
	 * A multi-purpose read function to return the deserialized primitive value.
	 * 
	 * @param <T>
	 * @param clazz
	 * @param buffer
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws UnknownHostException
	 */
	public static <T> Object read(Class<T> clazz, ByteBuffer buffer)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, UnknownHostException {
//		System.out.println("Reading class...");

		if (clazz == Integer.TYPE || clazz == Integer.class) {
			return (Object) readInt(buffer);
		} else if (clazz == UUID.class) {
			return (Object) readUUID(buffer);
		} else if (clazz == String.class) {
			return (Object) readString(buffer);
		} else if (clazz.isEnum() || clazz == Enum.class) {
			return (Object) readEnum(clazz, buffer);
		} else if (clazz == InetAddress.class) {
			return (Object) readInetAddress(clazz, buffer);
		} else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
			return (Object) readBool(buffer);
		}
		else {
			Object obj = clazz.newInstance();

			for (Field field : clazz.getDeclaredFields()) {
				field.setAccessible(true);
				int modifiers = field.getModifiers();
				// dirty fix for ArrayList as size is public int which is not a field we want to set.
				if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && !field.getName().equals("size")) {

					Type type = field.getGenericType();
					// if type is of Body, call readBody
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

	/**
	 * Called for each field of the class, deserialize data 
	 * based on the class field to set the value of field.
	 * 
	 * Works when attributes of the class is ArrayList<Integer>
	 * Also works for Integer, UUID, String, Enum
	 * @param type
	 * @param field
	 * @param obj
	 * @param buffer
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws UnknownHostException
	 */
	public static void read(Type type, Field field, Object obj, ByteBuffer buffer)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException, UnknownHostException {
		
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			Object returnObj = read((Class) pType.getRawType(), buffer);
			int length = readInt(buffer);
			for (int i = 0; i < length; i++) {
				Object newObj = read((Class<?>) pType.getActualTypeArguments()[0], buffer);
				((List) returnObj).add(newObj);
			}
			field.set(obj, returnObj);
		} else if (type == Integer.TYPE || type == Integer.class) {
			field.set(obj, readInt(buffer));
		} else if (type == UUID.class) {
			field.set(obj, readUUID(buffer));
		} else if (type == String.class) {
			field.set(obj, readString(buffer));
		} else if (type == Enum.class) {
			field.set(obj, readEnum(((Class<?>) type), buffer));
		} else if (type == Boolean.class){
			field.set(obj,  readBool(buffer));
		} else {
			field.set(obj, read(field.getType(), buffer));
		}
	}
	
	/**
	 * Deserialize the boolean value from the buffer.
	 * 
	 * @param buffer
	 * @return
	 */
	public static Boolean readBool(ByteBuffer buffer) {
		return buffer.get() == 1 ? true: false;
	}
	
	/*
	 * https://github.com/fasterxml/jackson-databind/issues/1605
	 * We refer to fastxml to see how they serialize/deserialize InetAddress
	 */
	public static Object readInetAddress(Object obj, ByteBuffer buffer) throws UnknownHostException {
		String hostname = readString(buffer); 
		int length = buffer.getInt();
		byte[] address = new byte[length]; 
		buffer.get(address, 0, length);
		return InetAddress.getByAddress(hostname, address);
	}
	
	/**
	 * Deserialize the body class within Message class.
	 * Return the correct body type based on the opCode within the header.
	 * 
	 * @param obj
	 * @param buffer
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws UnknownHostException
	 */
	public static Object readBody(Object obj, ByteBuffer buffer) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, UnknownHostException {

		int opCode = ((Message) obj).getHeader().getOpCode();
		int messageType = ((Message) obj).getHeader().messageType;
		
		Object reqBody = null;
		Object respBody = null;
		if(messageType == 0) {
			switch(opCode) {
			case Constants.QUERY_AVAILABILITY:
				reqBody = read(QueryAvailabilityReqBody.class, buffer);
				break;
			case Constants.MAKE_BOOKING:
				reqBody = read(MakeBookingReqBody.class, buffer);
				break;
			case Constants.AMEND_BOOKING:
				reqBody = read(AmendBookingReqBody.class, buffer);
				break;
			case Constants.MONITOR_AVAILABILITY:
				reqBody = read(MonitorAvailabilityReqBody.class, buffer);
				break;
			case Constants.EXTEND_BOOKING:
				reqBody = read(ExtendBookingReqBody.class, buffer);
				break;
			case Constants.QUERY_FACILITY_TYPES:
				reqBody = read(QueryFacilityTypesReqBody.class, buffer);
				break;
			case Constants.QUERY_FACILITY_IDS:
				reqBody = read(QueryFacilityIDsReqBody.class, buffer);
				break;
			case Constants.CANCEL_BOOKING:
				reqBody = read(CancelBookingReqBody.class, buffer);
				break;
			}
			return reqBody;
		} else {
			switch(opCode) {
			case Constants.QUERY_AVAILABILITY:
				respBody = read(QueryAvailabilityRespBody.class, buffer);
				break;
			case Constants.MAKE_BOOKING:
				respBody = read(MakeBookingRespBody.class, buffer);
				break;
			case Constants.AMEND_BOOKING:
				respBody = read(AmendBookingRespBody.class, buffer);
				break;
			case Constants.MONITOR_AVAILABILITY:
				respBody = read(MonitorAvailabilityRespBody.class, buffer);
				break;
			case Constants.EXTEND_BOOKING:
				respBody = read(ExtendBookingRespBody.class, buffer);
				break;
			case Constants.QUERY_FACILITY_TYPES:
				respBody = read(QueryFacilityTypesRespBody.class, buffer);
				break;
			case Constants.QUERY_FACILITY_IDS:
				respBody = read(QueryFacilityIDsRespBody.class, buffer);
				break;
			case Constants.CANCEL_BOOKING:
				respBody = read(CancelBookingRespBody.class, buffer);
				break;
			}
			return respBody;
		}
	}

	/**
	 * Deserialize the int from buffer.
	 * 
	 * @param buffer
	 * @return
	 */
	public static int readInt(ByteBuffer buffer) {
		return buffer.getInt();
	}

	/**
	 * Deserialize the string from buffer, retrives the length of the string
	 * and reconstruct using the String constructor.
	 * 
	 * @param buffer
	 * @return
	 */
	public static String readString(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte[] byteString = new byte[length];

		buffer.get(byteString, 0, length);
		String outputString = new String(byteString, StandardCharsets.UTF_8);
		return outputString;
	}

	/**
	 * Deserialize the Enum constants.
	 * 
	 * @param clazz
	 * @param buffer
	 * @return
	 */
	public static Enum<?> readEnum(Class<?> clazz, ByteBuffer buffer) {
		int idx = buffer.get();
		Object[] values = clazz.getEnumConstants();
		if (idx >= values.length) {
			throw new DeserializationException("Having issue deserializing enum: Index out of Enum range");
		}
		return (Enum<?>) values[idx];
	}

	/**
	 * Deserialize and reconstruct the UUID from buffer
	 * by getting the MSB and LSB.
	 * @param buffer
	 * @return
	 */
	public static UUID readUUID(ByteBuffer buffer) {
		long high = buffer.getLong();
		long low = buffer.getLong();

		return new UUID(high, low);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
