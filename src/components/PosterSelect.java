package components;

import static main.Main.displayScale;

import java.io.File;
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

import main.Fonts;
import main.GUIRes;
import main.ImageFormats;
import main.Settings;
import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.util.Files;

public class PosterSelect extends Component {

	
	public static final int CACHED_BACK = 1, CACHED_FRONT = 1;
	
	
	public Rectangle pane;
	
	public List<File> posterFiles;
	public Image cache[];
	public ImageStruct[] structs;
	
	public AtomicInteger selected, cacheLoadInd;
	
	public MenuList<File> list;
	
	public MangaInfo info;
	
	public Thread loadThread;
	
	
	public PosterSelect(MangaInfo info) {
		
		float w = GUIRes.posterSelect.getWidth() * displayScale, h = GUIRes.posterSelect.getHeight() * displayScale;
		
		this.pane = new Rectangle((Display.getWidth() - w) / 2f, 150f * displayScale + (Display.getHeight() - 250f * displayScale - h) / 2f, w, h);
		
		this.info = info;
		
		File dir = new File(Settings.metaIn+"/"+info.title+"/_metadata/posters");
		if (!dir.exists()) { exit(); return; }
		
		this.posterFiles = Files.getFiles(dir, f -> !f.isDirectory() && ImageFormats.isSupported(f));
		Collections.sort(this.posterFiles);
		
		this.selected = new AtomicInteger(0);
		for (int i = 0; i < posterFiles.size(); i++) {
			
			if (posterFiles.get(i).getName().equalsIgnoreCase(info.poster)) { selected.set(i); break; }
		}
		
		this.cache = new Image[CACHED_BACK + 1 + CACHED_FRONT];
		this.structs = new ImageStruct[cache.length];
		
		this.cacheLoadInd = new AtomicInteger(-1);
		
		Rectangle listpane = new Rectangle(pane.x + 30f, pane.y + 20f, (512f - 60f) * displayScale, (830f - 40f) * displayScale);
		
		this.list = new MenuList<File>(posterFiles, listpane, 53f, MenuList.MODE_VERTICAL, false){
			
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
				
				if (!info.poster.equalsIgnoreCase(entry.getName())) {
				
					info.poster = entry.getName();
					
					try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					
					onPosterChanged();
					
				}
				
				exit();
			}
			
			@Override
			public void renderEntry(Graphics g, File entry, float x, float y, boolean selected, int ind) {
				
				if (entry.getName().equals(info.poster)) {
					
					float x1 = pane.x + pane.width - (20f + GUIRes.tick.getWidth() + 8f) * displayScale;
					float y1 = y + (entryHeight - GUIRes.tick.getHeight() * displayScale) * 0.5f;
					
					GUIRes.tick.draw(x1, y1, displayScale, Color.white.copy().setAlpha(0.8f));
				}
				
				super.renderEntry(g, entry, x, y, selected, ind);
			}
			
		};
		this.list.setFocus(selected.get());
		
		this.loadThread = new Thread() {
			
			@Override
			public void run() {
				
				while (!isInterrupted()) {
				
					for (int i = CACHED_BACK; i > -1;) {
						
						cacheLoadInd.set(i);
						
						int fileInd = selected.get() - CACHED_BACK + i;
						
						if (fileInd >= 0 && fileInd < posterFiles.size()) {
						
							if (structs[i] == null || !posterFiles.get(fileInd).getName().equals(structs[i].fileName)) {
								
								File file = null;
								
								try {
									
									file = posterFiles.get(fileInd);
									
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
	
	public void onPosterChanged() {
		
		
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		this.list.update(delta);
		
		if (list.closed) { exit(); }
		
		for (int i = 0; i < cache.length; i++) {
			
			int ind = selected.get() - CACHED_BACK + i;
			if (ind < 0 || ind >= posterFiles.size()) { continue; }
			
			String fileName = posterFiles.get(ind).getName();
			
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
		
		Image img = GUIRes.posterSelect;
		
		if (img != null) {
		
			img.draw(pane.x, pane.y, pane.width, pane.height);
			
		}
		
		Image poster = cache[CACHED_BACK];
		
		if (poster != null) {
			
			poster.draw(pane.x + (512f + 47f) * displayScale, pane.y + 90f * displayScale, 418f * displayScale, 650f * displayScale);
		}
		
		g.setColor(Color.white);
		g.setFont(Fonts.roboto.s36);
		
		String s = "Poster Selection";
		g.drawString(s, pane.x + 512f * displayScale + (512f * displayScale - g.getFont().getWidth(s)) * 0.5f,
												pane.y + (90f * displayScale - g.getFont().getHeight(s)) * 0.5f);
		
		this.list.render(g, pX, pY);
		
		/*
		g.setFont(Fonts.roboto.s18);
		for (int i = 0; i < cache.length; i++) {
			
			String str = "null";
			if (cache[i] != null) { str = cache[i].getName(); }
			if (structs[i] == null) { str += "[struct null"+((i == cacheLoadInd.get())?"?":"")+"]"; }
			
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
	
	public void indexChanged(int ind) {
		
		int change = ind - selected.get();
		this.selected.set(ind);
		
		if (Math.abs(change) == 0) { return; }
		
		if (Math.abs(change) > 1) {
			
			for (int i = 0; i < cache.length; i++) { destroy(cache[i]); cache[i] = null; }
			
		} else {
			
			if (change < 0) {
				
				destroy(cache[cache.length-1]);
				
				for (int i = cache.length-1; i > 0; i--) {
					
					cache[i] = cache[i-1];
				}
				
				cache[0] = null;
				
			} else {
				
				destroy(cache[0]);
				
				for (int i = 0; i < cache.length-1; i++) {
					
					cache[i] = cache[i+1];
				}
				
				cache[cache.length-1] = null;
				
			}
			
		}
		
	}
	
	private void destroy(Image img) {
		if (img != null && img.getName().equalsIgnoreCase(info.poster)) { return; }
		
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
