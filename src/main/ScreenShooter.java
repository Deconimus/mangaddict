package main;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class ScreenShooter {

	public static void makeScreenShot(Graphics g) throws SlickException {
		
		final String filename = "Screenshot - ["+System.currentTimeMillis()+"].png";
		
		final int width = Display.getWidth();
		final int height = Display.getHeight();
		
		// allocate space for RBG pixels
        ByteBuffer fb = ByteBuffer.allocateDirect(width * height * 3);
        
        // grab a copy of the current frame contents as RGB
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, fb);
		
		Thread t = new Thread(){
			
			@Override
			public void run() {
				
				//Creating an rbg array of total pixels
				int[] pixels = new int[width * height];
				
				BufferedImage imageIn = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		        // convert RGB data in ByteBuffer to integer array
		        for (int i=0; i < pixels.length; i++) {
		            int bindex = i * 3;
		            pixels[i] =
		                ((fb.get(bindex) << 16))  +
		                ((fb.get(bindex+1) << 8))  +
		                ((fb.get(bindex+2) << 0));
		        }
		        //Allocate colored pixel to buffered Image
		        imageIn.setRGB(0, 0, width, height, pixels, 0 , width);

		        //Creating the transformation direction (horizontal)
		        AffineTransform at =  AffineTransform.getScaleInstance(1, -1);
		        at.translate(0, -imageIn.getHeight(null));

		        //Applying transformation
		        AffineTransformOp opRotated = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		        BufferedImage imageOut = opRotated.filter(imageIn, null);
		        
		        File out = new File(Main.abspath+"/screenshots/"+filename);
		        if (!out.getParentFile().exists()) { out.getParentFile().mkdirs(); }
		        
		        try { ImageIO.write(imageOut, "png", out); }
		        catch (Exception e) { e.printStackTrace(); }
				
			}
			
		};
		
		t.start();
		
		System.out.println(filename+" saved!");
		
	}
	
}
