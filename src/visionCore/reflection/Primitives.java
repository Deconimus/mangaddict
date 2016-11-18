package visionCore.reflection;

public class Primitives {

	public static Class getBoxedClass(Class c) {
		
		if (c.isPrimitive()) {
			
			if (c.getName().equalsIgnoreCase("float")) {
				
				return java.lang.Float.class;
				
			} else if (c.getName().equalsIgnoreCase("int")) {
				
				return java.lang.Integer.class;
				
			} else if (c.getName().equalsIgnoreCase("boolean")) {
				
				return java.lang.Boolean.class;
				
			} else if (c.getName().equalsIgnoreCase("long")) {
				
				return java.lang.Long.class;
				
			} else if (c.getName().equalsIgnoreCase("double")) {
				
				return java.lang.Double.class;
				
			} else {
				
				return java.lang.Short.class;
				
			}
			
		} else { return c; }
		
	}
	
	public static Class getUnboxedClass(Class c) {
		
		if (c.equals(Float.class)) {
			
			return float.class;
			
		} else if (c.equals(Integer.class)) {
			
			return int.class;
			
		} else if (c.equals(Boolean.class)) {
			
			return boolean.class;
			
		} else if (c.equals(Long.class)) {
			
			return long.class;
			
		} else if (c.equals(Double.class)) {
			
			return double.class;
			
		} else if (c.equals(Short.class)) {
			
			return short.class;
			
		}
		
		return c;
		
	}
	
	public static Object getUnboxed(Object obj) {
		
		if (obj instanceof Float) {
			
			return (float)(obj);
			
		} else if (obj instanceof Integer) {
			
			return (int)(obj);
			
		} else if (obj instanceof Boolean) {
			
			return (boolean)(obj);
			
		} else if (obj instanceof Long) {
			
			return (long)(obj);
			
		} else if (obj instanceof Double) {
			
			return (double)(obj);
			
		} else if (obj instanceof Short) {
			
			return (short)(obj);
			
		}
		
		return obj;
		
	}
	
	public static Object getBoxed(Object obj) {
		
		Class c = obj.getClass();
		
		if (c.equals(float.class)) {
			
			return (Float)(obj);
			
		} else if (c.equals(int.class)) {
			
			return (Integer)(obj);
			
		} else if (c.equals(boolean.class)) {
			
			return (Boolean)(obj);
			
		} else if (c.equals(long.class)) {
			
			return (Long)(obj);
			
		} else if (c.equals(double.class)) {
			
			return (Double)(obj);
			
		} else if (c.equals(short.class)) {
			
			return (Short)(obj);
			
		}
		
		return obj;
		
	}
	
	public static boolean isBoxed(Object obj) {
		
		return obj != null && (obj instanceof Float || obj instanceof Integer || obj instanceof Boolean || obj instanceof Long
				|| obj instanceof Double || obj instanceof Short);
		
	}
	
}
