package main;

import static main.Main.displayScale;
import static main.Mangas.POSTER_HEIGHT;
import static main.Mangas.POSTER_WIDTH;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;
import visionCore.util.Files;

public class MangaView extends Menu {

	public static final int MODE_POSTER_ROW = 0, MODE_LIST = 1, MODE_TABLE = 2;
	
	public int mode, sortMode;
	
	public List<MangaInfo> infos;
	
	public MangaList list;
	
	public static Vector2f selectedCenter;
	public Component cm;
	
	public PosterPanel posterP;
	
	public MangaView(int mode) {
		
		this(mode, "");
	}
	
	public MangaView(int mode, String lastTitle) {
		
		this.mode = mode;
		this.sortMode = MangaList.SORT_ALPHABETICALLY;
		
		setBG(GUIRes.menubg);
		this.title = "MANGA";
		
		this.selectedCenter = new Vector2f(0f, 0f);
		
		HashMap<String, MangaInfo> mangas = Mangas.mangas.get();
		
		infos = new ArrayList<MangaInfo>(mangas.size());
		for (MangaInfo info : mangas.values()) { infos.add(info); }
		
		MangaList.sortInfos(infos, sortMode);
		
		int lastTitleInd = 0;
		for (int i = 0; i < infos.size(); i++) {
			MangaInfo info = infos.get(i);
			
			info.read = Files.getFiles(Settings.mangaDir+"/"+info.title, f1 -> f1.isDirectory() && !f1.getName().startsWith("_")).size() <= info.readChapters.size();
			
			if (lastTitle != null && lastTitle.trim().length() > 0 &&
				info.title.toLowerCase().trim().equals(lastTitle.toLowerCase().trim())) {
				
				lastTitleInd = i;
			}
			
			//Image img = loadPoster(info.title, info.poster);
			
			//posters.add(img);
		}
		
		lastTitleInd = FastMath.clampToRangeC(lastTitleInd, 0, infos.size());
		
		if (mode == MODE_POSTER_ROW) {
			
			MangaView tis = this;
			
			this.contentpanel.setX(200f * displayScale);
			
			float dsh = (Display.getWidth() / 1920f);
			
			Rectangle listpane = new Rectangle(200f * dsh, Display.getHeight() - POSTER_HEIGHT * 0.75f * displayScale - 100f * displayScale,
												Display.getWidth() - 200f * 2f * dsh, POSTER_HEIGHT * 0.75f * displayScale);
			
			list = new MangaPosterRow(infos, new File(Settings.metaIn), null, listpane, false) {
				
				@Override
				public void onAction(MangaInfo entry) {
					
					mangaOnAction(entry, posters);
				}
				
			};
			
			list.setFocus(lastTitleInd);
			
			
		} else if (mode == MODE_LIST) {
			
			MangaView tis = this;
			
			Rectangle posterpanel = new Rectangle(0f, 150f * displayScale, GUIRes.posterPanel.getWidth() * displayScale, GUIRes.posterPanel.getHeight() * displayScale);
			float pad = 25f * displayScale;
			float x = (int)((Display.getWidth() - (posterpanel.width + pad + contentpanel.width)) * 0.5f);
			posterpanel.setX(x);
			contentpanel.setX(posterpanel.getMaxX() + pad);
			
			Rectangle listpanel = new Rectangle(contentpanel.x + 30f, contentpanel.y + 20f, contentpanel.width - 60f, contentpanel.height - 40f);
			
			list = new MangaList(infos, new File(Settings.metaIn), null, listpanel, false){
				
				@Override
				public void update(int delta) throws SlickException {
					super.update(delta);
					
					Image pstr = posters.get(entries.get(selected).title);
					if (pstr != null && tis.posterP.poster == null) {
						
						tis.posterP.set(entries.get(selected), pstr); 
					}
					
				}
				
				@Override
				protected void down(boolean byButton) {
					super.down(byButton);
					
					tis.posterP.set(tis.infos.get(selected), posters.get(entries.get(selected).title));
				}
				
				@Override
				protected void up(boolean byButton) {
					super.up(byButton);
					
					tis.posterP.set(tis.infos.get(selected), posters.get(entries.get(selected).title));
				}
				
				@Override
				public void onAction(MangaInfo entry) {
					
					tis.clearScene(entry.title);
					Main.currentScene = new ChapterView(entry.title, posters.get(entry.title));
				}
				
			};
			list.drawPanels = true;
			list.setFocus(lastTitleInd);
			
			posterP = new PosterPanel(infos.get(lastTitleInd), posterpanel, list.posters.get(lastTitle));
			this.components.add(posterP);
			
			
		} else if (mode == MODE_TABLE) {
			
			MangaView tis = this;
			
			float entryW = MangaList.getEntrySize(MenuList.MODE_TABLE).x + 25f * displayScale;
			float w = (int)((Display.getWidth() - (60f + 200f) * displayScale) / entryW) * entryW - 25f * displayScale;
			float x = (Display.getWidth() - w) * 0.5f;
			
			contentpanel.setBounds((int)x, contentpanel.y, (int)w, contentpanel.height);
			
			Rectangle listpanel = new Rectangle(x, contentpanel.y + 20f * displayScale,
												w, contentpanel.height - 40f * displayScale);
			
			list = new MangaTable(infos, new File(Settings.metaIn), null, listpanel, false, true) {
				
				@Override
				public void onAction(MangaInfo entry) {
					
					mangaOnAction(entry, posters);
				}
				
			};
			list.drawPanels = true;
			list.setFocus(lastTitleInd);
			
		}
		
		this.components.add(list);
		setFocus(list);
		
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		super.render(g);
		
		g.setFont(Fonts.roboto.s21);
		
		String s = "Sort by ";
		if (sortMode == 0) { s += "title"; }
		else if (sortMode == 1) { s += "recently read"; }
		else if (sortMode == 2) { s += "new updates"; }
		
		float x = titleX + Fonts.roboto.s48.getWidth(title) + 20f * displayScale;
		float y = titleY + Fonts.roboto.s48.getHeight(title) - 7f * displayScale - g.getFont().getHeight(s);
		
		if (mode == MODE_POSTER_ROW) {
			
			x = titleX;
			y = titleY + Fonts.roboto.s48.getHeight(title);
		}
		
		int shadow = Math.max((int)(2f * displayScale + 0.5f), 1);
		
		g.drawStringShadow(s.toUpperCase(), (int)(x), (int)(y), shadow, 0.1f);
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		if (cm != null && !cm.closed) { cm.handleInput(key, c, pressed); return; }
		
		super.handleInput(key, c, pressed);
		
		if (pressed) {
			
			MangaInfo info = (infos.isEmpty()) ? null : infos.get(list.selected);
			
			if ((key == Input.KEY_E && (Main.ctrlDown || Main.altDown)) || key == Input.KEY_F1) {
				
				openContextMenu();
				
			} else if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
				
				clearScene();
				Main.currentScene = new MainMenu();
				
			} else if (key == Input.KEY_SPACE) {
				
				clearScene();
				Main.currentScene = ImageView.openLastRead(info.title);
				
			} else if (key == Input.KEY_F3) {
				
				openMangaInfoPanel(info);
				
			} else if ((key == Input.KEY_F && (Main.ctrlDown || Main.altDown)) || key == Input.KEY_F5) {
				
				sortMode = (sortMode + 1) % 3;
				
				String ft = list.entries.get(list.selected).title;
				
				list.sortEntries(sortMode);
				
				list.setFocus(ft);
				
			}
			
		}
		
	}
	
	private void mangaOnAction(MangaInfo entry, HashMap<String, Image> posters) {
		
		this.clearScene(entry.title);
		
		File d = new File(Settings.mangaDir+"/"+entry.title);
		
		File[] fl = null;
		if (d != null) {
			
			fl = d.listFiles(f -> f.isDirectory() && Character.isDigit(f.getName().trim().charAt(0)));
		}
		
		if (fl != null && fl.length == 1) {
			
			if (entry.lastPage <= 0 || entry.lastChapter <= 0) {
			
				File[] fll = fl[0].listFiles(f -> ImageFormats.isSupported(f));
				if (fll != null && fll.length > 0) {
					
					File f = fll[0];
					if (f != null) {
						
						try { Main.currentScene = new ImageView(f); } 
						catch (Exception e) { Main.currentScene = new ChapterView(entry.title, posters.get(entry.title)); }
					}
				}
				
			} else {
				
				Main.currentScene = ImageView.openLastRead(entry.title);
			}
			
		} else {
			
			Main.currentScene = new ChapterView(entry.title, posters.get(entry.title));
		}
	}
	
	private void openContextMenu() {
		
		MangaInfo info = infos.get(list.selected);
		
		boolean read = Files.getFiles(Settings.mangaDir+"/"+info.title, f1 -> f1.isDirectory() && !f1.getName().startsWith("_")).size() <= info.readChapters.size();
		
		String open = (info.lastChapter > 0) ? "Continue" : "Start";
		String mark = (read) ? "unread" : "read";
		
		Rectangle centerIn = null;
		
		if (mode == MODE_POSTER_ROW) {
			
			centerIn = new Rectangle(selectedCenter.x, selectedCenter.y, 0f, 0f);
			
		} else if (mode == MODE_LIST) {
			
			centerIn = new Rectangle(contentpanel.x, contentpanel.y + (list.selected - list.camera + 2) * list.entryHeight, contentpanel.width, list.entryHeight);
			
		}
		
		boolean updating = Main.mangadl.get() != null;
		String gr = (updating) ? "//" : "";
		
		MenuList<MangaInfo> mvlist = list;
		
		List<String> items = new ArrayList<String>();
		
		items.add(open+" reading");
		items.add("Mark as "+mark);
		
		if (info.lastChapter > 0) { items.add("Remove Bookmark"); }
		
		items.add("Show Manga Info");
		items.add("Update Manga");
		items.add("Select Poster");
		
		MangaView tis = this;
		
		cm = new ContextMenu(centerIn, items.toArray(new String[items.size()])){
			
			@Override
			public void onAction(int selected) {
				
				if (items.size() == 5 && selected >= 2) { selected++; }
				
				if (selected == 0) {
					
					clearScene();
					Main.currentScene = ImageView.openLastRead(info.title);
					
					closed = true;
					
				} else if (selected == 1) {
					
					if (read) {
						
						info.readChapters.clear();
						info.read = false;
						
					} else {
						
						List<File> fl = Files.getFiles(new File(Settings.mangaDir+"/"+info.title+"/"), f -> f.isDirectory() && !f.getName().startsWith("_"));
						
						for (File f : fl) {
							
							String chname = f.getName();
							double chnr = -1.0;
							try { chnr = Double.parseDouble(chname.substring(0, chname.indexOf(" -"))); } catch (Exception e) {}
							
							info.readChapters.add(chnr);
							
						}
						
						info.read = true;
						
					}
					
					try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					
					if (Settings.MAL_sync && MAL.canAuthenticate()) {
						
						new Thread(){@Override public void run(){ MAL.updateInList(info); }}.start();
					}
					
					closed = true;
					
				} else if (selected == 2) {
					
					info.lastChapter = -1;
					info.lastPage = -1;
					
					try { info.save(new File(Settings.mangaDir+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					try { info.save(new File(Settings.metaIn+"/"+info.title+"/_metadata/info.xml")); } catch (Exception e) {}
					
					if (Settings.MAL_sync && MAL.canAuthenticate()) {
						
						new Thread(){@Override public void run(){ MAL.updateInList(info); }}.start();
					}
					
				} else if (selected == 3) {
					
					openMangaInfoPanel(info);
					closed = true;
					
				} else if (selected == 4) {
					
					if (Main.mangadl.get() == null) {
						
						Main.mangadl.set(MangaDL.updateManga(info.title));
						
					} else {
						
						MangaDL.addToQueue("Updating "+info.title, new String[]{ "-d", "\""+info.title+"\"", "--noinput" });
					}
					
					closed = true;
					
				} else if (selected == 5) {
					
					focus = new PosterSelect(info){
						
						@Override
						public void onPosterChanged() {
							
							tis.list.checkForPosterChange();
							Mangas.posterChange();
						}
						
					};
					
				}
				
			}
			
		};
		components.add(cm);
		
	}
	
	private void openMangaInfoPanel(MangaInfo info) {
		
		MangaView tis = this;
		
		cm = new MangaInfoPanel(info, new File(Settings.metaIn+"/"+info.title+"/_metadata"), new String[]{ "Update", "Refresh", "Poster", "Remove" }){
			
			@Override
			public void onButton(String button) {
				button = button.toLowerCase().trim();
				
				if (button.startsWith("update")) {
					
					if (Main.mangadl.get() == null) {
						
						Main.mangadl.set(MangaDL.updateManga(info.title));
						
					} else {
						
						MangaDL.addToQueue("Updating "+info.title, new String[]{ "-d", "\""+info.title+"\"", "--noinput" });
					}
					
				} else if (button.startsWith("refresh")) {
					
					if (Main.mangadl.get() == null) {
						
						Main.mangadl.set(MangaDL.refreshManga(info.title));
						
					} else {
						
						MangaDL.addToQueue("Refreshing "+info.title, new String[]{ "-r", "\""+info.title+"\"" });
					}
					
				} else if (button.contains("poster")) {
					
					this.cm = new PosterSelect(info){
						
						@Override
						public void onPosterChanged() {
							
							tis.list.checkForPosterChange();
							Mangas.posterChange();
						}
						
					};
					
				} else if (button.contains("remove")) {
					
					this.cm = new Dialogbox("Removing Manga", "Are you sure you want to remove \""+info.title+"\" from your library?", new String[]{ "Yes", "No" }){
						
						@Override
						public void onButton(String button) {
							button = button.toLowerCase();
							
							if (button.equals("yes")) {
								
								if (Settings.mangaDir.length() < 2 || info.title.length() < 2) { return; }
								
								File dir = new File(Settings.mangaDir+"/"+info.title);
								
								if (dir.exists() && dir.isDirectory()) {
									
									Files.cleanseDir(dir);
									dir.delete();
								}
								
								if (Settings.metaIn.length() < 2) { return; }
								
								File mdir = new File(Settings.metaIn+"/"+info.title);
								
								if (mdir.exists() && mdir.isDirectory()) {
									
									Files.cleanseDir(mdir);
									mdir.delete();
								}
								
								HashMap<String, MangaInfo> mangas = Mangas.mangas.get();
								mangas.remove(info.title);
								Mangas.mangas.set(mangas);
								
								for (Iterator<MangaInfo> it = list.entries.iterator(); it.hasNext();) {
									MangaInfo nfo = it.next();
									
									if (nfo.title.equalsIgnoreCase(info.title)) { it.remove(); }
								}
								
								list.selected = FastMath.clampToRangeC(list.selected, 0, list.entries.size()-1);
								
								if (Settings.MAL_sync && MAL.canAuthenticate()) {
									
									new Thread(){
										
										@Override
										public void run() {
											
											MAL.putOnHold(info);
										}
										
									}.start();
									
								}
								
								cm.closed = true;
								
							}
							
							closed = true;
						}
						
					};
					
				}
				
			}
			
		};
		components.add(cm);
		
	}
	
	@Override
	public void clearScene() {
		super.clearScene();
		
		
		
	}
	
	public void clearScene(String ignoreTitle) {
		
		for (Component c : components) {
			
			if (c instanceof MangaList) {
				
				((MangaList)c).clear(ignoreTitle);
				
			} else {
				
				c.clear();
			}
			
		}
		
	}
	
	/*
	private Image loadPoster(String title, String poster) {
		
		File imgFile = new File(Settings.metaIn+"/"+title+"/_metadata/posters/"+poster);
		Files.waitOnFile(imgFile, 2);
		
		Image img = null;
		
		try {
			
			img = new Image(imgFile.getAbsolutePath());
			img.setFilter(Image.FILTER_LINEAR);
			img.setName(imgFile.getName());
			
		} catch (Exception e) {}
		
		return img;
	}*/
	
}
