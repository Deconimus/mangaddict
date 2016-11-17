package main;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.shader.ShaderProgram;

public class Shaders {

	public static boolean working;
	public static ShaderProgram blur, bicubic;
	
	public static void load() {
		
		working = false;
		
		try {
			
			blur = ShaderProgram.loadProgram(Main.abspath+"/res/shaders/blur_shader.vert", Main.abspath+"/res/shaders/blur_shader.frag");
			
			bicubic = ShaderProgram.loadProgram(Main.abspath+"/res/shaders/bicubic.vert", Main.abspath+"/res/shaders/bicubic.frag");
			
			working = true;
			
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
}
