package visionCore.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ArrayUtils {

	public static List asList(Object... args) {
		return asArrayList(args);
	}
	
	public static <T> ArrayList<T> asCastedArrayList(T[] args) {
		
		ArrayList<T> list = new ArrayList<T>();
		
		for (T o : args) {
			
			list.add(o);
			
		}
		
		return list;
	}
	
	public static ArrayList asArrayList(Object... args) {
		
		args = checkForMiscast(args);
		
		ArrayList<Object> list = new ArrayList<Object>();
		
		for (Object o : args) {
			
			list.add(o);
			
		}
		
		return list;
	}
	
	public static LinkedList asLinkedList(Object... args) {
		
		args = checkForMiscast(args);
		
		LinkedList<Object> list = new LinkedList<Object>();
		
		for (Object o : args) {
			
			list.add(o);
			
		}
		
		return list;
	}
	
	public static <T> T[] fromList(List<T> list) {
		return fromList(list, false);
	}
	
	public static <T> T[] fromList(List<T> list, final boolean removeNulls) {
		
		if (list == null) { System.out.println("Error: visionCore.util.ArrayUtils fromList(List<T> list) list is null."); return null; }
		
		int size = list.size();
		
		//if (size == 0) { System.out.println("Warning: visionCore.util.ArrayUtils fromList(List<T> list) list.size() == 0."); return null; }
		
		int nulls = 0;
		if (removeNulls) {
			
			for (T t : list) {
				
				if (t == null) { nulls++; }
				
			}
			
		}
		
		T[] array = (T[])(new Object[list.size() - nulls]);
		
		int i = 0;
		for (T t : list) { // faster for LinkedList and such.
			
			if (removeNulls && t == null) { continue; }
			
			array[i] = t;
			
			i++;
		}
		
		return array;
	}
	
	private static Object[] checkForMiscast(Object[] args) {
		
		if (args.length == 1 && args[0] instanceof Object[]) {
			args = (Object[])(args[0]);
		}
		
		return args;
	}
	
	public static Object getCasted(Object[] arr, Class type) {
		
		if (arr == null || type == null) { return null; }
		
		return Arrays.copyOf(arr, arr.length, type);
	}
	
	
	public static int indexOf(long[] array, long val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(short[] array, short val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(char[] array, char val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(boolean[] array, boolean val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(byte[] array, byte val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(double[] array, double val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(float[] array, float val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(int[] array, int val) {
		
		if (array.length < 0) { return -1; }
		for (int i = 0; i < array.length; i++) { if (array[i] == val) { return i; } }
		return -1;
	}
	
	public static int indexOf(Object[] array, Object obj) {
		
		if (array.length > 0) {
			
			for (int i = 0; i < array.length; i++) {
				
				if (array[i].equals(obj)) { return i; }
			}
			
		}
		
		return -1;
	}
	
	public static int indexOf(String[] array, String str) {
		
		if (array.length > 0) {
			
			for (int i = 0; i < array.length; i++) {
				
				if (array[i].equalsIgnoreCase(str)) { return i; }
			}
			
		}
		
		return -1;
	}
	
	
	public static boolean contains(long[] array, long val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(short[] array, short val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(char[] array, char val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(boolean[] array, boolean val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(byte[] array, byte val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(double[] array, double val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(float[] array, float val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(int[] array, int val) {
		
		return indexOf(array, val) > -1;
	}
	
	public static boolean contains(Object[] array, Object obj) {
		
		return indexOf(array, obj) > -1;
	}
	
	public static boolean contains(String[] array, String str) {
		
		return indexOf(array, str) > -1;
	}
	
	
	public static void sort(int[] array, Comparator<Integer> comp) {
		
		
	}
	
}
