package main;

import static main.Main.displayScale;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageStruct;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;
import visionCore.math.FastMath;
import visionCore.util.Files;

// copy-paste from poster select...

public class WallpaperSelect extends Component {

public static final int CACHED_BACK = 1, CACHED_FRONT = 1;
	
	
	public Rectangle pane;
	
	public List<File> imageFiles;
	public Image cache[];
	public ImageStruct structs[];
	
	public AtomicInteger cacheLoadInd, selected;
	
	public MenuList<File> list;
	
	public Field field;
	public String wallpName, fieldName;
	
	public Thread loadThread;
	
	public float loadingRot;
	
	
	public WallpaperSelect(Field field, String fieldName, String wallpName) {
		
		this.field = field;
		this.fieldName = fieldName;
		this.wallpName = wallpName;
		
		this.loadingRot = 0f;
		
		WallpaperSelect tis = this;
		
		float w = 1280f * displayScale, h = 830f * displayScale;
		
		this.pane = new Rectangle((Display.getWidth() - w) / 2f, 150f * displayScale + (Display.getHeight() - 250f * displayScale - h) / 2f, w, h);
		
		File dir = new File(Main.abspath+"/res/wallpaper");
		if (!dir.exists()) { exit(); }
		
		this.imageFiles = Files.getFiles(dir, f -> !f.isDirectory() && ImageFormats.isSupported(f));
		Collections.sort(this.imageFiles);
		
		this.selected = new AtomicInteger(0);
		for (int i = 0; i < imageFiles.size(); i++) {
			
			if (imageFiles.get(i).getName().equalsIgnoreCase(wallpName)) { selected.set(i); break; }
		}
		
		this.cache = new Image[CACHED_BACK + 1 + CACHED_FRONT];
		this.structs = new ImageStruct[cache.length];
		
		this.cacheLoadInd = new AtomicInteger(0);
		
		Rectangle listpane = new Rectangle(pane.x + 30f, pane.y + 20f, (512f - 45f) * displayScale, (830f - 40f) * displayScale);
		
		this.list = new MenuList<File>(imageFiles, listpane, 53f, MenuList.MODE_VERTICAL, false) {
			
			@Override
			protected void up(boolean bybutton) {
				super.up(bybutton);
				
				indexChanged(selected);
			}
			
			@Override
			protected void down(boolean bybutton) {
				super.down(bybutton);
				
				indexChanged(selected);
			}
			
			@Override
			public void onAction(File entry) {
				
				if (tis.wallpName != entry.getName()) {
				
					tis.wallpName = entry.getName();
					
					try { field.set(null, entry.getName()); }
					catch (Exception e) { e.printStackTrace(); }
					
					onWallpaperChange();
					
				}
				
				exit();
			}
			
			@Override
			public void render(Graphics g, float pX, float pY) throws SlickException {
				
				int maxEntries = getMaxEntries();
				
				Color transp = new Color(1f, 1f, 1f, 0.6f);
				
				GUIRes.splitter.startUse();
				
				float space = pane.height - maxEntries * entryHeight;
				
				for (int i = camera, end = Math.min(camera + maxEntries, entries.size()); i < ((space < 20f * displayScale || end == entries.size()) ? end-1 : end); i++) {
					
					GUIRes.splitter.drawEmbedded((int)(-30f * displayScale + pane.x), (int)(pane.y + entryHeight * (i - camera + 1) - 1f), 
							(int)(pane.width + 20f * displayScale), (int)(GUIRes.splitter.getHeight() * displayScale + 0.5f), transp);
				}
				
				GUIRes.splitter.endUse();
					
				super.render(g, pX, pY);
			}
			
			@Override
			public void renderEntry(Graphics g, File entry, float x, float y, boolean selected, int ind) {
				
				if (entry.getName().equals(wallpName)) {
					
					float x1 = pane.x + pane.width - (20f + GUIRes.tick.getWidth() + 8f) * displayScale;
					float y1 = y + (entryHeight - GUIRes.tick.getHeight() * displayScale) * 0.5f;
					
					GUIRes.tick.draw(x1, y1, displayScale, Color.white.copy().setAlpha(0.8f));
				}
				
				super.renderEntry(g, entry, x, y, selected, ind);
			}
			
		};
		this.list.drawPanels = false;
		this.list.drawSplitter = false;
		this.list.setFocus(selected.get());
		
		this.loadThread = new Thread() {
			
			@Override
			public void run() {
				
				while (!isInterrupted()) {
				
					for (int i = CACHED_BACK; i > -1;) {
						
						cacheLoadInd.set(i);
						
						int fileInd = selected.get() - CACHED_BACK + i;
						
						if (fileInd >= 0 && fileInd < imageFiles.size()) {
						
							if (structs[i] == null || !imageFiles.get(fileInd).getName().equals(structs[i])) {
								
								File file = null;
								
								try {
									
									file = imageFiles.get(fileInd);
									
								} catch (Exception e) { e.printStackTrace(); }
								
								try {
									
									structs[i] = new ImageStruct(file.getAbsolutePath(), Image.FILTER_LINEAR);
									
								} catch (Exception e) { }
								
							}
							
						}
						
						if (i < CACHED_BACK) { i--; }
						else { i++; }
						
						if (i >= structs.length) { i = CACHED_BACK-1; }
						
					}
					
					cacheLoadInd.set(-1);
					
					try { sleep(100); } catch (InterruptedException e) { return; }
					
					cacheLoadInd.set(-1);
					
				}
				
			}
			
		};
		this.loadThread.setDaemon(true);
		this.loadThread.start();
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		loadingRot += UpdatingPanel.ROT_VEL * delta;
		loadingRot = FastMath.normalizeCircular(loadingRot, 0f, FastMath.PI2);
		
		this.list.update(delta);
		
		if (list.closed) { exit(); }
		
		for (int i = 0; i < cache.length; i++) {
			
			int ind = selected.get() - CACHED_BACK + i;
			if (ind < 0 || ind >= imageFiles.size()) { continue; }
			
			String fileName = imageFiles.get(ind).getName();
			
			if (cache[i] == null && i != cacheLoadInd.get() && structs[i] != null && structs[i].fileName.equals(fileName)) {
				
				cache[i] = new Image(structs[i]);
				cache[i].setFilter(Image.FILTER_LINEAR);
				cache[i].setName(fileName);
			}
			
		}
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		g.setColor(Color.white);
		
		GUIRes.drawContextPane(g, pane);
		
		float x = list.pane.x + list.pane.width + 20f * displayScale;
		
		float w = pane.x + pane.width - x - 20f * displayScale;
		float scw = w / Display.getWidth();
		float h = Display.getHeight() * scw;
		
		float y = pane.y + (pane.height - h) * 0.5f * displayScale;
		
		Image wallp = cache[CACHED_BACK];
		
		if (wallp != null) {
			
			float sw = w / wallp.getWidth();
			float sh = h / wallp.getHeight();
			
			float scale = Math.max(sw, sh);
			
			wallp.getSubImage((int)((wallp.getWidth() - w / scale) * 0.5f), (int)((wallp.getHeight() - h / scale) * 0.5f),
							  (int)(w / scale), (int)(h / scale)).draw(x, y, w, h);
			
		} else {
			
			float lx = x + (w - GUIRes.loading.getWidth() * displayScale) * 0.5f;
			float ly = y + (h - GUIRes.loading.getHeight() * displayScale) * 0.5f;
			
			GUIRes.loading.setRotation((loadingRot / FastMath.PI2) * 360f);
			GUIRes.loading.draw((int)lx, (int)ly, (int)(GUIRes.loading.getWidth() * displayScale), 
								(int)(GUIRes.loading.getHeight() * displayScale), Menu.flavors[Settings.menu_flavor]);
			
		}
		
		g.setColor(Color.grayTone(136));
		g.drawRect(x, y, w, h, 2);
		
		g.setColor(Color.white);
		g.setFont(Fonts.roboto.s36);
		
		String s = fieldName+" Selection";
		g.drawString(s, x + (((pane.x + pane.width) - x) - g.getFont().getWidth(s)) * 0.5f,
												pane.y + (90f * displayScale - g.getFont().getHeight(s)) * 0.5f);
		
		this.list.render(g, pX, pY);
		
		/*
		g.setFont(Fonts.roboto.s18);
		for (int i = 0; i < cache.length; i++) {
			
			String str = "null";
			if (cache[i] != null) { str = cache[i].getName(); }
			
			g.drawString(str, 10, 10 + i * g.getFont().getHeight("I"));
			
		}*/
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		super.handleInput(key, c, pressed);
		
		this.list.handleInput(key, c, pressed);
		
		if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
			
			exit();
		}
		
	}
	
	public void onWallpaperChange() {
		
		
		
	}
	
	public void indexChanged(int ind) {
		
		int change = ind - selected.get();
		this.selected.set(ind);
		
		if (Math.abs(change) == 0) { return; }
		
		if (Math.abs(change) > 1) {
			
			for (int i = 0; i < cache.length; i++) { destroy(cache[i]); cache[i] = null; structs[i] = null; }
			
		} else {
			
			//loadThread.interrupt();
			
			if (change < 0) {
				
				destroy(cache[cache.length-1]);
				
				for (int i = cache.length-1; i > 0; i--) {
					
					cache[i] = cache[i-1];
					structs[i] = structs[i-1];
				}
				
				cache[0] = null;
				structs[0] = null;
				
			} else {
				
				destroy(cache[0]);
				
				for (int i = 0; i < cache.length-1; i++) {
					
					cache[i] = cache[i+1];
					structs[i] = structs[i+1];
				}
				
				cache[cache.length-1] = null;
				structs[cache.length-1] = null;
				
			}
			
			//loadThread.start();
			
		}
		
		/*
		if (cache[CACHED_BACK] == null) {
			
			try {
				
				this.cache[CACHED_BACK] = new Image(imageFiles.get(selected).getAbsolutePath());
				this.cache[CACHED_BACK].setFilter(Image.FILTER_LINEAR);
				this.cache[CACHED_BACK].setName(imageFiles.get(selected).getName());
				
			} catch (Exception e) { }
			
		}*/
		
	}
	
	private void destroy(Image img) {
		if (img != null && img.getName().equalsIgnoreCase(wallpName)) { return; }
		
		try { img.destroy(); } catch (Exception | Error e) {}
		
	}
	
	private void exit() {
		
		this.closed = true;
		
		this.loadThread.interrupt();
		
		if (cache == null) { return; }
		
		for (Image img : cache) {
			
			destroy(img);
		}
		
	}
	
}
