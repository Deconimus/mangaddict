package components;

import java.io.File;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.geom.Rectangle;

import main.MangaDL;
import main.MangaView;
import main.Menu;
import main.Settings;
import mangaLib.MangaInfo;
import mangaLib.scrapers.Scraper;
import visionCore.util.Files;

public class ChapterSourceSelect extends ContextMenu {
	
	
	public static final String[] SITES = new String[]{ "Mangafox (default)", "MangaSeeOnline", "Enter URL" };
	public static final String[] SITE_URLS = new String[]{ "http://mangafox.me", "https://mangaseeonline.net", "asd" };
	
	
	public MangaInfo info;
	
	public Menu mv;
	
	
	public ChapterSourceSelect(MangaInfo info, Menu mv) {
		super("Select Chapter Source:", new Rectangle(0f, 0f, Display.getWidth(), Display.getHeight()), SITES);
		
		this.info = info;
		this.mv = mv;
	}
	
	
	@Override
	public void onAction(int selected) {
		
		Component chs = null;
		
		if (selected == 0) {
			
			info.chsubs = null;
			try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
			try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
			
		} else if (selected < SITES.length-1) {
			
			Scraper scraper = Scraper.getScraper(SITE_URLS[selected]);
			
			chs = new ChapterSourceSelectResults(scraper, info, mv);
			
		} else {
			
			chs = new InputPanel("Enter URL", (info.chsubs == null) ? "" : info.chsubs, null){
				
				@Override
				public void onAction(String input) {
					
					if (input == null || input.trim().length() <= 5) { return; }
					if (input.equals(info.chsubs)) { closed = true; return; }
					
					info.chsubs = input;
					try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					
					Component c = ChapterSourceSelectResults.getRedownloadDialog(info, mv);
					
					mv.components.add(c);
					mv.setFocus(c);
					
					closed = true;
					
				}
			};
			
		}
		
		if (chs != null) {
			
			if (mv instanceof MangaView) {
				
				MangaView mangv = (MangaView) mv;
			
				mangv.cm = chs;
				mangv.components.add(chs);
				
			} else {
				
				mv.components.add(chs);
				mv.setFocus(chs);
			}
		}
		
		this.closed = true;
	}
	
	@Override
	public void onCancel() {
		
		if (mv instanceof MangaView) {
			
			MangaView mangv = (MangaView)(mv);
			mangv.setFocus(mangv.list);
		}
	}
	
}
