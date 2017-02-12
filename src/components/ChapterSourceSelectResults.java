package components;

import static main.Main.displayScale;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import main.Fonts;
import main.GUIRes;
import main.Main;
import main.MangaDL;
import main.MangaView;
import main.Menu;
import main.Settings;
import mangaLib.MangaInfo;
import mangaLib.scrapers.Scraper;
import visionCore.geom.Color;
import visionCore.math.FastMath;
import visionCore.util.Files;

public class ChapterSourceSelectResults extends Component {
	
	
	public MangaInfo info;
	public Menu mv;
	
	public AtomicReference<MangaList> results;
	public AtomicBoolean loadingResults;
	public float loadingRot;
	
	public Thread t;
	
	public Rectangle pane;
	
	
	public ChapterSourceSelectResults(Scraper scraper, MangaInfo info, Menu mv) {
		
		this.info = info;
		this.mv = mv;
		
		this.results = new AtomicReference<MangaList>(null);
		this.loadingResults = new AtomicBoolean(true);
		this.loadingRot = 0f;
		
		this.pane = new Rectangle(0f, 0f, 640f * Main.displayScale, 720f * Main.displayScale);
		this.pane.setX((Display.getWidth() - pane.width) * 0.5f);
		this.pane.setY((Display.getHeight() - pane.height) * 0.5f);
		
		ChapterSourceSelectResults tis = this;
		
		t = new Thread(){
			
			{ setDaemon(true); }
			
			@Override
			public void run() {
				
				try {
				
					List<MangaInfo> searchResults = scraper.search(info.title);
					
					if (tis.closed) { return; }
					
					if (searchResults.size() == 1) {
						
						info.chsubs = searchResults.get(0).url;
						try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
						try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
						
						tis.closed = true;
						
						Component c = getRedownloadDialog(info, mv);
						
						mv.components.add(c);
						mv.setFocus(c);
					
					} else if (!searchResults.isEmpty()) {
					
						Rectangle listPane = new Rectangle(pane.x + 30f * displayScale, pane.y + 78f * displayScale,
														   pane.width - 60f * displayScale, pane.height - 78f * displayScale);
					
						MangaList ml = new MangaList(searchResults, null, null, listPane, false, 0){
							
							@Override
							public void onAction(MangaInfo entry) {
								
								info.chsubs = entry.url;
								try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
								try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
								
								tis.closed = true;
								
								Component c = getRedownloadDialog(info, mv);
								
								mv.components.add(c);
								mv.setFocus(c);
								
							}
						};
						tis.results.set(ml);
					}
					
				} catch (Exception | Error e) { e.printStackTrace(); }
					
				loadingResults.set(false);
			}
		};
		t.start();
	}
	
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		MangaList ml = results.get();
		
		if (ml != null) {
			
			ml.update(delta);
			
		} else if (loadingResults.get()) {
			
			loadingRot += UpdatingPanel.ROT_VEL * delta;
			loadingRot = FastMath.normalizeCircular(loadingRot, 0f, FastMath.PI2);
		}
		
	}
	
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		GUIRes.drawContextPane(g, pane, pX, pY);
		
		MangaList ml = results.get();
		
		g.setFont(Fonts.robotoBold.s30);
		g.drawString("Select Result:", pX + pane.x + 30f * displayScale, pY + pane.y + 20f * displayScale);
		
		if (ml != null) {
			
			ml.render(g, pX, pY);
			
		} else {
			
			if (loadingResults.get()) {
				
				float x = pX + pane.x + (pane.width - GUIRes.loading.getWidth()) * 0.5f;
				float y = pY + pane.y + (pane.height - GUIRes.loading.getHeight()) * 0.5f;
				
				GUIRes.loading.setRotation((loadingRot / FastMath.PI2) * 360f);
				GUIRes.loading.draw(x, y, displayScale, Menu.flavors[Settings.menu_flavor]);
				
			} else {
				
				String s = "No results found.";
				
				g.setColor(Color.lightGray);
				g.setFont(Fonts.roboto.s24);
				
				float x = pX + pane.x + (pane.width - g.getFont().getWidth(s)) * 0.5f;
				float y = pY + pane.y + (pane.height - g.getFont().getHeight(s)) * 0.5f;
				
				g.drawString(s, x, y);
				
				g.setColor(Color.white);
			}
		}
		
	}
	
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		
		if (pressed) {
			
			if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
				
				if (mv instanceof MangaView) {
					
					MangaView mangv = (MangaView) mv;
				
					mangv.cm = new ChapterSourceSelect(info, mv);
					mangv.components.add(mangv.cm);
					
				} else {
					
					Component chs = new ChapterSourceSelect(info, mv);
					mv.components.add(chs);
					mv.setFocus(chs);
				}
				
				this.closed = true;
				return;
			}
		}
		
		MangaList ml = results.get();
		
		if (ml != null) {
			
			ml.handleInput(key, c, pressed);
		}
	}
	
	
	public static Dialogbox getRedownloadDialog(MangaInfo info, Menu mv) {
		
		return new Dialogbox("Re-download chapters?", "This will re-download existing chapters from the new source.", new String[]{"Yes", "No"}){
			
			@Override
			public void onButton(String button) {
				
				if (button.toLowerCase().equals("yes")) {
					
					File dir = new File(Settings.mangaDir+"/"+info.title);
					if (!dir.exists()) { return; }
					
					for (File f : dir.listFiles()) {
						
						if (!f.isDirectory()) { continue; }
						
						boolean cd = true;
						for (int i = 0; i < 4; i++) { if (!Character.isDigit(f.getName().charAt(i))) { cd = false; } }
						if (!cd) { continue; }
						
						Files.deleteDir(f);
					}
					
					MangaDL.downloadManga(info.title);
					
				}
				
				closed = true;
				
				if (mv instanceof MangaView) {
					
					MangaView mangv = (MangaView)(mv);
					mangv.setFocus(mangv.list);
				}
			}
			
		};
	}
	
	
}
