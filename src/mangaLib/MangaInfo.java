package mangaLib;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import visionCore.math.FastMath;
import visionCore.util.Files;
import visionCore.util.StringUtils;
import visionCore.util.Web;

public class MangaInfo {

	// not to be saved
	public File directory;
	private Thread saveThread;
	public boolean read, notdone;
	
	// to be saved
	public String title, author, artist, status, url, synopsis;
	
	public List<String> genres;
	
	public int released, lastPage, lockedWidth, poprank, mal_id;
	public double lastChapter;
	
	public long lastReadMillis, recentChapterMillis;
	
	public HashSet<Double> readChapters;
	
	public String poster;
	
	
	public MangaInfo() {
		
		title = "unknown";
		author = "unknown";
		artist = "unknown";
		status = "unknown";
		synopsis = "n/a";
		url = "";
		genres = new ArrayList<String>();
		released = -1;
		
		lastChapter = -1;
		lastPage = -1;
		readChapters = new HashSet<Double>();
		
		lockedWidth = -1;
		
		poprank = -1;
		mal_id = -1;
		
		poster = "01.jpg";
		
		read = false;
		notdone = false;
		
		lastReadMillis = 0;
		recentChapterMillis = 0;
		
	}
	
	public static MangaInfo load(File f) {
		
		return load(f, false);
	}
	
	public static MangaInfo load(File f, boolean printErr) {
		
		MangaInfo info = new MangaInfo();
		
		File lock = new File(f.getParentFile().getAbsolutePath()+"/INFO_LOCK");
		for (long m = 0, w = 50; m < 3000 && lock.exists(); m += w) {
			
			try { Thread.sleep(w); } catch (Exception e) {}
		}
		
		SAXReader reader = new SAXReader();
		Document doc = null;
		try { doc = reader.read(f); }
		catch (DocumentException e) {
			
			if (printErr) {
				System.out.println("Failed at reading \""+f.getAbsolutePath().replace("\\", "/")+"\""); 
				System.out.println(); e.printStackTrace();
			}
			return null;
		}
		
		Element root = doc.getRootElement();
		
		if (root == null) { return null; }
		
		try { String s = cleanTitle(root.element("title").getText().trim()); info.title = s; } catch (Exception e) {}
		try { String s = info.artist = root.element("artist").getText().trim(); info.artist = s; } catch (Exception e) {}
		try { String s = root.element("author").getText().trim(); info.author = s; } catch (Exception e) {}
		try { String s = root.element("status").getText().trim(); info.status = s; } catch (Exception e) {}
		try { String s = root.element("url").getText().trim(); info.url = s; } catch (Exception e) {}
		try { String s = cleanSynopsis(root.element("synopsis").getText().trim(), false); info.synopsis = s; } catch (Exception e) {}
		
		try { int i = Integer.parseInt(root.element("released").getText().trim()); info.released = i; } catch (Exception e) {}
		try { int i = Integer.parseInt(root.element("lastPage").getText().trim()); info.lastPage = i; } catch (Exception e) {}
		
		try { double d = Double.parseDouble(root.element("lastChapter").getText().trim()); info.lastChapter = d; } catch (Exception e) {}
		
		try { int i = Integer.parseInt(root.element("lockedWidth").getText().trim()); info.lockedWidth = i; } catch (Exception e) {}
		try { int i = Integer.parseInt(root.element("poprank").getText().trim()); info.poprank = i; } catch (Exception e) {}
		try { int i = Integer.parseInt(root.element("mal_id").getText().trim()); info.mal_id = i; } catch (Exception e) {}
		
		try { long l = Long.parseLong(root.element("lastReadMillis").getText().trim()); info.lastReadMillis = l; } catch (Exception e) {}
		try { long l = Long.parseLong(root.element("recentChapterMillis").getText().trim()); info.recentChapterMillis = l; } catch (Exception e) {}
		
		Element readchaps = root.element("readChapters");
		
		if (readchaps != null) {
		
			if (info.readChapters == null) { info.readChapters = new HashSet<Double>(); }
			
			String s = readchaps.getText();
			String[] nmbrs = s.split(",");
			
			for (String n : nmbrs) {
				
				try {
					
					info.readChapters.add(Double.parseDouble(n.trim()));
					
				} catch (Exception e) {}
				
			}
			
		}
		
		Element genres = root.element("genres");
		
		if (genres != null) {
		
			if (info.genres == null) { info.genres = new ArrayList<String>(); }
			
			for (Iterator<Element> it = genres.elementIterator(); it.hasNext();) {
				Element el = it.next();
				
				if (el != null && el.getText() != null) {
				
					info.genres.add(el.getText().trim());
				}
				
			}
			
		}
		
		Element elem = root.element("poster");
		if (elem != null && elem.getText() != null && elem.getText().trim() != null) { info.poster = elem.getText().trim(); }
		
		if (info.poster == null || info.poster.trim().equals("")) { info.poster = "01.jpg"; }
		
		info.directory = f.getParentFile();
		if (info.directory.getName().toLowerCase().trim().startsWith("_meta")) { info.directory = info.directory.getParentFile(); }
		
		if (info.status != null && !info.status.equals("unknown")) { info.status = StringUtils.ucFirst(info.status.toLowerCase()); }
		
		if (info.synopsis == null) { info.synopsis = "n/a"; }
		if (info.artist == null) { info.artist = "unknown"; }
		if (info.author == null) { info.author = "unknown"; } else if (info.author.toLowerCase().equals("one")) { info.author = "ONE"; }
		
		info.title = cleanTitle(info.title);
		
		return info;
	}
	
	public void save(File f) {
		
		if (status != null && !status.equals("unknown")) { status = StringUtils.ucFirst(status.toLowerCase()); }
		
		title = cleanTitle(title);
		
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");
		
		
		root.addElement("title").setText(title);
		root.addElement("artist").setText(artist);
		root.addElement("author").setText(author);
		root.addElement("status").setText(status);
		root.addElement("url").setText(url.trim());
		root.addElement("released").setText(released+"");
		root.addElement("synopsis").setText(synopsis.trim());
		
		Element genres = root.addElement("genres");
		
		for (String g : this.genres) {
			
			genres.addElement("genre").setText(g);
		}
		
		root.addElement("poster").setText(poster.trim());
		
		root.addElement("lastPage").setText(lastPage+"");
		root.addElement("lastChapter").setText(lastChapter+"");
		
		root.addElement("mal_id").setText(mal_id+"");
		root.addElement("poprank").setText(poprank+"");
		root.addElement("lockedWidth").setText(lockedWidth+"");
		
		root.addElement("lastReadMillis").setText(lastReadMillis+"");
		root.addElement("recentChapterMillis").setText(recentChapterMillis+"");
		
		Element readchaps = root.addElement("readChapters");
		
		//List<Double> readChapsTmp = new ArrayList<Double>(this.readChapters);
		//Collections.sort(readChapsTmp);
		
		StringBuilder sb = new StringBuilder();
		
		for (double d : this.readChapters) { sb.append(((d == Math.floor(d)) ? Integer.toString((int)d) : Double.toString(d))+", "); }
		String s = sb.toString();
		
		if (s.length() > 2) { s = s.substring(0, s.length()-2); }
		
		readchaps.setText(s);
		
		if (saveThread == null || !saveThread.isAlive()) {
			
			saveThread = new Thread(){
				
				@Override
				public void run() {
					
					OutputFormat format = OutputFormat.createPrettyPrint();
					format.setIndent("\t");
					
					try {
					
						File lock = new File(f.getParentFile().getAbsolutePath()+"/INFO_LOCK");
						for (long m = 0, w = 50; m < 3000 && lock.exists(); m += w) {
							
							try { Thread.sleep(w); } catch (Exception e) {}
						}
						
						if (!f.exists()) { f.getParentFile().mkdirs(); }
						
						lock.createNewFile();
						lock.deleteOnExit();
						
						XMLWriter writer = new XMLWriter(new FileWriter(f), format);
						writer.write(doc);
						writer.close();
						
						lock.delete();
						
					} catch (Exception e) { e.printStackTrace(); }
					
				}
				
			};
			saveThread.start();
			
		} else {
			
			Thread t1 = new Thread(){
				
				@Override
				public void run() {
					
					for (long m = 0, w = 200; saveThread.isAlive() && m < 5000; m += w) { 
						
						try { Thread.sleep(w); } catch (Exception e) {}
					}
					
					saveThread = new Thread(){
						
						@Override
						public void run() {
							
							OutputFormat format = OutputFormat.createPrettyPrint();
							format.setIndent("\t");
							
							try {
							
								File lock = new File(f.getParentFile().getAbsolutePath()+"/INFO_LOCK");
								for (long m = 0, w = 50; m < 3000 && lock.exists(); m += w) {
									
									try { Thread.sleep(w); } catch (Exception e) {}
								}
								
								if (!f.exists()) { f.getParentFile().mkdirs(); }
								
								lock.createNewFile();
								lock.deleteOnExit();
								
								XMLWriter writer = new XMLWriter(new FileWriter(f), format);
								writer.write(doc);
								writer.close();
								
								lock.delete();
								
							} catch (Exception e) { e.printStackTrace(); }
							
						}
						
					};
					saveThread.start();
					
				}
				
			};
			
			t1.start();
			
		}
		
	}
	
	public static String cleanTitle(String title) {

		title = title.replace("/", "");
		title = title.replace("\\", "");
		title = title.replace("\"", "");
		title = title.replace("*", "");
		title = title.replace("?", "");
		title = title.replace("<", "[");
		title = title.replace(">", "]");
		title = title.replace("|", "-");
		title = title.replace(":", " -");
		title = title.replaceAll("[^ -~]", "");
		
		return title;
	}
	
	public static String cleanSynopsis(String synopsis) {
		
		return cleanSynopsis(synopsis, true);
	}
	
	public static String cleanSynopsis(String synopsis, boolean textCorrection) {
		
		synopsis = Web.clean(synopsis);
		synopsis = synopsis.replace("\"", "");
		synopsis = synopsis.replaceAll("[^ -~]", "");
		
		if (textCorrection) {
		
			for (int i = 0, ind = 0; i < 100 && (ind = synopsis.indexOf("http://")) > -1;) {
				
				String s = synopsis.substring(0, ind);
				String s1 = synopsis.substring(ind);
				
				if (s1.contains(" ")) {
					
					synopsis = s + s1.substring(FastMath.clampToRangeC(s1.indexOf(" ")+1, 0, s1.length()-1));
					
				} else { synopsis = s; }
				
			}
			
			for (int i = 0, l = synopsis.length(); i < l-1; i++) {
				
				char c0 = synopsis.charAt(i);
				char c1 = synopsis.charAt(i+1);
				
				if ((c0 == '.' || c0 == ',' || c0 == '!' || c0 == '?' || c0 == ';' || c0 == ':') && Character.isAlphabetic(c1)) {
					
					synopsis = synopsis.substring(0, i+1)+" "+synopsis.substring(i+1);
					i++;
					l++;
				}
				
			}
			
		}
		
		return synopsis;
	}
	
	
	public MangaInfo copy() {
		
		MangaInfo c = new MangaInfo();
		
		c.artist = this.artist;
		c.author = this.author;
		c.directory = new File(this.directory.getAbsolutePath());
		c.genres = new ArrayList<String>(); c.genres.addAll(this.genres);
		c.lastChapter = this.lastChapter;
		c.lastPage = this.lastPage;
		c.lastReadMillis = this.lastReadMillis;
		c.lockedWidth = this.lockedWidth;
		c.notdone = this.notdone;
		c.poprank = this.poprank;
		c.poster = this.poster;
		c.read = this.read;
		c.readChapters = new HashSet<Double>(); c.readChapters.addAll(this.readChapters);
		c.recentChapterMillis = this.recentChapterMillis;
		c.released = this.released;
		c.status = this.status;
		c.synopsis = this.synopsis;
		c.title = this.title;
		c.url = this.url;
		
		return c;
	}
	
}
