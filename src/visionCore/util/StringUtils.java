package visionCore.util;

import java.util.ArrayList;
import java.util.List;

import visionCore.stack.LinkedStack;
import visionCore.stack.Stack;

public class StringUtils {

	public static Object parse(Object ref, String arg) {
		
		arg = arg.trim();
		
		if (arg != null && !arg.equals("") && !arg.equals(" ") && arg.length() > 0) {
			
			try {
				
				if (ref instanceof Boolean) {
					
					if (arg.length() == 1) {
						
						if (arg.toCharArray()[0] == '1') { return true; } 
						else if (arg.toCharArray()[0] == '0') { return false; }
						
					} else { return (boolean)Boolean.parseBoolean(arg); }
					
				} else if (ref instanceof Float) { return (float)Float.parseFloat(arg); } 
				else if (ref instanceof Integer) { return (int)Integer.parseInt(arg); } 
				else if (ref instanceof String) { return (String)arg; } 
				else if (ref instanceof Double) { return (double)Double.parseDouble(arg); } 
				else if (ref instanceof Long) { return (long)Long.parseLong(arg); } 
				else if (ref instanceof Character) { return (char)arg.toCharArray()[0]; }
				else if (ref instanceof Short) { return (short)Short.parseShort(arg); }
				
			} catch (Exception e) { e.printStackTrace(); }
			
		}
		
		return ref;
		
	}
	
	public static String ucWords(String string) {
		
		return capitolWords(string);
	}
	
	public static String capitolWords(String string) {
		
		String[] words = string.trim().split(" ");
		
		String result = "";
		
		for (String w : words) {
			
			w = w.replace(" ", "");
			w = ucFirst(w);
			
			result += w+" ";
			
		}
		
		result = result.trim();
		if (result.length() < 1) { return string; }
		
		return result;
	}
	
	/** Sets the first letter to upper case. */
	public static String ucFirst(String string) {
		
		char[] chars = string.toCharArray();
		if (chars.length > 0) {
			if (Character.isLowerCase(chars[0])) {
				chars[0] = Character.toUpperCase(chars[0]);
				string = String.copyValueOf(chars);
			}
		}
		
		return string;
	}
	
	/** Sets the first letter to lower case. */
	public static String lcFirst(String string) {
		
		char[] chars = string.toCharArray();
		if (chars.length > 0) {
			if (Character.isUpperCase(chars[0])) {
				chars[0] = Character.toLowerCase(chars[0]);
				string = String.copyValueOf(chars);
			}
		}
		
		return string;
	}
	
	public static String[] splitUnquoted(String source, String str) {
		
		if (!existsUnquoted(source, str)) { return null; }
		
		int[] indexes = unquotedIndexes(source, str);
		
		String[] split = new String[indexes.length+1];
		
		if (indexes[0] > 0) {
			split[0] = source.substring(0, indexes[0]);
		} else { split[0] = ""; }
		
		for (int i = 1; i < split.length-1; i++) {
			
			split[i] = source.substring(indexes[i-1]+str.length(), indexes[i]);
			
		}
		
		if (indexes[indexes.length-1] < source.length()) {
			
			split[split.length-1] = source.substring(indexes[indexes.length-1]+str.length());
			
		} else {
			
			split[split.length-1] = "";
			
		}
		
		return split;
		
	}
	
	public static boolean existsUnquoted(String source, String key) {
		
		int index = -1;
		while(index < source.length()-1) {
			
			index = source.indexOf(key, index+1);
			if (index >= 0) {
				
				if (!inQuotes(source, index)) {
					return true;
				}
				
			} else { break; }
			
		}
		
		return false;
		
	}
	
	public static int unquotedOccurences(String source, String key) {
		
		int i = 0;
		
		int index = -1;
		while(index < source.length()-1) {
			
			index = source.indexOf(key, index+1);
			if (index >= 0) {
				
				if (!inQuotes(source, index)) {
					i++;
				}
				
			} else { break; }
			
		}
		
		return i;
		
	}
	
	public static int firstUnquoted(String source, String key) {
		
		int index = -1;
		while(index < source.length()-1) {
			
			index = source.indexOf(key, index+1);
			if (index >= 0) {
				
				if (!inQuotes(source, index)) {
					return index;
				}
				
			} else { break; }
			
		}
		
		return -1;
		
	}
	
	public static int[] unquotedIndexes(String source, String key) {
		
		int[] indexes = new int[unquotedOccurences(source, key)];
		
		int i = 0;
		
		int index = -1;
		while(index < source.length()-1) {
			
			index = source.indexOf(key, index+1);
			if (index >= 0) {
				
				if (!inQuotes(source, index)) {
					indexes[i] = index;
					i++;
				}
				
			} else { break; }
			
		}
		
		if (indexes.length > 0) {
			return indexes;
		} else { return null; }
		
	}
	
	public static boolean inQuotes(String source, String key) {
		return inQuotes(source, source.indexOf(key));
	}
	
	public static boolean inQuotes(String source, int index) {
		
		if (index < source.length() && index >= 0) {
		
			boolean quoted = false;
			Stack<Character> quotations = new LinkedStack<Character>();
			
			char[] chars = source.toCharArray();
			
			for (int i = 0; i < chars.length; i++) {
				
				if (i == index) {
					
					return quoted;
					
				}
				
				if (chars[i] == '"' || chars[i] == '\'') {
					
					if (!quoted) {
						
						quoted = true;
						quotations.push(chars[i]);
						
					} else {
						
						if (chars[i] == (char)quotations.peek()) {
							
							quotations.pop();
							
						}
						
						if (quotations.peek() == null) {
							
							quoted = false;
							
						}
						
					}
					
				}
				
			}
			
		}
		
		return false;
		
	}
	
	public static boolean containsNum(String string, int num, String... args) {
		
		int i = 0;
		
		for (String arg : args) {
			
			if (string.contains(arg)) { i++; }
			
		}
		
		return i >= num;
	}
	
	public static boolean containsAll(String string, String... args) {
		
		for (String arg : args) {
			
			if (!string.contains(arg)) { return false; }
			
		}
		
		return true;
	}
	
	public static boolean containsAllIgnoreCase(String string, String... args) {
		
		for (int i = 0; i < args.length; i++) { args[i] = args[i].toLowerCase(); }
		
		return containsAll(string.toLowerCase(), args);
	}
	
	public static boolean containsAllInOrder(String string, String... args) {
		
		int index = 0;
		
		for (String arg : args) {
			
			int i = string.indexOf(arg);
			
			if (i == -1 || i < index) {
				
				return false;
			}
			
			index = i;
		}
		
		return true;
	}
	
	public static boolean containsAllInOrderIgnoreCase(String string, String... args) {
		
		for (int i = 0; i < args.length; i++) { args[i] = args[i].toLowerCase(); }
		
		return containsAllInOrder(string.toLowerCase(), args);
	}
	
	
	public static int[] indexesOf(String str, String match) {
		
		List<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; str.indexOf(match, i) > -1;) {
			
			list.add(str.indexOf(match, i));
			
			i = Lists.getLast(list)+1;
		}
		
		int[] arr = new int[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			
			arr[i] = list.get(i);
		}
		
		return arr;
	}
	
	public static int[] indexesOf(String str, char ch) {
		
		return indexesOf(str, ch+"");
	}
	
	public static String floatToString(float value, int decimals) {
		
		String s = value+"";
		
		return s.substring(0, Math.min(s.lastIndexOf('.')+1+decimals, s.length()));
	}
	
	public static String doubleToString(double value, int decimals) {
		
		String s = value+"";
		
		return s.substring(0, Math.min(s.lastIndexOf('.')+1+decimals, s.length()));
	}
	
	
	public static String dropVowels(String str) {
		
		return str.replaceAll("[AaEeIiOoUu]", "");
	}
	
	public static int countOccurences(String str, String pattern) {
		
		int occ = 0;
		
		for (int i = 0; i < 1000000 && str.contains(pattern); i++, occ++) {
			
			str.substring(str.indexOf(pattern)+pattern.length());
		}
		
		return occ;
	}
	
	
	/**
	 * Splits a camel-case formatted string into distinct words or digit-sequences. <br>
	 * "dankVapes420" would return { "dank", "Vapes", "420" }
	 * @param str The camel-case formatted string.
	 * @return A String-array holding the distinct words.
	 */
	public static String[] splitCamelCase(String str) {
		
		return str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
	}
	
	/**
	 * Reformats a camel-case string using a given delimiter. <br>
	 * "barrelRoll" could be split into "barrel_roll" using "_" as delimiter.
	 * @param str The camel-case formatted string.
	 * @param delimiter The delimiter that will connect the split-up string.
	 * @param ucWords Indicates wether the words should be capitalized.
	 * @return The newly formatted string.
	 */
	public static String formatCamelCase(String str, String delimiter, boolean ucWords) {
		
		String split[] = splitCamelCase(str);
		str = "";
		
		for (String s : split) {
			
			if (ucWords) { s = ucFirst(s); }
			else { s = lcFirst(s); }
			
			str += s+delimiter;
		}
		
		return str.trim();
	}
	
}
