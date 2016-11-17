package main;

import java.io.File;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class LoadingScene extends Scene {

	private void load() {
		
		Shaders.load();
		Fonts.load();
		Mangas.load();
		
		MAL.load();
		
		MangaDL.load();
		
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
			
				Main.currentScene = ImageView.openLastRead();
				if (Main.currentScene == null) { try { Main.currentScene = new ChapterView(Mangas.lastRead); } catch (Exception e) {} }
				if (Main.currentScene == null) { Main.currentScene = new MangaView(Settings.menu_mangaMode, Mangas.lastRead); }
			}
			
		} else if (Settings.bootInto == Settings.BOOT_LAST_CHAPTER) {
			
			try { Main.currentScene = new ChapterView(Mangas.lastRead); } catch (Exception e) { e.printStackTrace(); }
			
		}
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (sceneFrames == 2) {
			
			load();
		}
		
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		
		/*
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
		if (img != null) {
			
			float w = img.getWidth() * displayScale;
			float h = img.getHeight() * displayScale;
			
			float x = (Display.getWidth() - w) * 0.5f;
			float y = (Display.getHeight() - h) * 0.5f;
			
			img.draw((int)(x + 0.5f), (int)(y + 0.5f), (int)(w + 0.5f), (int)(h + 0.5f));
		}
		*/
		
	}
	
}
