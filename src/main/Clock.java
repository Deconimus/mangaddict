package main;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;

import visionCore.geom.Color;

public class Clock {

	public static void renderClock(Graphics g, float x, float y, Color col, Font font) {
		
		renderClock(g, x, y, col, font, false);
	}
	
	public static void renderClock(Graphics g, float x, float y, Color col, Font font, boolean textShadow) {
		
		Font fontOld = g.getFont();
		Color colOld = g.getColor();
		
		g.setFont(font);
		g.setColor(col, 1f);
		
		
		Date d = new Date();
		String s = new SimpleDateFormat("HH:mm").format(d);
		
		
		if (x < 0f) { x = Display.getWidth() - font.getWidth(s) + x; }
		
		if (textShadow) {
			
			float gray = ((1f - col.r) + (1f - col.g) + (1f - col.b)) / 3f;
			
			Color shadowCol = new Color(gray, gray, gray, 1f);
			
			g.drawStringShadow(s, (int)x, (int)y, Math.max((int)(2f * Main.displayScale + 0.5f), 1), 0.1f, shadowCol);
			
		} else {
			
			g.drawString(s, (int)x, (int)y);
		}
		
		
		g.setFont(fontOld);
		g.setColor(colOld);
		
	}
	
}
