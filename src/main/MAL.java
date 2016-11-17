package main;

import static main.Main.displayScale;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.util.Web;

public class MAL extends mangaLib.MAL {

	public static Image avatar;
	public final static int AVATAR_SIZE = 60;
	
	
	public static void load() {
		
		if (!hasUser()) { return; }
		
		loadAvatar();
		
	}
	
	public static void loadAvatar() {
		
		if (!loadAvatarImage()) {
			
			File pic = new File(Main.abspath+"/res/mal/avatars/"+Settings.MAL_userName.toLowerCase().trim()+".png");
			File lock = new File(pic.getParentFile().getAbsolutePath()+"/LOCK");
			
			if (!pic.getParentFile().exists()) { pic.getParentFile().mkdirs(); }
			
			new Thread() {
				
				@Override
				public void run() {
					
					BufferedImage img = MAL.getProfilePic(Settings.MAL_userName.trim());
					if (img == null) { return; }
					
					BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
					resized.getGraphics().drawImage(img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH), 0, 0, 64, 64, null);
					
					if (!lock.exists()) { try { lock.createNewFile(); } catch (Exception e) {} }
					
					lock.deleteOnExit();
					
					if (resized != null) {
					
						try {
							
							ImageIO.write(resized, "PNG", pic);
							
						} catch (IOException e) { e.printStackTrace(); }
						
					}
					
					lock.delete();
					
				}
				
			}.start();
			
		}
		
	}
	
	public static boolean loadAvatarImage() {
		
		File pic = new File(Main.abspath+"/res/mal/avatars/"+Settings.MAL_userName.toLowerCase().trim()+".png");
		File lock = new File(pic.getParentFile().getAbsolutePath()+"/LOCK");
		
		if (pic.exists() && !lock.exists()) {
			
			try {
				
				avatar = new Image(pic.getAbsolutePath(), Image.FILTER_LINEAR);
				avatar.setName(pic.getName().substring(0, pic.getName().indexOf(".png")));
				
				return true;
				
			} catch (Exception e) { e.printStackTrace(); }
			
		}
		
		return false;
	}
	
	public static void renderProfile(Graphics g, int corner) {
		
		float x = 0f, y = 0f;
		
		Font sFont = Fonts.roboto.s18;
		Font uFont = Fonts.roboto.s24;
		
		String s = "MAL:";
		String u = Settings.MAL_userName;
		
		float txtWidth = Math.max(sFont.getWidth(s), uFont.getWidth(u));
		
		float width = AVATAR_SIZE * displayScale + 10f * displayScale + txtWidth;
		
		if (corner == 0 || corner == 2) { x = 20f * displayScale; } 
		else if (corner == 1 || corner == 3) { x = Display.getWidth() - 20f * displayScale - width; }
		
		if (corner == 0 || corner == 1) { y = 20f * displayScale; }
		else if (corner == 2 || corner == 3) { y = Display.getHeight() - (AVATAR_SIZE + 20f) * displayScale; }
		
		if (avatar != null && !avatar.isDestroyed()) {
			
			avatar.draw((int)(x), (int)(y), (int)(AVATAR_SIZE * displayScale + 0.5f), (int)(AVATAR_SIZE * displayScale + 0.5f));
			
		} else {
			
			GUIRes.blankAvatar.draw((int)(x), (int)(y), (int)(AVATAR_SIZE * displayScale + 0.5f), (int)(AVATAR_SIZE * displayScale + 0.5f));
		}
		
		g.setColor(Color.grayTone(136));
		g.drawRect((int)(x), (int)(y), (int)(AVATAR_SIZE * displayScale + 0.5f), (int)(AVATAR_SIZE * displayScale + 0.5f));
		
		x += (AVATAR_SIZE + 10f) * displayScale;
		y += (AVATAR_SIZE * displayScale - sFont.getHeight(s)) - 5f * displayScale;
		
		g.setColor(Color.white);
		g.setFont(uFont);
		g.drawStringShadow(u, (int)(x), (int)(y), Math.max(1, (int)(2f * displayScale + 0.5f)), 0.1f);
		
		y -= uFont.getHeight(u) - 10f * displayScale;
		
		g.setFont(sFont);
		g.drawStringShadow(s, (int)(x), (int)(y), Math.max(1, (int)(2f * displayScale + 0.5f)), 0.1f);
		
	}
	
	
	public static int getID(MangaInfo info) {
		
		int id = -1;
		
		if (info.mal_id != -1) {
			
			id = info.mal_id;
			
		} else {
			
			id = getID(info.title);
			info.mal_id = id;
			
			if (id == -1) { System.out.println("MAL: \""+info.title+"\" not found."); return -1; }
			
			if (info.directory != null && info.directory.getAbsolutePath().startsWith(Settings.metaIn)) {
				
				try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
				try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
				
			} else if (info.directory != null) {
				
				try { info.save(new File(info.directory.getAbsolutePath()+"/_metadata/info.xml")); } catch (Exception e) {}
			}
			
		}
		
		return id;
	}
	
	public static void updateInList(MangaInfo info) {
		
		int id = getID(info);
		if (id < 0) { return; }
		
		//String method = "update";
		
		String method = "add";
		
		List<MALEntry> userList = getMangaList(Settings.MAL_userName);
		for (MALEntry entry : userList) {
			
			if (entry.id == id) { method = "update"; break; }
		}
		
		double chapterMax = 0;
		try { chapterMax = info.readChapters.stream().max((i0, i1) -> Double.compare(i0, i1)).get(); } catch (Exception e) { }
		
		int status = STATUS_READING;
		if (info.read) { status = STATUS_FINISHED; }
		else if (info.readChapters.isEmpty()) { status = STATUS_PLAN_TO_READ; }
		
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("entry");
		
		root.addElement("chapter").setText(((int)chapterMax)+"");
		root.addElement("status").setText(status+"");
		
		editList(id, method, doc);
		
	}
	
	public static void putOnHold(MangaInfo info) {
		
		int id = getID(info);
		if (id < 0) { return; }
		
		String method = "update";
		int status = STATUS_ON_HOLD;
		
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("entry");
		
		root.addElement("status").setText(status+"");
		
		editList(id, method, doc);
		
	}
	
	public static void editList(int id, String method, Document xmlDoc) {
		
		curltry: try {
			
			File curl = new File(Main.abspath+"/res/bin/curl/"+((Main.linux) ? "linux/curl.ermine" : "win32/curl.exe"));
			if (!curl.exists()) { System.out.println("\""+curl.getAbsolutePath()+"\" not found"); break curltry; }
			
			File dir = curl.getParentFile();
			
			List<String> cmdAndArgs = new ArrayList<String>();
			
			if (!Main.linux) {
				
				cmdAndArgs.add("\""+curl.getAbsolutePath()+"\"");
				
			} else { 
				
				cmdAndArgs.add(curl.getAbsolutePath());
			}
			
			cmdAndArgs.add("-u");
			cmdAndArgs.add(Settings.MAL_userName+":"+Settings.MAL_password);
			
			String xml = xmlDoc.asXML().replace("\\", "").replaceAll("[^ -~]", "").replace("\"", "'");
			xml = URLEncoder.encode(xml, "UTF-8");
			
			//System.out.println(xml);
			
			cmdAndArgs.add("-d");
			cmdAndArgs.add("data=\""+xml+"\"");
			cmdAndArgs.add("http://myanimelist.net/api/mangalist/"+method+"/"+id+".xml");
			
			//for (String s : cmdAndArgs) { System.out.print(s+" "); } System.out.print("\n");
			
			ProcessBuilder pb = new ProcessBuilder(cmdAndArgs);
			//pb.directory(dir);
			//pb.redirectErrorStream(true); //dont set to true or mari wont work...
			Process p = pb.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			for (String line = "", cur = ""; (line = reader.readLine()) != null;) {
				line = line.trim();

				System.out.println("MAL: "+line);
				
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
		/*
		try {
			
			HttpURLConnection connection = (HttpURLConnection) new URL("http://myanimelist.net/api/mangalist/update/"+id+".xml").openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			
			String userPassword = Settings.MAL_userName + ":" + Settings.MAL_password;
			String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + encoding);
			
			String xml = doc.asXML().replace("\n", "").replace("\r", "").replace("\"", "'");
			xml = URLEncoder.encode(xml, "UTF-8");
			
			System.out.println(xml);
			
			connection.setRequestMethod("POST");
			
			connection.setRequestProperty("data", "\""+xml+"\"");
			//connection.setRequestProperty("Content-Length", xml.getBytes().length+"");
			
			int response = connection.getResponseCode();
			String responseMsg = connection.getResponseMessage();
			
			if (response >= 400) {
				
				System.out.println(response+" - "+responseMsg);
				
			} else {
			
				Scanner scanner = new Scanner(connection.getInputStream());
				scanner.useDelimiter("\\Z");
				String result = scanner.next();
				
				System.out.println(result);
				
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		*/
		
	}
	
	public static int getID(String title) {
		if (Settings.MAL_userName.length() < 2 && Settings.MAL_password.length() < 4) { return -1; }
		
		String search = title.trim().toLowerCase().replace(" ", "+");
		String html = Web.getHTML("http://myanimelist.net/api/manga/search.xml?q="+search, Settings.MAL_userName, Settings.MAL_password);
		
		if (html == null || html.length() < 50) { return -1; }
		
		Document doc = null;
		try { doc = DocumentHelper.parseText(html); } catch (Exception e) { e.printStackTrace(); return -1; }
		Element root = doc.getRootElement();
		
		Element first = root.element("entry");
		if (first == null) { return -1; }
		
		List<String> titles = new ArrayList<String>();
		titles.add(first.element("title").getText().trim().toLowerCase());
		
		String synonyms = first.element("synonyms").getText().trim();
		
		for (int i = 0; i < 100 && synonyms != null && synonyms.trim().length() > 0; i++) {
			
			int ind = synonyms.indexOf(";");
			if (ind == -1) { ind = synonyms.length(); }
			
			String s = synonyms.substring(0, ind);
			synonyms = synonyms.substring(Math.min(ind+1, synonyms.length()));
			
			titles.add(s.trim().toLowerCase());
		}
		
		boolean matches = false;
		for (String t : titles) {
			
			//System.out.println(t);
			
			if (t.equals(title.toLowerCase())) {
				
				matches = true;
				break;
			}
		}
		if (!matches) { return -1; }
		
		int id = -1;
		try {
			
			id = Integer.parseInt(first.element("id").getText().trim());
			
		} catch (Exception e) { e.printStackTrace(); }
		
		return id;
	}
	
	
	public static boolean canAuthenticate() {
		
		return hasUser() && hasPassword();
	}
	
	public static boolean hasUser() {
		
		return Settings.MAL_userName != null && Settings.MAL_userName.length() > 2;
	}
	
	public static boolean hasPassword() {
		
		return Settings.MAL_password != null && Settings.MAL_password.length() > 3;
	}
	
}
