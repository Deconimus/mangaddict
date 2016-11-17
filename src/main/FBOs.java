package main;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.EmptyImageData;
import org.newdawn.slick.opengl.pbuffer.FBOGraphics;

public class FBOs {

	public static FBOGraphics intermediate;
	
	public static Image intermediateImg;
	
	public static void load() {
		
		try {
		
			intermediateImg = new Image(new EmptyImageData(Display.getWidth(), Display.getHeight()));
			intermediate = new FBOGraphics(intermediateImg);
			
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
}
