package main;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import components.UpdatingPanel;
import mangaLib.MangaInfo;
import visionCore.math.FastMath;

public class LoadingScene extends Scene {

	
	private AtomicReference<Float> loadingRot;
	private AtomicBoolean isLoading;
	
	
	public LoadingScene() {
		
		loadingRot = new AtomicReference<Float>(0f);
		isLoading = new AtomicBoolean(true);
		
		Thread t = new Thread(){
			
			{ setDaemon(true); }
			
			@Override
			public void run() {
				
				float d = (1000f / (float)Main.display.getTargetFrameRate());
				int di = (int)(d + 0.5f);
				
				while (isLoading.get()) {
					
					float add = UpdatingPanel.ROT_VEL * d;
					
					float lr = loadingRot.get() + add;
					FastMath.normalizeCircular(lr, 0f, FastMath.PI2);
					loadingRot.set(lr);
					
					try { Thread.sleep(di); } catch (Exception e) { break; }
				}
			}
		};
		t.start();
	}
	
	
	private void load(int part) throws SlickException {
		
		if (part == 0) { GUIRes.load(); } 
		else if (part == 1) { Shaders.load(); }
		else if (part == 2) { Fonts.load(); }
		else if (part == 3) { Mangas.load(); }
		else if (part == 4) { MAL.load(); }
		else if (part == 5) { MangaDL.load(); }
		else {
			
			isLoading.set(false);
			
			/*
			if (Settings.mangaAutoUpdate && System.currentTimeMillis() - Settings.lastUpdated >= 3600L * 1000L * Settings.updateEveryHours) {
				
				Main.mangadl.set(MangaDL.updateMangas());
			}
			
			MangaDL.startQueueDaemon();
			*/
			
			boolean lr = (Mangas.lastRead != null && Mangas.lastRead.trim().length() > 0);
			
			if (Settings.bootInto == Settings.BOOT_MAIN_MENU) {
				
				try { Main.currentScene = new MainMenu(); } catch (Exception e) { e.printStackTrace(); }
				
			} else if (Settings.bootInto == Settings.BOOT_DOWNLOAD) {
				
				try { Main.currentScene = new DownloadMenu(); } catch (Exception e) { e.printStackTrace(); }
				
			} else if (Settings.bootInto == Settings.BOOT_SETTINGS) {
				
				try { Main.currentScene = new SettingsMenu(); } catch (Exception e) { e.printStackTrace(); }
				
			} else if (!lr || Settings.bootInto == Settings.BOOT_MANGAS) {
				
				Main.currentScene = new MangaView(Settings.menu_mangaMode, Mangas.lastRead);
				
			} else if (Settings.bootInto == Settings.BOOT_LAST_READ) {
				
				File mangadir = new File(Settings.mangaDir);
				
				if (!mangadir.exists() || mangadir.listFiles() == null || mangadir.listFiles().length < 2) {

					Main.currentScene = new DownloadMenu();
					
				} else {
					
					MangaInfo manga = Mangas.get(Mangas.lastRead);
					
					if (manga == null || (manga.lastChapter < 0 || manga.lastPage < 0)) {
						
						Main.currentScene = new MangaView(Settings.menu_mangaMode, Mangas.lastRead);
						
					} else {
						
						Main.currentScene = ImageView.openLastRead();
						if (Main.currentScene == null) { try { Main.currentScene = new ChapterView(Mangas.lastRead); } catch (Exception e) {} }
						if (Main.currentScene == null) { Main.currentScene = new MangaView(Settings.menu_mangaMode, Mangas.lastRead); }
					}
				}
				
			} else if (Settings.bootInto == Settings.BOOT_LAST_CHAPTER) {
				
				try { Main.currentScene = new ChapterView(Mangas.lastRead); } catch (Exception e) { e.printStackTrace(); }
				
			}
			
		}
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (sceneFrames >= 2) {
			
			load((int)(sceneFrames - 2));
		}
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		
		Image img = GUIRes.menubg;
		if (img != null) {
			
			float w = (float)Display.getWidth() / (float)img.getWidth();
			float h = (float)Display.getHeight() / (float)img.getHeight();
			
			float scale = Math.max(w, h);
			
			int x = (int)((Display.getWidth() - img.getWidth() * scale) * 0.5f);
			int y = (int)((Display.getHeight() - img.getHeight() * scale) * 0.5f);
			
			img.draw(x, y, scale);
		}
		
		img = GUIRes.loadingBanner;
		Image img2 = GUIRes.loading;
		if (img != null) {
			
			float w = img.getWidth() * Main.displayScale;
			float h = img.getHeight() * Main.displayScale;
			
			float w2 = img2.getWidth() * Main.displayScale;
			float h2 = img2.getHeight() * Main.displayScale;
			
			float x = (Display.getWidth() - w) * 0.5f;
			float y = (Display.getHeight() - h - 10f * Main.displayScale - h2) * 0.5f;
			
			img.draw((int)(x + 0.5f), (int)(y + 0.5f), (int)(w + 0.5f), (int)(h + 0.5f));
			
			x =  (Display.getWidth() - w2) * 0.5f;
			y += h + 10f * Main.displayScale;
			
			GUIRes.loading.setRotation((loadingRot.get() / FastMath.PI2) * 360f);
			img2.draw((int)(x + 0.5f), (int)(y + 0.5f), (int)(w2 + 0.5f), (int)(h2 + 0.5f), Menu.flavors[Settings.menu_flavor]);
		}
		
	}
	
}
