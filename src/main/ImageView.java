package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageStruct;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.math.FastMath;
import visionCore.util.Files;

public class ImageView extends Scene {

	
	public static final int CACHED_FRONT = 5, CACHED_BACK = 2;
	public static final int LOAD_TRESHOLD = 750;
	
	public long loadTimer;
	
	public File mangdir, chapdir, curfile, cachFiles[];
	public Image[] cached;
	public ImageStruct[] structs;
	
	public int curImg, curFileDirInd, curChapDirInd, pageturned;
	
	public AtomicInteger cachLoadInd;
	
	public List<File> chapfiles, prevchapfiles, nextchapfiles, mangaChaps;
	
	public String chname, chnrstr, mangaTitle;
	
	public float chalpha;
	public final float chalphavel = 0.001f;
	public int chtime;
	public final int chtimeEnd = 3000, chtimeEndMax = 6500, chtimePerChar = 200;
	
	public TrueTypeFont clockFont = null;
	public Color bgCol, textCol;
	
	public List<String> chinfo_strs;
	public int info_pages, info_pageNr;
	
	public static final float scrollSpeed = 2.4f, scrollStep = 60f;
	public static final int SCROLL_HOLD_TIME = 500;
			
	public float camX, camY;
	public double camZ;
	
	public int upHold, downHold, leftHold, rightHold;
	public boolean ctrlHold, altHold, shiftHold;
	
	public boolean lockedZoom, updateCamZoomNextFrame;
	public float lastWidth;
	
	public int pageActionQueue;
	
	public ImageLoadingThread loadingThread;
	
	
	public ImageView(File file) throws SlickException {
		
		pageActionQueue = 0;
		
		camX = 0f;
		camY = 0f;
		camZ = 1.0;
		
		updateCamZoomNextFrame = false;
		
		lockedZoom = false;
		lastWidth = 0;
		
		upHold = -1;
		downHold = -1;
		leftHold = -1;
		rightHold = -1;
		
		ctrlHold = false; altHold = false; shiftHold = false;
		
		chtime = 0;
		chalpha = 1f;
		
		loadTimer = 0;
		pageturned = 1;
		
		curfile = file;
		chapdir = file.getParentFile();
		mangdir = chapdir.getParentFile();
		
		mangaTitle = mangdir.getName().trim();
		
		MangaInfo info = Mangas.get(mangaTitle);
		
		if (info.lockedWidth > 1) {
			
			lockedZoom = true;
			lastWidth = info.lockedWidth;
			
		} else if (Mangas.knownLockedWidth(mangaTitle)) {
			
			lockedZoom = true;
			lastWidth = 576f;
			
		}
		
		getChapInfo();
		
		structs = new ImageStruct[CACHED_BACK + 1 + CACHED_FRONT];
		
		cachFiles = new File[CACHED_BACK + 1 + CACHED_FRONT];
		curImg = CACHED_BACK;
		cachFiles[curImg] = curfile;
		
		mangaChaps = Files.getFiles(mangdir, f -> f.isDirectory() && Character.isDigit(f.getName().charAt(0)));
		Collections.sort(mangaChaps);
		
		for (int i = 0; i < mangaChaps.size(); i++) {
			
			if (mangaChaps.get(i).equals(chapdir)) {
				
				curChapDirInd = i;
				break;
			}
		}
		
		if (curChapDirInd-1 >= 0) {
			
			prevchapfiles = Files.getFiles(mangaChaps.get(curChapDirInd-1), f -> ImageFormats.isSupported(f));
			Collections.sort(prevchapfiles);
		}
		
		chapfiles = Files.getFiles(chapdir, f -> ImageFormats.isSupported(f));
		Collections.sort(chapfiles);
		
		if (curChapDirInd+1 < mangaChaps.size()) {
			
			nextchapfiles = Files.getFiles(mangaChaps.get(curChapDirInd+1), f -> ImageFormats.isSupported(f));
			Collections.sort(nextchapfiles);
		}
		
		for (int i = 0; i < chapfiles.size(); i++) {
			
			if (chapfiles.get(i).equals(curfile)) {
				
				curFileDirInd = i;
				break;
			}
		}
		
		for (int i = 1; i <= CACHED_BACK; i++) {
			
			int n = curFileDirInd - i;
			
			if (n >= 0 && n < chapfiles.size()) {
				
				cachFiles[CACHED_BACK-i] = chapfiles.get(n);
				
			} else if (prevchapfiles != null && prevchapfiles.size() + n >= 0) {
				
				cachFiles[CACHED_BACK-i] = prevchapfiles.get(prevchapfiles.size()+n);
			}
			
		}
		
		for (int i = 1; i <= CACHED_FRONT; i++) {
			
			int n = curFileDirInd + i;
			
			if (n >= 0 && n < chapfiles.size()) {
				
				cachFiles[CACHED_BACK+i] = chapfiles.get(n);
				
			} else if (nextchapfiles != null && n - chapfiles.size() < nextchapfiles.size()) {
				
				cachFiles[CACHED_BACK+i] = nextchapfiles.get(n - chapfiles.size());
			}
			
		}
		
		clockFont = Fonts.roboto.s48;
		
		if (Settings.imageView_backgroundColor == 0) {
			
			bgCol = new Color(Color.black);
			textCol = new Color(Color.white);
			
		} else {
			
			bgCol = new Color(Color.white);
			textCol = new Color(Color.black);
		}
		
		System.out.println("Opened ImageView with \""+file.getAbsolutePath().replace("\\", "/")+"\".");
		
		cached = new Image[CACHED_BACK + 1 + CACHED_FRONT];
		curImg = CACHED_BACK;
		
		ImageStruct struct = null;
		try { struct = new ImageStruct(curfile); } 
		catch (IOException e) { }
		struct.filter = Image.FILTER_LINEAR;
		
		cached[curImg] = new Image(struct);
		cached[curImg].setFilter(Image.FILTER_LINEAR);
		cached[curImg].setName(curfile.getName());
		
		cachLoadInd = new AtomicInteger(curImg+1);
		
		updatePageInfo();
		
		loadingThread = new ImageLoadingThread();
		loadingThread.start();
		
	}
	
	private class ImageLoadingThread extends Thread {
		
		public ImageLoadingThread() {
			
			setDaemon(true);
		}
		
		@Override
		public void run() {
			
			while (!isInterrupted()) {
				
				for (int i = CACHED_BACK; i > -1;) {
					
					cachLoadInd.set(i);
					
					File file = null;
					
					try {
						
						file = cachFiles[i];
						
					} catch (Exception e) { e.printStackTrace(); }
					
					if (file != null && (structs[i] == null || !cachFiles[i].getName().equals(structs[i].fileName))) {
							
						try {
							
							//System.out.println(file.getName());
							
							structs[i] = new ImageStruct(file);
							structs[i].filter = Image.FILTER_LINEAR;
							
							//structs[i] = new ImageStruct(file.getAbsolutePath(), Image.FILTER_LINEAR);
							
						} catch (Exception e) { e.printStackTrace(); }
						
					}
						
					if (i < CACHED_BACK) { i--; }
					else { i++; }
					
					if (i >= structs.length) { i = CACHED_BACK-1; }
				}
				
				cachLoadInd.set(-1);
				
				try { sleep(100); } catch (InterruptedException e) { return; }
				
				cachLoadInd.set(-1);
				
			}
			
		}
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (loadTimer < LOAD_TRESHOLD) { loadTimer += delta; }
		
		if (updateCamZoomNextFrame) {
			
			updateCamZoomNextFrame = false;
			updateCamZoom();
		}
		
		if (upHold >= 0) {
			
			upHold = Math.min(upHold + delta, SCROLL_HOLD_TIME);
			
			if (upHold >= SCROLL_HOLD_TIME) {
				
				if (!shiftHold) {
					
					camY += scrollSpeed * delta;
					
				} else { camX += scrollSpeed * delta; }
			
				clampCam();
			}
		}
		
		if (downHold >= 0) {
			
			downHold = Math.min(downHold + delta, SCROLL_HOLD_TIME);
			
			if (downHold >= SCROLL_HOLD_TIME) {
				
				if (!shiftHold) {
					
					camY -= scrollSpeed * delta;
					
				} else { camX -= scrollSpeed * delta; }
				
				clampCam();
			}
		}
		
		if (leftHold >= 0) {
			
			leftHold = Math.min(leftHold + delta, SCROLL_HOLD_TIME);
			
			if (leftHold >= SCROLL_HOLD_TIME) {
				
				camX += scrollSpeed * delta;
				clampCam();
			}
		}
		
		if (rightHold >= 0) {
			
			rightHold = Math.min(rightHold + delta, SCROLL_HOLD_TIME);
			
			if (rightHold >= SCROLL_HOLD_TIME) {
				
				camX -= scrollSpeed * delta;
				clampCam();
			}
		}
		
		
		if (Math.abs(Main.rXAxis) > 0.2f && !(lockedZoom || cached[CACHED_BACK].getWidth() * getImgRenderScale(cached[CACHED_BACK]) * camZ < Display.getWidth())) {
			
			camX += Main.rXAxis * scrollSpeed * delta;
			clampCam();
		}
		
		if (Math.abs(Main.rYAxis) > 0.2f) {
			
			camY -= Main.rYAxis * scrollSpeed * delta;
			clampCam();
		}
		
		/*
		if (cachLoadInd != -1 && (loadTimer >= LOAD_TRESHOLD || cached[Math.max(Math.min(CACHED_BACK+pageturned, cached.length-1), 0)] == null)) {
			
			File f = cachFiles[cachLoadInd];
			
			if (f != null && cached[cachLoadInd] == null) {
				
				cached[cachLoadInd] = new Image(f.getAbsolutePath());
				cached[cachLoadInd].setFilter(Image.FILTER_LINEAR);
			}
			
			if (cachLoadInd >= CACHED_BACK) {
				
				cachLoadInd++;
				if (cachLoadInd >= cached.length) { cachLoadInd = CACHED_BACK-1; }
				
			} else { cachLoadInd--; }
			
		}
		*/
		
		for (int i = 0; i < cached.length; i++) {
			
			String fileName = "";
			if (cachFiles[i] != null) { fileName = cachFiles[i].getName(); }
			
			if (cached[i] == null && i != cachLoadInd.get() && cachFiles[i] != null && structs[i] != null && structs[i].fileName.equals(fileName)) {
				
				cached[i] = new Image(structs[i]);
				cached[i].setFilter(Image.FILTER_LINEAR);
				cached[i].setName(fileName);
				
				break;
			}
			
		}
		
		if (chtime < Math.min(Math.max(chtimeEnd, chtimePerChar * chname.length()), chtimeEndMax)) {
			
			chtime += delta;
			
		} else if (chalpha > 0f) {
			
			chalpha -= chalphavel * delta;
			if (chalpha <= 0f) { chalpha = 0f; chtime = 0; }
		}
		
		
		if (pageActionQueue != 0) {
			
			if (pageActionQueue > 0 && cached[CACHED_BACK+1] != null) {
				
				pageActionQueue = 0;
				nextPage();
				
			} else if (pageActionQueue < 0 && cached[CACHED_BACK-1] != null) {
				
				pageActionQueue = 0;
				prevPage();
			}
			
		}
		
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		
		Graphics.setCurrent(g);
		g.setColor(bgCol);
		g.fillRect(0f, 0f, Display.getWidth(), Display.getHeight());
		
		Image img = cached[curImg];
		
		float imgwidth = -1f;
		
		if (img != null) {
			
			float scale = (float)(getImgRenderScale(img) * camZ);
			
			imgwidth = scale * img.getWidth();
			if (lockedZoom && (camZ > 1.0 || lastWidth == 0f)) { lastWidth = imgwidth; }
			
			float w = img.getWidth() * scale;
			float h = img.getHeight() * scale;
			
			if (scale > 1f && Shaders.working && Settings.video_bicubicFiltering) {
				
				Shaders.bicubic.bind();
			}
			
			img.draw((int)(camX + 0.5f), (int)(camY + 0.5f), (int)w, (int)h);
			
			if (scale > 1f && Shaders.working && Settings.video_bicubicFiltering) {
				
				Shaders.bicubic.unbind();
			}
		}
		
		g.setColor(textCol);
		
		if (Settings.imageView_debug) {
		
			for (int i = 0; i < cached.length; i++) {
				
				String s = "null";
				
				File f = cachFiles[i];
				if (f != null) { s = f.getName(); }
				
				if (cached[i] == null) { s = "null"; }
				
				if (i == CACHED_BACK) { s += " (current)"; }
				
				g.drawString("cached["+i+"] = "+s, 10, 30 + 15 * i);
				
			}
			
		}
		
		if (!Settings.imageView_alwaysShowChapter) {
			
			if (chalpha > 0) {
				
				renderChapterInfo(g, chalpha, imgwidth);
			}
			
		} else { renderAltChapterInfo(g); }
		
		if (Settings.imageView_showTime) {
			
			Clock.renderClock(g, -20f, 10f, textCol, clockFont, camZ > 1);
		}
		
		if (camZ > 1f || lockedZoom) {
			
			String z = (int)(camZ * 100f + 0.5f)+"% Zoom";
			
			if (lockedZoom) { z = "["+(int)(Math.ceil(lastWidth))+"px]"; }
			
			g.setColor(textCol);
			
			g.setFont(Fonts.roboto.s36);
			g.drawStringShadow(z, 20f, 10f, (int)(2f * Main.displayScale + 0.5f), 0.2f, bgCol);
		}
		
		if (Settings.imageView_showPageNr) {
			
			renderPageNr(g);
		}
		
		if (camZ > 1.0) {
			
			renderScrollbar(g);
		}
		
	}
	
	private void renderChapterInfo(Graphics g, float alpha, float imgwidth) {
		
		g.setColor(textCol, alpha);
		g.setFont(Fonts.roboto.s60);
		
		String s0 = "CHAPTER "+chnrstr;
		String s1 = chname.toUpperCase();
		
		float x0 = 20f;
		float x1 = 20f;
		
		if (imgwidth > Display.getWidth() * 0.667f) {
			
			x0 = (Display.getWidth() - g.getFont().getWidth(s0)) * 0.5f;
			x1 = (Display.getWidth() - g.getFont().getWidth(s1)) * 0.5f;
			
			g.drawStringShadow(s0, (int)x0, 10, 2f, 0.4f * alpha, bgCol);
			g.drawStringShadow(s1, (int)x1, (int)(10 + g.getFont().getLineHeight() - 14f), 2f, 0.4f * alpha, bgCol);
			
		} else {
			
			for (int i = 0; i < chinfo_strs.size(); i++) {
				
				g.drawString(chinfo_strs.get(i), 20, (int)(10 + i * g.getFont().getHeight(s0)));
			}
			
		}
		
		g.setColor(textCol, 1f);
	}
	
	private void renderAltChapterInfo(Graphics g) {
		
		g.setColor(textCol, 1f);
		g.setFont(Fonts.roboto.s24);
		
		for (int j = 0; j < chinfo_strs.size(); j++) {
			
			String s = chinfo_strs.get(j);
			float x = 20f;
			float y = (int)(Display.getHeight() - g.getFont().getHeight(chinfo_strs.get(0)) * (chinfo_strs.size() - j) - 10);
			
			if (camZ > 1f) {
				
				g.drawStringShadow(s, x, y, (int)(2f * Main.displayScale + 0.5f), 0.2f, bgCol);
				
			} else { g.drawString(s, x, y); }
		}
		
	}
	
	private void renderPageNr(Graphics g) {
		
		String nrstr = info_pageNr+"";
		String pagesStr = info_pages+"";
		
		for (int i = 0, l = nrstr.length(); i < pagesStr.length()-l; i++) { nrstr = "0"+nrstr; }
		
		String s = nrstr+"/"+pagesStr;
		
		g.setFont(Fonts.roboto.s24);
		
		float x = (int)(Display.getWidth() - g.getFont().getWidth(s) - 20f);
		float y = (int)(Display.getHeight() - g.getFont().getHeight(s) - 10);
		
		g.setColor(textCol);
		
		if (camZ > 1f) {
			
			g.drawStringShadow(s, x, y, (int)(2f * Main.displayScale + 0.5f), 0.2f, bgCol);
			
		} else { g.drawString(s, x, y); }
		
	}
	
	private void renderScrollbar(Graphics g) {
		
		Image img = cached[CACHED_BACK];
		
		float w = (float)(img.getWidth() * getImgRenderScale(img) * camZ);
		float h = (float)(img.getHeight() * getImgRenderScale(img) * camZ);
		
		float x = camX + w;
		
		x = Math.min(x + 16f * Main.displayScale, Display.getWidth() - 8f * Main.displayScale - 5f);
		
		g.setColor(textCol.copy().setAlpha(0.5f));
		g.fillRoundRect((int)x, -(int)((camY / h) * Display.getHeight()), 5, (int)((Display.getHeight() / h) * Display.getHeight()), 10);
		
	}
	
	
	private void prevPage() {
		
		if (cached[CACHED_BACK-1] == null) {
			
			pageActionQueue = -1;
			return;
		}
		
		if (curFileDirInd > 0 || prevchapfiles != null) {
			
			if (curFileDirInd == 0) {
				
				prevChapter();
				checkForNulls();
				
			} else { curFileDirInd--; }
			
			try { cached[cached.length-1].destroy(); } catch (Exception | Error e) {}
			
			for (int i = cached.length-1; i >= 1; i--) {
				
				cached[i] = cached[i-1];
				cachFiles[i] = cachFiles[i-1];
				structs[i] = structs[i-1];
			}
			
			cachFiles[0] = null;
			cached[0] = null;
			structs[0] = null;
			
			int n = curFileDirInd - CACHED_BACK;
			if (n >= 0) { cachFiles[0] = chapfiles.get(n); }
			else if (prevchapfiles != null && prevchapfiles.size() + n >= 0) { cachFiles[0] = prevchapfiles.get(prevchapfiles.size()+n); }
			
			if (cachLoadInd.get() == -1) { cachLoadInd.set(Math.max(CACHED_BACK-1, 0)); }
			else { cachLoadInd.set(CACHED_BACK); }
			
			updatePageInfo();
			
			pageturned = -1;
		}
		
	}
	
	private void nextPage() {
		
		if (curFileDirInd == chapfiles.size()-1 && nextchapfiles == null) {
			
			exitView();
			
			MangaInfo info = Mangas.get(mangaTitle);
			info.lastPage = -1;
			info.lastChapter = -1;
			info.read = true;
			
			saveInfo(info);
			
			return;
		}
		
		if (cached[CACHED_BACK+1] == null) {
			
			pageActionQueue = 1;
			return;
		}
		
		if (curFileDirInd < chapfiles.size()-1 || nextchapfiles != null) {
			
			boolean chapterChanged = false;
			
			if (curFileDirInd == chapfiles.size()-1) {
				
				nextChapter();
				checkForNulls();
				chapterChanged = true;
				
			} else { curFileDirInd++; }
			
			try { cached[0].destroy(); } catch (Exception | Error e) {}
			
			for (int i = 0; i < cached.length-1; i++) {
				
				cached[i] = cached[i+1];
				cachFiles[i] = cachFiles[i+1];
				structs[i] = structs[i+1];
			}
			
			cachFiles[cachFiles.length-1] = null;
			cached[cached.length-1] = null;
			structs[structs.length-1] = null;
			
			int n = curFileDirInd + CACHED_FRONT;
			if (n < chapfiles.size()) { cachFiles[cachFiles.length-1] = chapfiles.get(n); }
			else if (nextchapfiles != null && n - chapfiles.size() < nextchapfiles.size()) { cachFiles[cachFiles.length-1] = nextchapfiles.get(n - chapfiles.size()); }
			
			if (cachLoadInd.get() == -1) { cachLoadInd.set(Math.min(CACHED_BACK+1, cached.length-1)); }
			else { cachLoadInd.set(CACHED_BACK); }
			
			updatePageInfo();
			
			if (chapterChanged) { saveLastRead(); }
			
			pageturned = 1;
		}
		
	}
	
	
	private void prevChapter() {
		if (prevchapfiles == null) { return; }
		
		curChapDirInd--;
		chapdir = mangaChaps.get(curChapDirInd);
		
		nextchapfiles = chapfiles;
		chapfiles = prevchapfiles;
		prevchapfiles = null;
		
		if (curChapDirInd > 0) {
			
			prevchapfiles = Files.getFiles(mangaChaps.get(curChapDirInd-1), f -> ImageFormats.isSupported(f));
			Collections.sort(prevchapfiles);
		}
		
		curFileDirInd = chapfiles.size()-1;
		
		chtime = 0;
		chalpha = 1f;
		if (getImgRenderWidth(cached[curImg]) > Display.getWidth() * 0.667f) { chtime = -(int)(chtimeEnd * 0.5f); }
		
		getChapInfo();
	}
	
	private void nextChapter() {
		if (nextchapfiles == null) { return; }
		
		curChapDirInd++;
		chapdir = mangaChaps.get(curChapDirInd);
		
		prevchapfiles = chapfiles;
		chapfiles = nextchapfiles;
		nextchapfiles = null;
		
		if (curChapDirInd < mangaChaps.size()-1) {
			
			nextchapfiles = Files.getFiles(mangaChaps.get(curChapDirInd+1), f -> ImageFormats.isSupported(f));
			Collections.sort(nextchapfiles);
		}
		
		curFileDirInd = 0;
		
		chtime = 0;
		chalpha = 1f;
		if (getImgRenderWidth(cached[curImg]) > Display.getWidth() * 0.667f) { chtime = -(int)(chtimeEnd * 0.5f); }
		
		getChapInfo();
	}
	
	private void checkForNulls() {
		
		for (int i = 0; i < cachFiles.length; i++) {
			
			if (cachFiles[i] == null) {
				
				int n = curFileDirInd - (CACHED_BACK - i);
				
				if (n < 0 && prevchapfiles != null && prevchapfiles.size() + n >= 0) {
					int ind = prevchapfiles.size() + n + 1;
					if (ind >= 0 && ind < prevchapfiles.size()) {
						
						cachFiles[i] = prevchapfiles.get(ind);
					}
					
				} else if (n >= chapfiles.size() && nextchapfiles != null && nextchapfiles.size() > n - chapfiles.size() - 1) {
					int ind = n - chapfiles.size() - 1;
					if (ind >= 0 && ind < nextchapfiles.size()) {
						
						cachFiles[i] = nextchapfiles.get(ind);
					}
					
				} else if (n >= 0 && n < chapfiles.size()) { cachFiles[i] = chapfiles.get(n); }
			}
			
		}
		
		cachLoadInd.set(CACHED_BACK);
		
		/*
		for (int i = 0; i < cached.length; i++) {
			
			if (cached[i] == null) {
			
				try {
					cached[i] = new Image(cachFiles[i].getAbsolutePath());
				} catch (Exception e) { e.printStackTrace(); }
				
			}
			
		}
		*/
		
	}
	
	
	@Override
	public void mouseWheelMoved(int change) {
		
		if (!shiftHold && !ctrlHold) {
			
			if (camZ > 1 || lockedZoom) {
				
				camY += change;
				clampCam();
				
			} else {
				
				if (change < 0) { nextPage(); }
				else { prevPage(); }
				
			}
			
		} else if (ctrlHold && !shiftHold) {
			
			if (change >= 0f) { zoomIn(); }
			else { zoomOut(); }
			
		} else if (shiftHold && !ctrlHold) {
			
			camX += change;
			clampCam();
		}
		
	}
	
	@Override
	public void mouseInput(int button, boolean pressed) {
		
		if (pressed) {
		
			if (button == 0) {
				
				if (Main.mouseX >= Display.getWidth() / 3f) {
					
					nextPage();
					
				} else {
					
					prevPage();
				}
				
			} else if (button == 3) {
				
				prevPage();
				
			} else if (button == 4) {
				
				nextPage();
			}
		}
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		super.handleInput(key, c, pressed);
		
		if (key == Input.KEY_UP) {
			
			upHold = pressed ? 0 : -1;
			
			if (pressed) {
				
				if (!shiftHold) {
					
					camY += scrollStep;
					
				} else { camX += scrollStep; }
				
				clampCam(); 
			}
			
		} else if (key == Input.KEY_DOWN) {
			
			downHold = pressed ? 0 : -1;
			
			if (pressed) { 
				
				if (!shiftHold) {
					
					camY -= scrollStep;
					
				} else { camX -= scrollStep; }
				
				clampCam();
			}
			
		} else if (key == Input.KEY_LCONTROL || key == Input.KEY_RCONTROL) {
			
			ctrlHold = pressed;
			
		} else if (key == Input.KEY_LALT || key == Input.KEY_RALT) {
			
			altHold = pressed;
			
		} else if (key == Input.KEY_LSHIFT || key == Input.KEY_RSHIFT) {
			
			shiftHold = pressed;
			
		}
		
		if (pressed) {
			
			float imgWidth = (cached[curImg] == null) ? 0f : (float)(getImgRenderWidth(cached[curImg]) * camZ);
			
			if (key == Input.KEY_LEFT) {
				
				if (ctrlHold || (imgWidth > Display.getWidth() && camZ > 1.0)) {
					
					leftHold = 0;
					
					camX += scrollStep;
					clampCam();
					
				} else {
					
					prevPage();
				}
				
			} else if (key == Input.KEY_RIGHT) {
				
				if (ctrlHold || (imgWidth > Display.getWidth() && camZ > 1.0)) {
					
					rightHold = 0;
					
					camX -= scrollStep;
					clampCam();
					
				} else {
					
					nextPage();
				}
				
			} else if (key == Input.KEY_LALT || key == Input.KEY_RALT) {
				
				saveLastRead();
				
			} else if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
				
				exitView();
				
			} else if ((key == 13 && c == '+') || key == Input.KEY_ADD) {
				
				zoomIn();
				
			} else if (key == Input.KEY_MINUS || key == Input.KEY_SUBTRACT) {
				
				zoomOut();
				
			} else if (key == Input.KEY_0 || key == Input.KEY_NUMPAD0) {
				
				camZ = 1f;
				updateCam();
				
			} else if (key == Input.KEY_L) {
				
				lockedZoom = !lockedZoom;
				
			} else if (key == Input.KEY_END) {
				
				camY = Float.NEGATIVE_INFINITY;
				clampCam();
				
			} else if (key == Input.KEY_HOME) {
				
				camY = 0;
				clampCam();
				
			} else if (key == Input.KEY_PAGE_UP) {
				
				camY = (int)(camY + Display.getHeight() * 0.75f);
				clampCam();
				
			} else if (key == Input.KEY_PAGE_DOWN) {
				
				camY = (int)(camY - Display.getHeight() * 0.75f);
				clampCam();
				
			}
			
		} else {
			
			if (key == Input.KEY_LEFT) {
				
				leftHold = -1;
				
			} else if (key == Input.KEY_RIGHT) {
				
				rightHold = -1;
				
			}
			
		}
		
	}
	
	private void exitView() {
		
		saveLastRead();
		clearScene();
		
		File d = new File(Settings.mangaDir+"/"+mangaTitle);
		
		File[] fl = null;
		if (d != null) {
			
			fl = d.listFiles(f -> f.isDirectory() && Character.isDigit(f.getName().trim().charAt(0)));
		}
		
		if (fl != null && fl.length == 1) {
			
			Main.currentScene = new MangaView(Settings.menu_mangaMode, mangaTitle);
			
		} else {
		
			Main.currentScene = new ChapterView(mangaTitle);
		}
	}
	
	private void zoomIn() {
		
		if (!lockedZoom) {
		
			float step = 0.25f;
			if (camZ >= 2f) { step = 0.5f; }
			
			camZ = Math.min((int)(camZ / 0.25f) * 0.25f + step, 4f);
			
		} else {
			
			float width = lastWidth;
			float step = Display.getWidth() * 0.05f;
			
			width = Math.min((int)(Math.ceil(width) / step) * step + step, Display.getWidth());
			
			Image img = cached[CACHED_BACK];
			camZ = Math.max(1f, width / (img.getWidth() * getImgRenderScale(img)));
			
		}
		
		updateCam();
	}
	
	private void zoomOut() {
		
		if (!lockedZoom) {
		
			float step = 0.25f;
			if (camZ > 2f) { step = 0.5f; }
			
			camZ = Math.max((int)(camZ / 0.25f) * 0.25f - step, 1f);
			
		} else {
			
			Image img = cached[CACHED_BACK];
			
			float width = lastWidth;
			float step = Display.getWidth() * 0.05f;
			
			width = Math.max((int)(Math.ceil(width) / step) * step - step, img.getWidth() * getImgRenderScale(img));
			
			camZ = Math.max(1f, width / (img.getWidth() * getImgRenderScale(img)));
			
		}
		
		updateCam();
	}
	
	private void updateCam() {
		
		Image img = cached[CACHED_BACK];
		if (img != null && !img.isDestroyed()) {
			
			float scale = (float)(getImgRenderScale(img) * camZ);
			
			if (img.getWidth() / Display.getWidth() <= img.getHeight() / Display.getHeight()) {
				
				camX = (Display.getWidth() - img.getWidth() * scale) * 0.5f;
				
			} else {
				
				camY = (Display.getHeight() - img.getHeight() * scale) * 0.5f;
			}
			
			clampCam();
		}
		
	}
	
	private void clampCam() {
		
		Image img = cached[CACHED_BACK];
		if (img != null && !img.isDestroyed()) {
			
			float scale = (float)(getImgRenderScale(img) * camZ);
			
			camX = FastMath.clampToRangeC(camX, Display.getWidth() - img.getWidth() * scale, 0f);
			camY = FastMath.clampToRangeC(camY, Display.getHeight() - img.getHeight() * scale, 0f);
		}
		
	}
	
	
	@Override
	public void clearScene() {
		
		for (Image img : cached) {
			
			if (img == null) { continue; }
			
			try { img.destroy(); } catch (Exception e) { e.printStackTrace(); }
		}
		
	}
	
	private void getChapInfo() {
		
		chname = chapdir.getName();
		chnrstr = chname.substring(0, chname.indexOf(" -"));
		chname = chname.substring(Math.min(chname.indexOf(" -")+3, chname.length()-1));
		
		double chapnr = Double.parseDouble(chnrstr.trim());
		chnrstr = chapnr+"";
		
		if (chapnr == Math.floor(chapnr)) { chnrstr = chnrstr.substring(0, chnrstr.indexOf(".")); }
		
	}
	
	private float getImgRenderScale(Image img) {
		if (img == null) { return -1f; }
		
		float w = (float)Display.getWidth() / (float)img.getWidth();
		float h = (float)Display.getHeight() / (float)img.getHeight();
		
		return Math.min(w, h);
	}
	
	private float getImgRenderWidth(Image img) {
		if (img == null) { return -1f; }
		
		return img.getWidth() * getImgRenderScale(img);
	}
	
	private float getImgRenderHeight(Image img) {
		if (img == null) { return -1f; }
		
		return img.getHeight() * getImgRenderScale(img);
	}
	
	private void updatePageInfo() {
		
		String fn = cachFiles[CACHED_BACK].getName().trim();
		info_pageNr = parseExtraSafely(fn);
		
		fn = chapfiles.get(chapfiles.size()-1).getName().trim();
		info_pages = parseExtraSafely(fn);
		
		buildChapterStrings();
		
		loadTimer = 0;
		
		updateCamZoom();
		
		try {
		
			if (curFileDirInd >= chapfiles.size()-1) {
				
				Mangas.get(mangaTitle).readChapters.add(Double.parseDouble(chnrstr));
			}
			
		} catch (Exception e) {}
		
	}
	
	private int parseExtraSafely(String s) {
		
		String toParse = "";
		
		for (int i = 0, len = s.length(); i < len; i++) {
			
			char c = s.charAt(i);
			
			if (Character.isDigit(c)) {
				
				toParse += c;
				
			} else if (!toParse.isEmpty()) {
				
				break;
			}
		}
		
		int n = 0;
		
		if (!toParse.isEmpty()) {
			
			try { n = Integer.parseInt(toParse); }
			catch (Exception e) { e.printStackTrace(); }
		}
		
		return n;
	}
	
	private void updateCamZoom() {
		
		Image img = cached[CACHED_BACK];
		
		if (img != null) {
		
			if (!lockedZoom) {
				
				camZ = 1f;
				
			} else {
				
				camZ = Math.max(1f, lastWidth / (img.getWidth() * getImgRenderScale(img)));
				
			}
			
			
			float scale = (float)(getImgRenderScale(img) * camZ);
			
			camX = (Display.getWidth() - img.getWidth() * scale) * 0.5f;
			camY = (Display.getHeight() - img.getHeight() * scale) * 0.5f;

			
			if (lockedZoom) {
				
				if (img.getWidth() / Display.getWidth() > img.getHeight() / Display.getHeight()) {
					
					camX = 0f;
					
				} else { camY = 0f; }
				
				updateCam();
				
			}
			
		} else { updateCamZoomNextFrame = true; }
		
	}
	
	private void saveLastRead() {
		
		MangaInfo info = Mangas.get(mangaTitle);
		info.lastPage = info_pageNr;
		info.lastChapter = Double.parseDouble(chnrstr);
		info.lockedWidth = lockedZoom ? (int)Math.ceil(lastWidth) : 0;
		info.lastReadMillis = System.currentTimeMillis();
		
		saveInfo(info);
		
		/*
		if (Settings.MAL_sync && MAL.canAuthenticate()) {
		
			new Thread(){
				
				@Override
				public void run() {
					
					MAL.updateInList(info.copy());
				}
				
			}.start();
		}
		*/
		
		Mangas.lastRead = mangaTitle;
		Mangas.saveGlobalMeta();
	}
	
	private void saveInfo(MangaInfo info) {
		
		try { info.save(new File(mangdir.getAbsolutePath()+"/_metadata/info.xml")); } catch (Exception e) {}
		try { info.save(new File(Settings.metaIn+"/"+mangaTitle+"/_metadata/info.xml")); } catch (Exception e) {}
	}
	
	private void buildChapterStrings() {
		
		if (chinfo_strs == null) {
			chinfo_strs = new ArrayList<String>();
		} else { chinfo_strs.clear(); }
		
		if (!Settings.imageView_alwaysShowChapter) {
			
			String s0 = "CHAPTER "+chnrstr;
			String s1 = chname.toUpperCase();
			
			chinfo_strs.add(s0);
			
			float gap = (Display.getWidth() - getImgRenderWidth(cached[curImg])) * 0.5f;
			
			for (String s = s1, sa = ""; s.trim().length() > 0; s = sa) {
				
				sa = "";
				
				while (Fonts.roboto.s60.getWidth(s) > gap - 40f && s.contains(" ")) {
					
					String a = s.substring(0, s.lastIndexOf(" ")).trim();
					sa = (s.substring(Math.min(a.length()+1, s.length()-1)).trim() + " " + sa).trim();
					s = a;
				}
				
				chinfo_strs.add(s);
			}
			
		} else {
		
			chinfo_strs.add("CHAPTER "+chnrstr);
			
			float gapH = (Display.getWidth() - getImgRenderWidth(cached[curImg])) * 0.5f;
			float gapV = (Display.getHeight() - getImgRenderHeight(cached[curImg])) * 0.5f;
			
			int i = 0;
			for (String s = chname.toUpperCase(), sa = ""; s.trim().length() > 0; s = sa) {
				
				sa = "";
				
				while (Fonts.roboto.s24.getWidth(s) > gapH - 40f && gapV < 68 && s.contains(" ")) {
					
					String a = s.substring(0, s.lastIndexOf(" ")).trim();
					sa = (s.substring(Math.min(a.length()+1, s.length()-1)).trim() + " " + sa).trim();
					s = a;
				}
				
				chinfo_strs.add(s);
				i++;
			}
			
		}
		
	}
	
	
	public static ImageView openLastRead() {
		
		return openLastRead(Mangas.lastRead);
	}
	
	public static ImageView openLastRead(String mangaTitle) {
		
		MangaInfo info = Mangas.get(mangaTitle);
		if (info == null) { System.out.println("\""+mangaTitle+"\" not found."); return null; }
		
		String chstart = Math.max(info.lastChapter, 1.0)+"";
		
		for (int i = 0, l = ((int)(info.lastChapter)+"").length(); i < 4 - l; i++) { chstart = "0"+chstart; }
		if (info.lastChapter == Math.floor(info.lastChapter)) { chstart = chstart.substring(0, chstart.indexOf(".")); }
		chstart += " -";
		
		String pagestart = Math.max(info.lastPage, 1)+"";
		for (int i = 0, l = pagestart.length(); i < 3 - l; i++) { pagestart = "0"+pagestart; }
		
		File dir = new File(Settings.mangaDir+"/"+info.title);
		if (!dir.exists()) { System.out.println("Directory of \""+info.title+"\" not found."); return null; }
		
		File chdir = null;
		for (File fl : dir.listFiles()) {
			if (!fl.isDirectory()) { continue; }
			
			if (fl.getName().trim().toLowerCase().startsWith(chstart)) {
				
				chdir = fl;
				break;
			}
		}
		if (chdir == null) { System.out.println("No chapter found."); return null; }
		
		File pagefile = null;
		for (File f : chdir.listFiles()) {
			if (f.isDirectory()) { continue; }
			
			if (f.getName().trim().toLowerCase().startsWith(pagestart)) {
				
				pagefile = f;
				break;
			}
		}
		
		if (pagefile == null) { System.out.println("No page found."); return null; }
		
		ImageView iv = null;
		
		try {
			iv = new ImageView(pagefile);
		} catch (Exception e) { e.printStackTrace(); }
		
		return iv;
	}
	
}
