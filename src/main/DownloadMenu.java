package main;

import static main.Main.displayScale;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;
import visionCore.util.Files;

public class DownloadMenu extends Menu {
	
	
	public MenuList<String> sectionList;
	
	public MenuList<?>[] tables;
	public Rectangle tablepane;
	
	public static MangaDL mangadl, mangadlSearch, mangadlMAL;
	public boolean loadedPopular, searched, loadedMAL;
	
	public float loadingRot;
	
	public String search;
	
	
	public DownloadMenu() {
		
		this.title = "GET NEW MANGA";
		this.renderComponents = false;
		
		DownloadMenu tis = this;
		this.loadingRot = 0f;
		
		setBG(GUIRes.downloadbg);
		
		this.tables = new MenuList<?>[6];
		
		contentpanel.setWidth(1280f * displayScale);
		float x = (Display.getWidth() - contentpanel.width) * 0.5f;
		contentpanel.setX(x);
		
		Rectangle sectionpane = new Rectangle(contentpanel.x + 30f * displayScale, contentpanel.y + 20f * displayScale,
														250f * displayScale, contentpanel.height - 40f * displayScale);
		
		List<String> items = Arrays.asList(new String[]{ "POPULAR", "SEARCH", "OTHER DEVICES", "PLAN TO READ", "ON HOLD", "DROPPED" });
		
		sectionList = new MenuList<String>(items, sectionpane, 56f * displayScale, MenuList.MODE_VERTICAL, false){
			
			@Override
			public void onAction(String entry) {
				
				if (tables[selected] != null && !tables[selected].entries.isEmpty()) {
					
					tis.setFocus(tables[selected]);
				}
			}
			
			@Override
			public boolean onRightBorder() {
				
				if (tables[selected] != null && !tables[selected].entries.isEmpty()) {
					
					tis.setFocus(tables[selected]);
				}
				return true;
			}
			
			@Override
			public void render(Graphics g, float pX, float pY) throws SlickException {
				super.render(g, pX, pY);
				
				GUIRes.splitter.draw((int)(-30f * displayScale + pane.x), (int)(pane.y + entryHeight * (tables.length) - 1f), 
								(int)(pane.width + 40f * displayScale), (int)(GUIRes.splitter.getHeight() * displayScale + 0.5f), Color.white.copy().setAlpha(0.6f));
				
				g.setFont(Fonts.roboto.s30);
				g.setColor(Color.grayTone(140));
				
				String s = "------------MAL------------";
				
				float x = pX + pane.x + (pane.width - 20f * displayScale - g.getFont().getWidth(s)) * 0.5f;
				float y = pY + pane.y + entryHeight * (3 - camera) + (entryHeight - g.getFont().getHeight(s)) * 0.5f;
				
				g.drawString(s, x, y);
				
			}
			
			@Override
			public void renderEntry(Graphics g, String entry, float x, float y, boolean selected, int i) {
				
				if (i > 2) {
					
					y += entryHeight;
				}
				
				super.renderEntry(g, entry, x, y, selected, i);
			}
			
		};
		
		components.add(sectionList);
		
		tablepane = new Rectangle(sectionpane.x + sectionpane.width + 40f * displayScale, contentpanel.y + 20f * displayScale,
								contentpanel.width - 100f * displayScale - sectionpane.width, contentpanel.height - 40f * displayScale);
		
		/*
		list = new MangaTable(new File(Main.abspath+"/res/bin/mangadl/tmp/hot"), tablepane, false){
			
			@Override
			public boolean onLeftBorder() {
				
				tis.setFocus(sectionList);
				return true;
			}
			
		};
		
		components.add(list);
		*/
		
		setFocus(sectionList);
		
		this.loadedPopular = false;
		this.loadedMAL = false;
		
	}
	
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if ((sceneTime < 250 && Main.display.getTargetFrameRate() < 45)) {
			
			return;
		}
		
		if (tables[sectionList.selected] == null) {
			
			loadingRot += UpdatingPanel.ROT_VEL * delta;
			loadingRot = FastMath.normalizeCircular(loadingRot, 0f, FastMath.PI2);
			
		}
		
		if (mangadl != null) {
			
			mangadl.update(delta);
			if (mangadl.finished.get()) { mangadl = null; }
			
			if (tables[0] == null && (mangadl == null || mangadl.status.get().toLowerCase().startsWith("loading posters"))) {
				
				loadPopular();
			}
			
		} else if (!loadedPopular) {
			
			File timetxt = new File(Main.abspath+"/res/bin/mangadl/tmp/hot/time");
			if (timetxt.exists()) {
				
				String txt = Files.readText(timetxt);
				
				long time = 0;
				try { time = Long.parseLong(txt.replace("\n", "").replace("\r", "").trim()); } catch (Exception | Error e) {}
				
				if (time == 0 || System.currentTimeMillis() - time >= Settings.menu_reloadPopularAfterDays * 24L * 3600L * 1000L) {
					
					mangadl = MangaDL.dumpHot();
					
				} else {
					
					loadPopular();
					
				}
				
			} else { mangadl = MangaDL.dumpHot(); }
			
		}
		
		if (loadedPopular) {
		
			if (tables[1] == null) {
				
				if (!searched) {
					
					loadSearchH();
				
				} else if (this.search != null && (mangadlSearch == null || mangadlSearch.status.get().toLowerCase().startsWith("loading posters"))) {
					
					loadSearchTable(this.search);
				}
				
			} else if (tables[2] == null) {
				
				loadOnOtherDevices();
				
			} else if (tables[5] == null) {
				
				if (MAL.hasUser()) {
				
					if (!loadedMAL) {
						
						mangadlMAL = MangaDL.dumpMAL();
						loadedMAL = true;
						
					} else if (mangadlMAL == null || mangadlMAL.status.get().toLowerCase().startsWith("loading posters")) {
						
						for (int i = 3; i < 6; i++) {
							
							int ind = i - 3;
							
							if (tables[i] == null) {
								
								loadMAL(ind);
								break;
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		if (tables[sectionList.selected] != null) { 
			
			tables[sectionList.selected].update(delta);
		}
		
	}
	
	
	@Override
	public void render(Graphics g) throws SlickException {
		super.render(g);
		
		GUIRes.menuSectionPanel.draw(contentpanel.x, contentpanel.y, contentpanel.width, contentpanel.height);
		
		if (tables[sectionList.selected] != null) {
			
			tables[sectionList.selected].render(g, 0f, 0f);
			
		} else {
			
			int x = (int)(tablepane.x + (tablepane.width - GUIRes.loading.getWidth() * displayScale) * 0.5f + 0.5f);
			int y = (int)(tablepane.y + (tablepane.height - GUIRes.loading.getHeight() * displayScale) * 0.5f + 0.5f);
			
			GUIRes.loading.setRotation((loadingRot / FastMath.PI2) * 360f);
			GUIRes.loading.draw(x, y, displayScale, Menu.flavors[Settings.menu_flavor]);
		}
		
		if (MAL.hasUser()) {
			
			MAL.renderProfile(g, 2);
		}
		
		renderComponents(g);
		
	}
	
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		
		if (pressed) {
			
			if (getFocus() == sectionList && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
				
				clearScene();
				Main.currentScene = new MainMenu(1);
				
			}
			
		}
		
		super.handleInput(key, c, pressed);
	}
	
	
	private void loadPopular() {

		loadedPopular = true;
		DownloadMenu tis = this;
		
		MangaTable mt = new MangaTable(new File(Main.abspath+"/res/bin/mangadl/tmp/hot/"), new Rectangle(tablepane), true, 0){
			
			@Override
			public void handleInput(int key, char c, boolean pressed) {
				
				if (pressed && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
					
					tis.setFocus(sectionList);
					return;
				}
				
				super.handleInput(key, c, pressed);
			}
			
			@Override
			public boolean onLeftBorder() {
				
				tis.setFocus(sectionList);
				
				return true;
			}
			
			@Override
			public void onAction(MangaInfo entry) {
				
				openInfoPanel(entry, new File(Main.abspath+"/res/bin/mangadl/tmp/hot/"+entry.title+"/_metadata"), true);
			}
			
		};
		mt.sortByRank = true;
		
		tables[0] = mt;
		
	}
	
	private void loadSearchH() {
		
		List<String> history = Files.readLines(new File(Main.abspath+"/res/mangasearch"), false);
		Collections.reverse(history); // REVERSING HISTORY!!
		history.add(0, "New search");
		
		DownloadMenu tis = this;
		
		tables[1] = new MenuList<String>(history, new Rectangle(tablepane), 52f * displayScale, MenuList.MODE_VERTICAL, false){
			
			@Override
			public void handleInput(int key, char c, boolean pressed) {
				
				if (pressed) {
				
					if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
						
						tis.setFocus(sectionList);
						return;
						
					} else if ((key == Input.KEY_E && (Main.ctrlDown || Main.altDown)) || key == Input.KEY_F1) {
						
						if (selected > 0) {
							
							openSearchHContext(selected);
						}
						
					}
					
				}
				
				super.handleInput(key, c, pressed);
			}
			
			@Override
			public boolean onLeftBorder() {
				
				tis.setFocus(sectionList);
				
				return true;
			}
			
			@Override
			public void onAction(String entry) {
				
				if (selected == 0) {
					
					InputPanel sp = new InputPanel("Search for Manga"){
						
						@Override
						public void onAction(String search) {
							search = MangaInfo.cleanTitle(search);
							
							MenuList<String> ml = (MenuList<String>)(tis.tables[1]);
							
							if (ml.entries.size() < 2 || !ml.entries.get(1).trim().equalsIgnoreCase(search.trim())) {
								
								ml.entries.add(1, search);
							}
							
							writeHistory();
							
							doSearch(search);
						}
						
						@Override
						public void onCancel() {
							
							tis.setFocus(tis.tables[1]);
						}
						
					};
					
					tis.components.add(sp);
					tis.setFocus(sp);
					
				} else {
					
					doSearch(entry);
				}
				
			}
			
		};
		
	}
	
	private void doSearch(String search) {
		search = search.trim();
		
		this.tables[1] = null;
		this.searched = true;
		this.search = search;
		
		
		File tmpdir = new File(Main.abspath+"/res/bin/mangadl/tmp");
		if (tmpdir.exists() && tmpdir.listFiles() != null) {
			
			File dir = null;
			
			for (File f : tmpdir.listFiles()) {
				if (!f.isDirectory()) { continue; }
				
				if (f.getName().equalsIgnoreCase(search)) {
					
					dir = f;
					break;
				}
			}
			
			if (dir != null) {
				
				File lock = new File(dir.getAbsolutePath()+"/lock");
				if (!lock.exists()) {
					
					File f = new File(dir.getAbsolutePath()+"/time");
					if (f.exists()) {
						
						String s = Files.readText(f).trim();
						long t = 0L;
						
						try { t = Long.parseLong(s); } 
						catch (Exception | Error e) {}
						
						if (System.currentTimeMillis() - t < Settings.menu_reloadSearchAfterDays * 24L * 3600L * 1000L) {
							
							loadSearchTable(search);
							return;
						}
						
					}
					
				}
				
			}
			
		}
		
		if (mangadlSearch != null) {
			
			if (mangadlSearch.process.isAlive()) {
				
				mangadlSearch.cancelled.set(true);
				mangadlSearch.process.destroy();
			}
		}
		
		mangadlSearch = MangaDL.dumpSearch(search);
		
	}
	
	private void loadSearchTable(String search) {
		search = search.trim();
		
		DownloadMenu tis = this;
		
		final String sq = search;
		
		MangaTable mt = new MangaTable(new File(Main.abspath+"/res/bin/mangadl/tmp/"+search+"/"), new Rectangle(tablepane), true, 0){
			
			@Override
			public void handleInput(int key, char c, boolean pressed) {
				
				if (pressed && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
					
					tis.tables[1] = null;
					loadSearchH();
					searched = false;
					tis.setFocus(tis.tables[1]);
					return;
				}
				
				super.handleInput(key, c, pressed);
			}
			
			@Override
			public boolean onLeftBorder() {
				
				tis.setFocus(sectionList);
				
				return true;
			}
			
			@Override
			public void onAction(MangaInfo entry) {
				
				openInfoPanel(entry, new File(Main.abspath+"/res/bin/mangadl/tmp/"+sq+"/"+entry.title+"/_metadata"), true);				
			}
			
		};
		
		mt.sortByRank = true;
		tables[1] = mt;
		
		setFocus(tables[1]);
	}
	
	private void loadOnOtherDevices() {
		
		DownloadMenu tis = this;
		
		MangaTable mt = new MangaTable(new File(Settings.metaIn), f -> Mangas.get(f.getName()) == null, new Rectangle(tablepane), true, 0){
			
			@Override
			public void handleInput(int key, char c, boolean pressed) {
				
				if (pressed && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
					
					tis.setFocus(sectionList);
					return;
				}
				
				super.handleInput(key, c, pressed);
			}
			
			@Override
			public boolean onLeftBorder() {
				
				tis.setFocus(sectionList);
				
				return true;
			}
			
			@Override
			public void onAction(MangaInfo entry) {
				
				openInfoPanel(entry, new File(Settings.metaIn+"/"+entry.title+"/_metadata"), false);
			}
			
		};
		mt.sortByRank = true;
		
		tables[2] = mt;
		
	}
	
	private void openSearchHContext(int selected) {
		
		DownloadMenu tis = this;
		
		String[] items = new String[]{ "Search", "Remove from History", "Clear History" };
		
		Rectangle r = new Rectangle(contentpanel.x, tablepane.y + 52f * (selected + 2) * displayScale, contentpanel.width, 52f * displayScale);
		
		ContextMenu cm = new ContextMenu(r, items) {
			
			@Override
			public void onAction(int sel) {
				
				if (sel == 0) {
					
					tis.doSearch((String)(tis.tables[1].entries.get(selected)));
					
				} else if (sel == 1) {
					
					File dir = new File(Main.abspath+"/res/bin/mangadl/tmp/"+MangaInfo.cleanTitle((String)tis.tables[1].entries.get(selected)));
					if (dir.exists()) { Files.cleanseDir(dir); dir.delete(); }
					
					tis.tables[1].entries.remove(selected);
					tis.tables[1].setFocus(Math.max(tis.tables[1].selected-1, 0));
					tis.writeHistory();
					
					tis.setFocus(tis.tables[1]);
					
				} else if (sel == 2) {
					
					MenuList<String> ml = (MenuList<String>)tis.tables[1];
					
					ml.setFocus(0);
					ml.entries.clear();
					ml.entries.add("New search");
					
					tis.writeHistory();
					
					File dir = new File(Main.abspath+"/res/bin/mangadl/tmp");
					if (dir.exists() && dir.listFiles() != null) {
						
						for (File fil : dir.listFiles()) {
							String fn = fil.getName().toLowerCase().trim();
							if (!fil.isDirectory() || fn.equals("hot") || fn.startsWith("mal_")) { continue; }
							
							Files.cleanseDir(fil);
							fil.delete();
						}
						
					}
					
					tis.setFocus(tis.tables[1]);
					
				}
				
				closed = true;
			}
			
			@Override
			public void onCancel() {
				
				tis.setFocus(tis.tables[1]);
			}
			
		};
		
		this.components.add(cm);
		this.setFocus(cm);
		
	}
	
	private void openInfoPanel(MangaInfo entry, File metadir, boolean p2r) {
		
		DownloadMenu tis = this;
		
		if (entry != null) {
			
			String[] buttons = null;
			
			if (p2r) {
				
				buttons = new String[]{ "Download", "Plan To Read", "Cancel" };
				
			} else {
				
				buttons = new String[]{ "Download", "Cancel" };
			}
			
			MangaInfoPanel mip = new MangaInfoPanel(entry, metadir, buttons){
				
				@Override
				public void onClosing() {
					
					tis.setFocus(tables[sectionList.selected]);
				}
				
			};
			
			tis.components.add(mip);
			tis.setFocus(mip);
			
		}
		
	}
	
	
	private void loadMAL(int ind) {
		
		String dr = "";
		
		switch (ind) {
		
			case 0: dr = "mal_planToRead"; break;
				
			case 1: dr = "mal_onHold"; break;
				
			case 2: dr = "mal_dropped"; break;
				
			default: return;
		}
		
		DownloadMenu tis = this;
		
		File fl = new File(Main.abspath+"/res/bin/mangadl/tmp/"+dr);
		
		MangaTable mt = new MangaTable(fl, f -> Mangas.get(f.getName()) == null, new Rectangle(tablepane), true, 0){
			
			@Override
			public void handleInput(int key, char c, boolean pressed) {
				
				if (pressed && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
					
					tis.setFocus(sectionList);
					return;
				}
				
				super.handleInput(key, c, pressed);
			}
			
			@Override
			public boolean onLeftBorder() {
				
				tis.setFocus(sectionList);
				
				return true;
			}
			
			@Override
			public void onAction(MangaInfo entry) {
				
				openInfoPanel(entry, new File(fl.getAbsolutePath()+"/"+entry.title+"/_metadata"), false);
			}
			
		};
		
		tables[3 + ind] = mt;
	}
	
	
	@Override
	public void clearScene() {
		super.clearScene();
		
		killMangaDL();
		
		for (MenuList<?> t : tables) {
			
			if (t != null) { t.clear(); }
		}
		
	}
	
	public static void killMangaDL() {
		
		if (mangadl != null) {
			
			if (mangadl.process.isAlive()) {
				
				mangadl.cancelled.set(true);
				mangadl.process.destroy();
			}
		}
		
		if (mangadlSearch != null) {
			
			if (mangadlSearch.process.isAlive()) {
				
				mangadlSearch.cancelled.set(true);
				mangadlSearch.process.destroy();
			}
		}
		
		if (mangadlMAL != null) {
			
			if (mangadlMAL.process.isAlive()) {
				
				mangadlMAL.cancelled.set(true);
				mangadlMAL.process.destroy();
			}
		}
		
	}
	
	private void writeHistory() {
		
		File f = new File(Main.abspath+"/res/mangasearch");
		if (f.exists()) { f.delete(); }
		
		if (tables[1].entries.size() <= 1) { return; }
		
		String txt = "";
		
		for (int size = tables[1].entries.size(), i = Math.min(size-1, Settings.menu_searchHistoryMax); i >= 1; i--) {
			
			txt += tables[1].entries.get(i)+"\r\n";
		}
		
		txt = txt.substring(0, Math.max(txt.length()-2, 0));
		
		Files.writeText(f, txt);
	}
	
}
