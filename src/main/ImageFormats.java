package main;

import java.io.File;
import java.util.HashSet;

public class ImageFormats {

	public static HashSet<String> extensions;
	
	static {
		
		extensions = new HashSet<String>();
		
		extensions.add(".png");
		extensions.add(".jpg");
		
	}
	
	public static boolean isSupported(File file) {
		if (file.isDirectory()) { return false; }
		
		return isSupported(file.getName());
	}
	
	public static boolean isSupported(String fileName) {
		
		String ext = fileName.trim().toLowerCase();
		
		if (!ext.contains(".")) { return false; }
		
		ext = ext.substring(ext.lastIndexOf("."));
		
		return extensions.contains(ext);
	}
	
}
