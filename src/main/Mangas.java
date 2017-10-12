package main;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageStruct;

import mangaLib.MangaInfo;
import visionCore.util.Files;

public class Mangas {
	
	
	public static final float POSTER_WIDTH = 256, POSTER_HEIGHT = 398;
	
	
	public static AtomicReference<HashMap<String, MangaInfo>> mangas;
	private static HashMap<String, Image> thumbs;
	
	public static HashSet<String> knownLockedWidths;
	public static Thread saveThread;
	
	public static String lastRead;
	
	
	public static void load() {
		
		System.out.print("Loading manga info..");
		
		lastRead = "";
		
		fixMetaData();
		
		reload();
		
		loadGlobalMeta();
		loadKnownLockedWidths();
		
		System.out.println(" done.");
	}
	
	
	public static MangaInfo get(String title) {
		
		return mangas.get().get(title.toLowerCase());
	}
	
	public static void reload() {
		
		if (mangas == null) { mangas = new AtomicReference<HashMap<String, MangaInfo>>(); }
		
		HashMap<String, MangaInfo> mangasMap = new HashMap<String, MangaInfo>();
		
		File metadir = new File(Settings.metaIn);
		File mangadir = new File(Settings.mangaDir);
		
		for (File f : metadir.listFiles()) {
			if (!f.isDirectory() || f.getName().startsWith("_")) { continue; }
			
			String title = f.getName();
			
			File metaf = new File(f.getAbsolutePath()+"/_metadata/info.xml");
			if (!metaf.exists()) { continue; }
			
			MangaInfo info = MangaInfo.load(metaf, true);
			if (info == null) { continue; }
			
			File mangaf = new File(mangadir.getAbsolutePath()+"/"+info.title);
			if (!mangaf.exists()) { continue; }
			
			mangasMap.put(title.toLowerCase(), info);
			
		}
		
		mangas.set(mangasMap);
		
		thumbs = new HashMap<String, Image>();
		loadThumbs();
		
	}
	
	public static void loadThumbs() {
		
		File metadir = new File(Settings.metaIn);
		
		for (File f : metadir.listFiles()) {
			if (!f.isDirectory() || f.getName().startsWith("_")) { continue; }
			
			String title = f.getName();
			
			String posterName = "null";
			MangaInfo info = get(title);
			if (info != null && info.poster != null) {

				posterName = info.poster;
			}
			
			File thumb = new File(metadir.getAbsolutePath()+"/"+title+"/_metadata/posters/thumbs/"+posterName);
			
			if (!thumb.exists()) {
				
				String n = posterName.substring(0, Math.max(posterName.lastIndexOf("."), 0));
				thumb = new File(Settings.metaIn+"/"+title+"/_metadata/posters/thumbs/"+n+".png");
			}
			
			if (thumb.exists()) {
				
				Image img = null;
				try {
					
					ImageStruct struct = TJUtil.getImageStruct(thumb);
					img = new Image(struct);
					img.setName(thumb.getName().substring(0, thumb.getName().lastIndexOf(".")));
				} catch (Exception e) {}
				
				if (img != null) {
					
					thumbs.put(title.toLowerCase(), img);
				}
				
			}
			
		}
		
	}
	
	public static void posterChange() {
		
		HashMap<String, MangaInfo> mangas = Mangas.mangas.get();
		if (mangas == null) { return; }
		
		for (String key : mangas.keySet()) {
			MangaInfo info = mangas.get(key);
			
			Image thumb = thumbs.get(key);
			
			if (thumb != null && !info.poster.equalsIgnoreCase(thumb.getName())) {
				
				File thumbf = new File(Settings.metaIn+"/"+info.title+"/_metadata/posters/thumbs/"+info.poster);
				
				if (!thumbf.exists()) {
					
					String n = info.poster.substring(0, Math.max(info.poster.lastIndexOf("."), 0));
					thumbf = new File(Settings.metaIn+"/"+info.title+"/_metadata/posters/thumbs/"+n+".png");
				}
				
				if (thumbf.exists()) {
					
					Image img = null;
					try {
						
						img = new Image(TJUtil.getImageStruct(thumbf));
						img.setName(thumbf.getName());
					} catch (Exception e) {}
					
					if (img != null) {
						
						thumbs.put(info.title.toLowerCase(), img);
					}
					
				}
				
			}
			
		}
		
	}
	
	public static Image getThumb(String title) {
		
		return thumbs.get(title.toLowerCase());
	}
	
	
	public static void fixMetaData() {
		
		if (Settings.metaIn.length() < 2 || Settings.metaIn.equals(Settings.mangaDir)) { return; }
		
		File metadir = new File(Settings.metaIn);
		File mangadir = new File(Settings.mangaDir);
		
		if (!metadir.exists() || !mangadir.exists() || mangadir.listFiles() == null) { return; }
		
		for (File dir : mangadir.listFiles()) {
			if (!dir.isDirectory() || dir.getName().startsWith("_")) { continue; }
			
			List<File> matches = Files.getFiles(metadir, f -> f.isDirectory() && f.getName().trim().toLowerCase().equals(dir.getName().trim().toLowerCase()));
			
			if (matches.isEmpty()) {
				
				File newdir = new File(Settings.metaIn+"/"+dir.getName().trim());
				newdir.mkdirs();
				
				File mangameta = new File(dir.getAbsolutePath()+"/_metadata");
				if (!mangameta.exists()) { continue; }
				
				List<File> metafiles = Files.getFilesRecursive(mangameta.getAbsolutePath(), f -> !f.isDirectory());
				
				for (File f : metafiles) {
					
					try {
					
						String p = f.getAbsolutePath();
						p = p.substring(Settings.mangaDir.length());
						
						File nf = new File(Settings.metaIn+p);
						nf.getParentFile().mkdirs();
						
						Files.copyFileUsingOS(f, nf);
						
					} catch (Exception e) { e.printStackTrace(); }
					
				}
				
			}
			
		}
		
	}
	
	
	public static void loadGlobalMeta() {
		
		File xml = new File(Settings.metaIn+"/_metadata/global.xml");
		if (!xml.exists()) { xml = new File(Settings.mangaDir+"/_metadata/global.xml"); }
		if (!xml.exists()) { System.out.println("No global-meta found."); return; }
		
		SAXReader reader = new SAXReader();
		Document doc = null;
		try { doc = reader.read(xml); }
		catch (DocumentException e) { System.out.println("Failed at reading \""+xml.getAbsolutePath().replace("\\", "/")+"\""); return; }
		
		Element root = doc.getRootElement();
		
		try { lastRead = root.element("lastread").getText().trim(); } catch (Exception e) {}
		
	}
	
	public static void saveGlobalMeta() {
		
		lastRead = lastRead.trim();
		
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");
		
		
		root.addElement("lastread").setText(lastRead);
		
		
		Thread t = new Thread(){
			
			@Override
			public void run() {
				
				OutputFormat format = OutputFormat.createPrettyPrint();
				format.setIndent("\t");
				
				File out = new File(Settings.metaIn+"/_metadata/global.xml");
				if (!out.getParentFile().exists()) { out.getParentFile().mkdirs(); }
				
				try {
				
					XMLWriter writer = new XMLWriter(new FileWriter(out), format);
					writer.write(doc);
					writer.close();
					
				} catch (Exception e) { e.printStackTrace(); }
				
				if (Settings.metaIn.equals(Settings.mangaDir)) { return; }
				
				out = new File(Settings.mangaDir+"/_metadata/global.xml");
				if (!out.getParentFile().exists()) { out.getParentFile().mkdirs(); }
				
				try {
				
					XMLWriter writer = new XMLWriter(new FileWriter(out), format);
					writer.write(doc);
					writer.close();
					
				} catch (Exception e) { e.printStackTrace(); }
				
			}
			
		};
		
		if (saveThread == null || !saveThread.isAlive()) {
			
			saveThread = t;
			saveThread.start();
			
		} else {
			
			Thread t1 = new Thread(){
				
				@Override
				public void run() {
					
					for (long m = 0, w = 200; saveThread.isAlive() && m < 5000; m += w) { 
						
						try { Thread.sleep(w); } catch (Exception e) {}
					}
					
					saveThread = t;
					saveThread.start();
					
				}
				
			};
			
			t1.start();
			
		}
		
	}
	
	public static void loadKnownLockedWidths() {
		
		knownLockedWidths = new HashSet<String>();
		
		knownLockedWidths.add("tower of god");
		knownLockedWidths.add("relife");
		knownLockedWidths.add("the gamer");
		
	}
	
	public static boolean knownLockedWidth(String title) {
		
		return knownLockedWidths.contains(title.toLowerCase().replace("\"", "").trim());
	}

}
