package visionCore.util;

import java.util.List;

public class Lists {

	public static <T> T getLast(List<T> list) {
		
		if (!list.isEmpty()) {
			
			return list.get(list.size()-1);
		}
		
		return null;
	}
	
	public static <T> T removeLast(List<T> list) {
		
		if (!list.isEmpty()) {
			
			return list.remove(list.size()-1);
		}
		
		return null;
	}
	
}
