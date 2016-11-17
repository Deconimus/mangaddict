package main;

import static main.Main.displayScale;

import java.awt.Font;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.newdawn.slick.TrueTypeFont;

import visionCore.math.FastMath;

public class Fonts {

	public static FontStruct roboto, robotoBold;
	public static TrueTypeFont robotoMono24;
	
	public static void load() {
		
		System.out.print("Loading Fonts..");
		
		//animeAce = new FontStruct(new File(Main.abspath+"/res/fonts/animeace2_reg.ttf"));
		//animeAceBold = new FontStruct(new File(Main.abspath+"/res/fonts/animeace2_bld.ttf"));
		roboto = new FontStruct(new File(Main.abspath+"/res/fonts/roboto.ttf"));
		robotoBold = new FontStruct(new File(Main.abspath+"/res/fonts/roboto-bold.ttf"));
		
		try {
			
			java.awt.Font fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(Main.abspath+"/res/fonts/roboto-mono.ttf"));
			fnt = fnt.deriveFont(FastMath.floor(24f * displayScale + 0.5f));
			robotoMono24 = new TrueTypeFont(fnt, true);
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		System.out.println(" done.");
		
	}
	
	public static class FontStruct {
		
		private static final int[] sizes = new int[]{ 18, 21, 24, 30, 36, 48, 60 };
		
		private TrueTypeFont[] sizedFonts;
		
		public TrueTypeFont s18, s21, s24, s30, s36, s48, s60;
		
		public FontStruct(File file) {
			
			if (!file.exists()) { System.out.println("Fontfile not found: \""+file.getAbsolutePath()+"\""); }
			
			try {
				
				java.awt.Font fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, file);
				fnt = fnt.deriveFont(FastMath.floor(60f * displayScale + 0.5f));
				s60 = new TrueTypeFont(fnt, true);
				fnt = fnt.deriveFont(FastMath.floor(48f * displayScale + 0.5f));
				s48 = new TrueTypeFont(fnt, true);
				fnt = fnt.deriveFont(FastMath.floor(36f * displayScale + 0.5f));
				s36 = new TrueTypeFont(fnt, true);
				
			} catch (Exception | Error e) { e.printStackTrace(); }
			
			try {
				
				java.awt.Font fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, file);
				fnt = fnt.deriveFont(FastMath.floor(30f * displayScale + 0.5f));
				s30 = new TrueTypeFont(fnt, true);
				fnt = fnt.deriveFont(FastMath.floor(24f * displayScale + 0.5f));
				s24 = new TrueTypeFont(fnt, true);
				fnt = fnt.deriveFont(FastMath.floor(21f * displayScale + 0.5f));
				s21 = new TrueTypeFont(fnt, true);
				fnt = fnt.deriveFont(FastMath.floor(18f * displayScale + 0.5f));
				s18 = new TrueTypeFont(fnt, true);
				
			} catch (Exception | Error e) { e.printStackTrace(); }
			
			sizedFonts = new TrueTypeFont[]{ s18, s24, s30, s36, s48, s60 };
			
		}
		
		public TrueTypeFont getSize(float size) {
			
			return getSize((int)(size + 0.5f));
		}
		
		public TrueTypeFont getSize(int size) {
			
			int leastDiff = -1;
			for (int i = 0, diff = Integer.MAX_VALUE; i < sizes.length; i++) {
				
				int d = Math.abs(size - sizes[i]);
				if (d < diff) { diff = d; leastDiff = i; }
			}
			
			return sizedFonts[leastDiff];
		}
		
	}
	
}
