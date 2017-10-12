package main;

import static main.Main.displayScale;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageStruct;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;

public class GUIRes {
	
	
	public static final int THREADS_NUM = Math.max(Runtime.getRuntime().availableProcessors(), 1);
	
	public static Image menubg, downloadbg, settingsbg;
	
	public static Image contentPanel, posterPanel, posterGlow, splitter, selected, glass, scrollbar, arrows,
						updatePanel, tick, contextPanel, loading, reading, posterSelect, posterFrame,
						menuSectionPanel, searchPanel, buttonSelected, radioButton, blankAvatar, loadingBanner;
	
	
	public static void loadEssentials() throws SlickException {
		
		menubg = new Image(Main.abspath+"/res/wallpaper/"+Settings.menu_background, Image.FILTER_LINEAR);
		loading = new Image(Main.abspath+"/res/textures/loading.png", Image.FILTER_LINEAR);
		loadingBanner = new Image(Main.abspath+"/res/textures/loadingbanner.png", Image.FILTER_LINEAR);
	}
	
	public static void load() throws SlickException {
		
		System.out.print("Loading GUIRes..");
		
		String path = Main.abspath+"/res/textures";
		
		int imgs = 21;
		
		String[] refs = new String[imgs];
		
		refs[0] = Main.abspath+"/res/wallpaper/"+Settings.menu_download_background;
		refs[1] = Main.abspath+"/res/wallpaper/"+Settings.menu_settings_background;
		refs[2] = path+"/contentPanel.png";
		refs[3] = path+"/updatePanel.png";
		refs[4] = path+"/posterPanel.png";
		refs[5] = path+"/posterGlow.png";
		refs[6] = path+"/splitter.png";
		refs[7] = path+"/selected.png";
		refs[8] = path+"/glass.png";
		refs[9] = path+"/scrollbar.png";
		refs[10] = path+"/arrows.png";
		refs[11] = path+"/tick.png";
		refs[12] = path+"/context.png";
		refs[13] = path+"/reading.png";
		refs[14] = path+"/posterSelect.png";
		refs[15] = path+"/menuSectionPanel.png";
		refs[16] = path+"/searchpanel.png";
		refs[17] = path+"/buttonSelected.png";
		refs[18] = path+"/posterFrame.png";
		refs[19] = path+"/radioButton.png";
		refs[20] = path+"/avatar-blank.png";
		
		ImageStruct[] structs = new ImageStruct[imgs];
		
		
		ExecutorService exec = Executors.newFixedThreadPool(THREADS_NUM);
		
		try {
		
			for (int i = 0; i < refs.length; i++) {
				final int index = i;
				
				exec.submit(new Runnable(){
					
					@Override
					public void run() {
						
						try {
							
							//structs[index] = new ImageStruct(refs[index], Image.FILTER_LINEAR);
							structs[index] = TJUtil.getImageStruct(new File(refs[index]));
							
						} catch (Exception e) { e.printStackTrace(); }
					}
					
				});
				
			}
			
		} finally {
			
			exec.shutdown();
		}
		
		
		try {
			
			exec.awaitTermination(2, TimeUnit.MINUTES);
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		
		downloadbg = new Image(structs[0]);
		settingsbg = new Image(structs[1]);
		
		contentPanel = new Image(structs[2]);
		updatePanel = new Image(structs[3]);
		posterPanel = new Image(structs[4]);
		posterGlow = new Image(structs[5]);
		splitter = new Image(structs[6]);
		selected = new Image(structs[7]);
		glass = new Image(structs[8]);
		scrollbar = new Image(structs[9]);
		arrows = new Image(structs[10]);
		tick = new Image(structs[11]);
		contextPanel = new Image(structs[12]);
		reading = new Image(structs[13]);
		posterSelect = new Image(structs[14]);
		menuSectionPanel = new Image(structs[15]);
		searchPanel = new Image(structs[16]);
		buttonSelected = new Image(structs[17]);
		posterFrame = new Image(structs[18]);
		radioButton = new Image(structs[19]);
		blankAvatar = new Image(structs[20]);
		
		System.out.println(" done.");
		
	}
	
	public static void drawContextPane(Graphics g, Rectangle pane) {
		
		drawContextPane(g, pane, 0f, 0f);
	}
	
	public static void drawContextPane(Graphics g, Rectangle pane, float offX, float offY) {
		if (contextPanel == null) { return; }
		
		contextPanel.startUse();
		
		contextPanel.getSubImage(0, 0, 32, 32)
		   .drawEmbedded(offX + pane.x - 16f * displayScale, offY + pane.y - 16f * displayScale, 32f * displayScale, 32f * displayScale);
		
		contextPanel.getSubImage(32, 0, 416-64, 32)
		   .drawEmbedded(offX + pane.x + (-16f + 32f) * displayScale, offY + pane.y - 16f * displayScale, pane.width - 32f * displayScale, 32f * displayScale);
		
		contextPanel.getSubImage(416-32, 0, 32, 32)
		   .drawEmbedded(offX + pane.x + pane.width - 16f * displayScale, offY + pane.y - 16f * displayScale, 32f * displayScale, 32f * displayScale);
		
		contextPanel.getSubImage(0, 32, 32, 160-64)
			.drawEmbedded(offX + pane.x - 16f * displayScale, offY + pane.y + 16f * displayScale, 32f * displayScale, pane.height - 32f * displayScale);
		
		contextPanel.getSubImage(32, 32, 416-64, 160-64)
			.drawEmbedded(offX + pane.x + 16f * displayScale, offY + pane.y + 16f * displayScale, pane.width - 32f * displayScale, pane.height - 32f * displayScale);
		
		contextPanel.getSubImage(416-32, 32, 32, 160-64)
			.drawEmbedded(offX + pane.x + pane.width - 16f * displayScale, offY + pane.y + 16f * displayScale, 32f * displayScale, pane.height - 32f * displayScale);
		
		contextPanel.getSubImage(0, 160-32, 32, 32)
		   .drawEmbedded(offX + pane.x - 16f * displayScale, offY + pane.y + pane.height - 16f * displayScale, 32f * displayScale, 32f * displayScale);
		
		contextPanel.getSubImage(32, 160-32, 416-64, 32)
		   .drawEmbedded(offX + pane.x + (-16f + 32f) * displayScale, offY + pane.y + pane.height - 16f * displayScale, pane.width - 32f * displayScale, 32f * displayScale);
		
		contextPanel.getSubImage(416-32, 160-32, 32, 32)
		   .drawEmbedded(offX + pane.x + pane.width - 16f * displayScale, offY + pane.y + pane.height - 16f * displayScale, 32f * displayScale, 32f * displayScale);
		
		contextPanel.endUse();
	}
	
	public static void drawRadioButton(float x, float y, boolean enabled) {
		
		radioButton.getSubImage(0, 20, 20, 20).draw(x, y, 20f * displayScale, 20f * displayScale, Color.white);
		
		if (enabled) {
			
			radioButton.getSubImage(0, 0, 20, 20).draw(x, y, 20f * displayScale, 20f * displayScale, Menu.flavors[Settings.menu_flavor]);
		}
	}
	
}
