package main;

import static main.Main.displayScale;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.ImageData;
import org.newdawn.slick.opengl.ImageDataFactory;
import org.newdawn.slick.opengl.InternalTextureLoader;
import org.newdawn.slick.opengl.LoadableImageData;

import mangaLib.MangaInfo;
import visionCore.dataStructures.tuples.Triplet;
import visionCore.dataStructures.tuples.Tuple;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;
import visionCore.util.Files;

public class MangaList extends MenuList<MangaInfo> {

	
	public static final int SORT_ALPHABETICALLY = 0, SORT_RECENTLY_READ = 1, SORT_RECENTLY_UPDATED = 2, SORT_AUTHOR = 3;
	
	
	public List<Triplet<String, ByteBuffer, ImageData>> startingImgs;
	public AtomicReference<List<MangaInfo>> entryBuffer;
	public AtomicReference<List<Tuple<String, String>>> postersToLoad;
	public AtomicReference<List<Triplet<String, ByteBuffer, ImageData>>> loadedPosterData;
	
	public InfoLoadThread infosThread;
	public ImageLoadThread imgThread;
	
	
	public boolean loadMetaFromDir, sortByRank;
	
	public File metadir;
	public HashMap<String, Image> posters;
	
	public float loadingRot;
	
	
	public MangaList(File metadir, Rectangle pane, boolean loadMetaFromDir) {
		
		this(new ArrayList<MangaInfo>(), metadir, null, pane, loadMetaFromDir);
	}
	
	public MangaList(File metadir, Predicate<File> dirFilter, Rectangle pane, boolean loadMetaFromDir) {
		
		this(new ArrayList<MangaInfo>(), metadir, dirFilter, pane, loadMetaFromDir);
	}
	
	public MangaList(List<MangaInfo> entries, File metadir, Predicate<File> dirFilter, Rectangle pane, boolean loadMetaFromDir) {
		
		this(entries, metadir, dirFilter, pane, MenuList.MODE_VERTICAL, true, false);
	}
	
	protected MangaList(List<MangaInfo> entries, File metadir, Predicate<File> dirFilter, Rectangle pane, int mode, boolean loadMetaFromDir, boolean drawPanels) {
		super(entries, pane, getEntrySize(mode), mode, drawPanels);
		
		this.posters = new HashMap<String, Image>();
		
		this.loadMetaFromDir = loadMetaFromDir;
		this.drawPanels = drawPanels;
		
		this.metadir = metadir;
		
		this.loadingRot = 0f;
		
		this.sortByRank = false;
		
		this.loadedPosterData = new AtomicReference<List<Triplet<String, ByteBuffer, ImageData>>>(Collections.synchronizedList(new ArrayList<Triplet<String, ByteBuffer, ImageData>>()));
		this.postersToLoad = new AtomicReference<List<Tuple<String, String>>>();
		
		List<Tuple<String, String>> p2l = Collections.synchronizedList(new ArrayList<Tuple<String, String>>(entries.size()));
		postersToLoad.set(p2l);
		
		
		startingImgs = Collections.synchronizedList(new ArrayList<Triplet<String, ByteBuffer, ImageData>>());
		for (int i = 0; i < entries.size(); i++) { startingImgs.add(null); }
		
		ExecutorService exec = Executors.newCachedThreadPool();
		
		try {
		
			for (int i = 0, size = entries.size(); i < size; i++) {
				MangaInfo info = entries.get(i);
				
				//p2l.add(new Tuple<String, String>(info.title, info.poster));
				
				final int ind = i;
				
				exec.submit(new Runnable(){
					
					@Override
					public void run() {
						
						File notdone = new File(metadir.getAbsolutePath()+"/"+info.title+"/_metadata/posters/notdone");
						if (notdone.exists()) { return; }
					
						File imgF = new File(metadir.getAbsolutePath()+"/"+info.title+"/_metadata/posters/"+info.poster);
						if (!imgF.exists()) { return; }
						
						LoadableImageData imageData = null;
						ByteBuffer imageBuffer = null;
						try {
							
							FileInputStream fis = new FileInputStream(imgF);
							BufferedInputStream bufin = new BufferedInputStream(fis);
							
							imageData = ImageDataFactory.getImageDataFor(imgF.getAbsolutePath());
							imageBuffer = imageData.loadImage(bufin, false, null);
							
							bufin.close();
							fis.close();
							
						} catch (Exception e) { e.printStackTrace(); }
						
						if (imageData != null && imageBuffer != null) {
							
							Triplet<String, ByteBuffer, ImageData> data = new Triplet<String, ByteBuffer, ImageData>(info.title, imageBuffer, imageData);
							
							startingImgs.set(ind, data);
						}
						
					}
					
				});
				
			}
		
		} finally {
			
			exec.shutdown();
			
		}
		
		
		this.entryBuffer = new AtomicReference<List<MangaInfo>>(Collections.synchronizedList(new ArrayList<MangaInfo>()));
		
		HashSet<String> ignores = new HashSet<String>();
		for (MangaInfo info : entries) { ignores.add(info.title.trim().toLowerCase()); }
		
		if (loadMetaFromDir) {
			
			infosThread = new InfoLoadThread(metadir, dirFilter, ignores);
			infosThread.start();
		}
		
		imgThread = new ImageLoadThread(metadir);
		imgThread.start();
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		loadingRot += UpdatingPanel.ROT_VEL * delta;
		loadingRot = FastMath.normalizeCircular(loadingRot, 0f, FastMath.PI2);
		
		List<MangaInfo> buffer = entryBuffer.get();
		
		for (int i = 0; i < buffer.size(); i++) {
			MangaInfo info = buffer.get(i);
			
			if (info != null) {
				
				buffer.remove(i); i--;
				entryBuffer.set(buffer);
				
				entries.add(info);
				sortEntries();
				
				List<Tuple<String, String>> p2l = postersToLoad.get();
				p2l.add(new Tuple<String, String>(info.title, info.poster));
				postersToLoad.set(p2l);
				
				//break;
			}
			
		}
		
		for (int i = 0; i < startingImgs.size(); i++) {
			Triplet<String, ByteBuffer, ImageData> data = startingImgs.get(i);
			
			if (data != null) {
				
				Image img = loadImageFromData(data);
				
				if (img != null) {
					
					posters.put(data.x, img);
					
					startingImgs.set(i, null);
					
					//return;
				}
				
			}
			
		}
		
		List<Triplet<String, ByteBuffer, ImageData>> lpd = loadedPosterData.get();
		
		for (int j = 0; j < lpd.size(); j++) {
			Triplet<String, ByteBuffer, ImageData> data = lpd.get(j);
			
			Image img = loadImageFromData(data);
			
			if (img != null) {
				
				posters.put(data.x, img);
				lpd.remove(j); j--;
				
				loadedPosterData.set(lpd);
				
				//return;
			}
			
		}
		
		for (int i = 0, size = entries.size(); i < size; i++) {
			MangaInfo info = entries.get(i);
			
			if (info == null || info.title == null) { continue; }
			
			if (info == null || info.notdone) {
				
				File notdone = new File(metadir.getAbsolutePath()+"/"+info.title+"/_metadata/notdone");
				if (notdone.exists()) { continue; }
				
				File file = new File(metadir.getAbsolutePath()+"/"+info.title+"/_metadata/info.xml");
				MangaInfo nfo = null;
				try { nfo = MangaInfo.load(file); } catch (Exception | Error e) {}
				
				if (nfo != null) {
					
					nfo.notdone = false;
					
					entries.set(i, nfo);
					break;
				}
				
			}
			
		}
		
	}
	
	@Override
	public void renderEntry(Graphics g, MangaInfo entry, float x, float y, boolean selected, int ind) {
		
		if (entry.read) {
			
			float x1 = pane.x + pane.width - (20f + GUIRes.reading.getWidth() + 8f) * displayScale;
			float y1 = y + (entryHeight - GUIRes.reading.getHeight() * displayScale) * 0.5f;
			
			GUIRes.tick.draw((int)x1, (int)y1, (int)(GUIRes.tick.getWidth() * displayScale + 0.5f), 
					 		 (int)(GUIRes.tick.getHeight() * displayScale + 0.5f), Color.white.copy().setAlpha(0.8f));
			
		} else if (entry.lastChapter > 0) {
			
			float x1 = pane.x + pane.width - (20f + GUIRes.tick.getWidth() + 8f) * displayScale;
			float y1 = y + (entryHeight - GUIRes.tick.getHeight() * displayScale) * 0.5f;
			
			GUIRes.reading.draw((int)x1, (int)y1, (int)(GUIRes.reading.getWidth() * displayScale + 0.5f), 
					 			(int)(GUIRes.reading.getHeight() * displayScale + 0.5f), Color.white.copy().setAlpha(0.8f));
			
		}
		
		super.renderEntry(g, entry, x, y, selected, ind);
	}
	
	
	@Override
	public void clear() {
		
		clear(null);
	}
	
	public void clear(String ignoreTitle) {
		super.clear();
		
		if (infosThread != null && infosThread.isAlive()) {
			
			infosThread.interrupt();
		}
		
		if (imgThread != null && imgThread.isAlive()) {
			
			imgThread.interrupt();
		}
		
		for (String key : posters.keySet()) {
			Image img = posters.get(key);
			if (img == null || img.isDestroyed()) { continue; }
			if (ignoreTitle != null && key.equalsIgnoreCase(ignoreTitle)) { continue; }
			
			try { img.destroy(); } catch (Exception e) { e.printStackTrace(); }
		}
		
	}
	
	
	public static Vector2f getEntrySize(int mode) {
		
		if (mode == MangaList.MODE_VERTICAL) {
			
			float f = 52f * displayScale;
			
			return new Vector2f(f, f);
		}
		
		if (mode == MangaList.MODE_HORIZONTAL) {
			
			float f = Mangas.POSTER_WIDTH * 0.75f * displayScale;
			
			return new Vector2f(f, f);
		}
		
		if (mode == MangaList.MODE_TABLE) {
			
			return new Vector2f(450f * displayScale, 260f * displayScale);
		}
		
		return new Vector2f(0f, 0f);
	}
	
	
	public void setFocus(String title) {
		
		for (int i = 0; i < entries.size(); i++) {
			
			if (entries.get(i).title.trim().toLowerCase().equals(title.trim().toLowerCase())) {
				
				setFocus(i);
				return;
			}
		}
		
	}
	
	
	public void checkForPosterChange() {
		
		for (String key : posters.keySet()) {
			Image poster = posters.get(key);
			MangaInfo info = Mangas.get(key);
			
			if (info != null && poster != null && !info.poster.equals(poster.getName())) {
				
				Image img = null;
				try {
					
					img = new Image(Settings.metaIn+"/"+key+"/_metadata/posters/"+info.poster, Image.FILTER_LINEAR);
					img.setName(info.poster);
					
				} catch (Exception e) { e.printStackTrace(); }
				
				if (img != null) {
					
					posters.put(key, img);
				}
			}
			
		}
		
	}
	
	
	private Image loadImageFromData(Triplet<String, ByteBuffer, ImageData> data) {
		
		for (int i = 0, size = entries.size(); i < size; i++) {
			MangaInfo info = entries.get(i);
			
			if (info == null || info.title == null || posters.containsKey(info.title)) { continue; }
			
			if (info.title.equals(data.x)) {
				
				Image img = null;
				
				try {
					
					img = new Image(InternalTextureLoader.get().getTexture(data.y, data.z, metadir.getAbsolutePath()+"/"+info.title+"/_metadata/posters/01.jpg", 
									GL11.GL_TEXTURE_2D, Image.FILTER_LINEAR, Image.FILTER_LINEAR, false, null));
					
					img.setName(info.poster);
					
				} catch (Exception e) { e.printStackTrace(); }
				
				return img;
			}
			
		}
		
		return null;
	}
	
	
	private class InfoLoadThread extends Thread {

		private HashSet<String> ignores;
		
		private File metadir;
		private Predicate<File> dirfilter;
		
		public InfoLoadThread(File metadir, Predicate<File> dirfilter) {
			
			this(metadir, dirfilter, new HashSet<String>());
		}
		
		public InfoLoadThread(File metadir, Predicate<File> dirfilter, HashSet<String> ignores) {
			
			this.metadir = new File(metadir.getAbsolutePath());
			this.dirfilter = dirfilter;
			
			this.ignores = ignores;
		}
		
		@Override
		public void run() {
			
			List<File> dirs = Files.getFiles(metadir, f -> f.isDirectory() && !f.getName().startsWith("_") &&
													  !ignores.contains(f.getName().trim().toLowerCase()) && (dirfilter == null || dirfilter.test(f)));
			
			for (int i = 0; i < dirs.size() && !interrupted(); i++) {
				File dir = dirs.get(i);
				
				File xml = new File(dir.getAbsolutePath()+"/_metadata/info.xml");
				if (!xml.exists()) { continue; }
				
				Files.waitOnFile(xml, 1);
				
				MangaInfo info = null;
				
				try {

					info = MangaInfo.load(xml);
					
				} catch (Exception e) {}
				
				if (info != null) {
					
					if (new File(dir.getAbsolutePath()+"/_metadata/notdone").exists()) { info.notdone = true; }
					
					List<MangaInfo> buffer = entryBuffer.get();
					buffer.add(info);
					
					entryBuffer.set(buffer);
				}
				
			}
			
		}
		
	}
	
	
	public void sortEntries() {
		
		sortEntries(SORT_ALPHABETICALLY);
		
		if (sortByRank) {
			
			Collections.sort(entries, (mi0, mi1) -> Integer.compare(mi0.poprank, mi1.poprank));
		}
	}
	
	public void sortEntries(int mode) {
		
		sortInfos(this.entries, mode);
	}
	
	public static void sortInfos(List<MangaInfo> entries, int mode) {
		
		// Sort manga alphabetically anyhow for other values can be equal
		
		Collections.sort(entries, (m0, m1) -> m0.title.toLowerCase().replaceAll("^the ", "")
												.compareTo(m1.title.toLowerCase().replaceAll("^the ", "")));
		
		if (mode == SORT_RECENTLY_READ) {
			
			Collections.sort(entries, (m0, m1) -> -Long.compare(m0.lastReadMillis, m1.lastReadMillis));
			
		} else if (mode == SORT_RECENTLY_UPDATED) {
			
			Collections.sort(entries, (m0, m1) -> -Long.compare(m0.recentChapterMillis, m1.recentChapterMillis));
			
		} else if (mode == SORT_AUTHOR) {
			
			Collections.sort(entries, (m0, m1) -> m0.author.toLowerCase().trim().compareTo(m1.author.toLowerCase().trim()));
		}
		
	}
	
	
	private class ImageLoadThread extends Thread {
		
		
		private static final int DEFAULT_RATE = 15, RATE_MIN = 500, RATE_DECREASE = 25;
		
		
		private File metadir;
		//private List<Tuple<String, String>> p2l;
		
		private int rate;
		
		
		public ImageLoadThread(File metadir/*, List<Tuple<String, String>> postersToLoad*/) {
			
			this.setDaemon(true);
			
			this.metadir = new File(metadir.getAbsolutePath());
			//this.p2l = postersToLoad;
			
			this.rate = DEFAULT_RATE;//(int)(1000f / (float)Main.display.getTargetFrameRate() + 0.5f);
		}
		
		
		@Override
		public void run() {
			
			/*
			while (!interrupted() && !p2l.isEmpty()) {
				
				long millis = System.currentTimeMillis();
				
				for (int i = 0, size = p2l.size(); i < size; i++) {
					String title = p2l.get(i).x;
					String image = p2l.get(i).y;
					
					if (loadImageData(title, image)) {
						
						p2l.remove(i);
						break;
					}
				}
				
				try { sleep(Math.max(rate - (System.currentTimeMillis() - millis), 0)); } catch (InterruptedException e) { return; }
			}
			*/
			
			for (List<Tuple<String, String>> toload = postersToLoad.get(); !interrupted(); toload = postersToLoad.get()) {
				
				long millis = System.currentTimeMillis();
				
				if (toload != null && !toload.isEmpty()) {
					
					rate = DEFAULT_RATE;
					
					for (int i = 0, size = toload.size(); i < size; i++) {
						String title = toload.get(i).x;
						String image = toload.get(i).y;
						
						if (loadImageData(title, image)) {
							
							toload.remove(i);
							break;
						}
					}
					
					postersToLoad.set(toload);
					
				} else {
					
					rate = Math.min(rate + RATE_DECREASE, RATE_MIN);
				}
				
				try { sleep(Math.max(rate - (System.currentTimeMillis() - millis), 0)); } catch (InterruptedException e) { return; }
			}
			
		}
		
		private boolean loadImageData(String title, String imageName) {
			
			if (imageName == null || imageName.trim().length() < 5) {
				
				imageName = "01.jpg";
			}
			
			File notdone = new File(metadir.getAbsolutePath()+"/"+title+"/_metadata/posters/notdone");
			if (notdone.exists()) { return false; }
		
			File imgF = new File(metadir.getAbsolutePath()+"/"+title+"/_metadata/posters/"+imageName);
			if (!imgF.exists()) { return false; }
			
			LoadableImageData imageData = null;
			ByteBuffer imageBuffer = null;
			try {
				
				FileInputStream fis = new FileInputStream(imgF);
				BufferedInputStream bufin = new BufferedInputStream(fis);
				
				imageData = ImageDataFactory.getImageDataFor(imgF.getAbsolutePath());
				imageBuffer = imageData.loadImage(bufin, false, null);
				
				bufin.close();
				fis.close();
				
			} catch (Exception e) { e.printStackTrace(); }
			
			if (imageData != null && imageBuffer != null) {
				
				Triplet<String, ByteBuffer, ImageData> data = new Triplet<String, ByteBuffer, ImageData>(title, imageBuffer, imageData);
				
				List<Triplet<String, ByteBuffer, ImageData>> lpd = loadedPosterData.get();
				lpd.add(data);
				loadedPosterData.set(lpd);
				
				return true;
			}
			
			return false;
		}
		
	}
	
}
