package main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.SlickException;

import visionCore.math.FastMath;
import visionCore.reflection.Primitives;
import visionCore.util.Files;
import visionCore.util.StringUtils;

public class Settings {
	
	
	private static final char[] PASSWORD = "ElPsyCongroo".toCharArray();
	private static final byte[] SALT = new byte[]{ (byte) 0xCF, (byte) 0xBA, (byte) 0xBE, (byte) 0xFD, (byte) 0x60, (byte) 0xD, (byte) 0xc7, (byte) 0xfc };
	
	
	private static Thread saveThread;
	
	
	public static final String[] menu_flavor_names = new String[]{ "White", "Electric Blue", "Cyan", "Green", "Mint", "Orange", "Yellow", "Red", "Magenta" }, 
								bootInto_names = new String[]{ "Last Read", "Last Manga", "Manga", "Main Menu", "Get Manga", "Settings" }, 
								imageView_backgroundColor_names = new String[]{ "Black", "White" }, 
								video_targetFPS_names = new String[]{ "Native" },
								video_width_names = new String[]{ "Native" }, video_height_names = new String[]{ "Native" },
								menu_mangaMode_names = new String[]{ "Poster Row", "List", "Table" },
								video_dodgeTaskbar_names = new String[]{ "None" };
	
	
	// Field order here dictates the order inside the settings-menu

	public static int imageView_backgroundColor, imageView_clockSize;
	public static boolean imageView_showTime, imageView_showPageNr, imageView_alwaysShowChapter, imageView_debug;
	
	public static int video_width, video_height, video_targetFPS;
	public static boolean video_showFPS, video_bicubicFiltering;
	public static int video_monitor, video_dodgeTaskbar;
	public static boolean video_windowDecoration;
	
	public static int menu_flavor, menu_mangaMode;
	public static String menu_background, menu_download_background, menu_settings_background;
	public static int menu_reloadPopularAfterDays, menu_reloadSearchAfterDays, menu_searchHistoryMax;
	public static float menu_glassAlpha;
	
	public static int bootInto;
	public static String mangaDir, metaIn, mangadlPath;
	public static boolean mangaAutoUpdate;
	public static int updateEveryHours;
	public static long lastUpdated;
	
	public static String MAL_userName, MAL_password;
	public static boolean MAL_sync;
	
	
	public static final int BOOT_LAST_READ = 0, BOOT_LAST_CHAPTER = 1, BOOT_MANGAS = 2, BOOT_MAIN_MENU = 3, BOOT_DOWNLOAD = 4, BOOT_SETTINGS = 5;
	
	
	public static void load() {
		
		imageView_backgroundColor = 0;
		imageView_clockSize = 48;
		imageView_showTime = true;
		imageView_showPageNr = true;
		imageView_alwaysShowChapter = true;
		imageView_debug = false;
		
		video_width = 0;
		video_height = 0;
		video_windowDecoration = true;
		video_dodgeTaskbar = 0;
		video_bicubicFiltering = false;
		
		mangaDir = "manga";
		metaIn = mangaDir+"";
		mangadlPath = "";
		mangaAutoUpdate = true;
		bootInto = BOOT_LAST_READ;
		updateEveryHours = 24;
		lastUpdated = 0;
		
		menu_background = "steins_gate_01.png";
		menu_download_background = "hyouka_01.png";
		menu_settings_background = "ghost_in_the_shell_01.png";
		menu_flavor = 2;
		menu_glassAlpha = 0.25f;
		menu_mangaMode = MangaView.MODE_POSTER_ROW;
		menu_reloadPopularAfterDays = 7;
		menu_reloadSearchAfterDays = 1;
		menu_searchHistoryMax = 25;
		
		MAL_userName = "";
		MAL_password = "";
		MAL_sync = true;
		
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();
		GraphicsDevice primaryDevice = g.getDefaultScreenDevice();
		
		video_monitor = 0;
		for (int i = 0; i < devices.length; i++) {
			
			if (primaryDevice == devices[i]) { video_monitor = i; break; }
		}
		
		video_showFPS = false;
		video_targetFPS = -1;
		
		
		File xml = new File(Main.abspath+"/res/settings.xml");
		if (xml.exists()) {
			
			SAXReader reader = new SAXReader();
			Document doc = null;
			try { doc = reader.read(xml); }
			catch (DocumentException e) { System.out.println("Failed at reading \""+xml.getAbsolutePath().replace("\\", "/")+"\""); return; }
			
			Element root = doc.getRootElement();
			
			Field[] fields = Settings.class.getFields();
			
			for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
				Element elem = it.next();
				
				for (Iterator<Element> elit = elem.elementIterator(); elit.hasNext();) {
					Element el = elit.next();
				
					String elemName = elem.getName().trim()+"_";
					if (elemName.equalsIgnoreCase("general_")) { elemName = ""; }
					elemName += el.getName().trim();
					
					Field field = null;
					
					for (Field f : fields) {
						
						if (f.getName().equalsIgnoreCase(elemName)) {
							
							field = f;
							break;
						}
					}
					if (field == null) { continue; }
					
					Object parsed = null;
					try { parsed = StringUtils.parse(field.get(null), el.getText().trim()); } catch (Exception | Error e) {}
					
					if (field.getName().equals("MAL_password")) {
						
						String dec = null;
						try {
							
							dec = decrypt(el.getText().trim());
							
						} catch (Exception e) { e.printStackTrace(); }
						
						parsed = dec;
					}
					
					if (parsed == null) {
						
						Class c = Primitives.getBoxedClass(field.getType());
						
						for (Method m : c.getMethods()) {
							
							if (m.getName().toLowerCase().startsWith("parse")) {
								
								try {
									parsed = m.invoke(null, el.getText().trim());
									break;
								} catch (Exception e) {}
								
							}
							
						}
					}
					
					if (parsed == null) { continue; }
					
					try {
						
						field.set(null, Primitives.getUnboxed(parsed));
						
					} catch (Exception e) {}
					
				}
				
			}
			
		}
		
		
		video_monitor = FastMath.clampToRange(video_monitor, 0, devices.length-1);
		
		menu_flavor = FastMath.clampToRange(menu_flavor, 0, Menu.flavors.length-1);
		
		if (mangaDir != null) {
			
			mangaDir = mangaDir.replace("\\", "/");
			while (mangaDir.endsWith("/")) { mangaDir = mangaDir.substring(0, mangaDir.length()-1); }
			mangaDir = mangaDir.trim();
		}
		
		if (mangadlPath == null) { mangadlPath = ""; }
		else { mangadlPath = mangadlPath.replace("\\", "/").replace("\"", "").trim(); }
		
		if (metaIn == null || metaIn.trim().length() < 3) { metaIn = mangaDir+""; }
		else { metaIn = metaIn.replace("\\", "/").replace("\"", "").trim(); }
		
		save();
		
	}
	
	public static void settingChanged(Field field) {
		
		String fieldName = field.getName();
		
		if (fieldName.equals("video_showFPS")) {
			
			Main.display.setShowFPS(video_showFPS);
			
		} else if (fieldName.equals("video_targetFPS")) {
			
			int fps = video_targetFPS;
			
			if (fps < 5) {
				
				fps = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[video_monitor].getDisplayMode().getRefreshRate();
			}
			
			Main.display.setTargetFrameRate(fps);
			
		} else if (fieldName.equals("video_monitor") || fieldName.equals("video_width") || 
				   fieldName.equals("video_height") || fieldName.equals("video_dodgeTaskbar")) {
			
			resetWindowPos();
			
		} else if (fieldName.equals("mangaDir") || fieldName.equals("metaIn")) {
			
			Mangas.reload();
			
		} else if (fieldName.equals("MAL_userName")) {
			
			if (MAL.avatar == null || !MAL.avatar.getName().equals(Settings.MAL_userName.toLowerCase().trim())) {
				
				try { MAL.avatar.destroy(); } catch (Exception e) {}
				MAL.avatar = null;
				
				MAL.loadAvatar();
			}
			
		}
		
	}
	
	public static void resetWindowPos() {
		
		java.awt.Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[Settings.video_monitor].getDefaultConfiguration().getBounds();
		
		int w = Settings.video_width;
		int h = Settings.video_height;
		
		if (w < 640) { w = r.width; }
		if (h < 480) { h = r.height; }
		
		try {
			
			Main.display.setDisplayMode(w, h, false);
			
		} catch (Exception e) { e.printStackTrace(); }
		
		int x = (int)(r.x + (r.getWidth() - Display.getWidth()) * 0.5f);
		int y = (int)(r.y + (r.getHeight() - Display.getHeight()) * 0.5f);
		
		if (!Main.display.isFullscreen() && video_windowDecoration) { y -= 8f; }
		
		if (Settings.video_dodgeTaskbar != 0) {
			
			y = (int)FastMath.clampToRangeC(y + Settings.video_dodgeTaskbar, 0, r.getHeight() - Display.getHeight());
		}
		
		Display.setLocation(x, y);
		
		Main.displayScale = (float)Display.getHeight() / 1080f;
		
	}
	
	public static void save() {
		
		File xml = new File(Main.abspath+"/res/settings.xml");
		
		if (!xml.exists()) { xml.getParentFile().mkdirs(); }
		
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");
		
		Field[] fields = Settings.class.getFields();
		
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers())) { continue; }
			
			String prefix = "general";
			String fieldName = field.getName();
			
			if (fieldName.contains("_")) {
				
				prefix = fieldName.substring(0, fieldName.indexOf("_"));
				fieldName = fieldName.substring(fieldName.indexOf("_")+1);
			}
			
			Element section = root.element(prefix);
			if (section == null) { section = root.addElement(prefix); }
			
			try {
				
				String val = field.get(null).toString();
				
				if (field.getName().equals("MAL_password")) {
					
					val = encrypt(MAL_password);
				}
				
				section.addElement(fieldName).setText(val);
				
			} catch (Exception e) {}
			
		}
		
		
		String path = Settings.mangadlPath.trim();
		
		if (path == null || path.length() <= 2) {
			
			path = Main.abspath+"/res/bin/mangadl/cfg.xml";
			
		} else if (path.toLowerCase().endsWith(".jar")) {
			
			path = path.substring(0, path.lastIndexOf("/"));
		}
		
		if (!path.toLowerCase().endsWith(".xml")) {
			
			if (path.endsWith("/")) { path = path.substring(0, path.length()-1); }
			path += "/cfg.xml";
		}
		
		File mangadlxml = new File(path);
		if (!mangadlxml.exists()) { xml.getParentFile().mkdirs(); }
		
		Document mdldoc = DocumentHelper.createDocument();
		Element mdlroot = mdldoc.addElement("root");
		
		try { mdlroot.addElement("mangadir").setText(mangaDir); } catch (Exception e) {}
		try { mdlroot.addElement("metaout").setText(metaIn); } catch (Exception e) {}
		
		Thread t = new Thread(){
			
			@Override
			public void run() {
				
				OutputFormat format = OutputFormat.createPrettyPrint();
				format.setIndent("\t");
				
				try {
					
					Files.waitOnFile(xml, 15);
					
					if (xml.exists()) { xml.delete(); }
					
					XMLWriter writer = new XMLWriter(new FileWriter(xml), format);
					writer.write(doc);
					writer.close();
					
				} catch (Exception e) { e.printStackTrace(); }
				
				try {
					
					Files.waitOnFile(mangadlxml, 15);
					
					if (mangadlxml.exists()) { mangadlxml.delete(); }
					
					XMLWriter writer = new XMLWriter(new FileWriter(mangadlxml), format);
					writer.write(mdldoc);
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
	
	private static String encrypt(String property) throws GeneralSecurityException, UnsupportedEncodingException {
		
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		
		return Base64.getEncoder().encodeToString(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }
	
	private static String decrypt(String property) throws GeneralSecurityException, IOException {
		
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		
		return new String(pbeCipher.doFinal(Base64.getDecoder().decode(property)), "UTF-8");
    }
	
}

