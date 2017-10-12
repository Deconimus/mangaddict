package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.newdawn.slick.Image;
import org.newdawn.slick.ImageStruct;

import visionCore.reflection.Classes;

//#ifdef _WIN64
/*
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJDecompressor;
*/
//#endif

public class TJUtil {
	
	
	public static ImageStruct getImageStruct(File file) {
		
		//#ifndef _WIN64
		ImageStruct struct = null;
		try { struct = new ImageStruct(file, Image.FILTER_LINEAR); }
		catch (Exception e) { e.printStackTrace(); }
		return struct;
		//#endif
		//#ifdef _WIN64
		/*
		return getImageStruct(file, null);
		*/
		//#endif
	}
	
	//#ifdef _WIN64
	/*
	public static ImageStruct getImageStruct(File file, TJDecompressor decoder) {
		
		ImageStruct struct = null;
		
		String pathLC = file.getAbsolutePath().toLowerCase();
		
		if (pathLC.endsWith(".jpg") || pathLC.endsWith(".jpeg")) {
			
			try {
				
				if (decoder == null) { decoder = new TJDecompressor(); }
				
				byte[] compressedData = Files.readAllBytes(file.toPath());
				decoder.setSourceImage(compressedData, compressedData.length);
				
				int width = decoder.getWidth();
				int height = decoder.getHeight();
				
				int format = TJ.PF_RGBA;
				int bpp = TJ.getPixelSize(format);
				int pitch = bpp * width;
				int size = pitch * height;
				
				byte[] data = new byte[size];
				decoder.decompress(data, 0, 0, width, pitch, height, format, 0);
				
				struct = new ImageStruct(data, width, height, bpp == 4 ? true : false, Image.FILTER_LINEAR, file.getAbsolutePath());
				
			} catch (Exception e) { e.printStackTrace(); }
			
		} else {
			
			try { struct = new ImageStruct(file, Image.FILTER_LINEAR); }
			catch (Exception e) { e.printStackTrace(); }
		}
		
		return struct;
	}
	*/
	//#endif

}
