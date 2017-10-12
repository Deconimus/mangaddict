package main;

import static main.Main.displayScale;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageStruct;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import components.Component;
import components.ContextMenu;
import components.InputPanel;
import components.MenuList;
import components.PosterPanel;
import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.util.Files;

public class ChapterView extends Menu {

	
	private AtomicReference<ImageStruct> pstrStruct;
	
	public MangaInfo mangaInfo;
	public Image mangaPoster;
	
	public Rectangle posterpane;
	public PosterPanel posterPanel;
	
	public MenuList<File> chapterList;
	
	public Component cm;
	
	public ChapterView tis;
	
	
	public ChapterView(String title) {
		
		this(title, null);
	}
	
	public ChapterView(String title, Image poster) {
		
		this.tis = this;
		
		setBG(GUIRes.menubg);
		this.title = title.toUpperCase()+" CHAPTERS";
		
		File mangadir = new File(Settings.mangaDir+"/"+title);
		
		this.mangaInfo = Mangas.get(title);
		this.mangaPoster = poster;
		
		posterpane = new Rectangle(0f, 150f * displayScale, GUIRes.posterPanel.getWidth() * displayScale, GUIRes.posterPanel.getHeight() * displayScale);
		
		float pad = 25f * displayScale;
		
		float x = (int)((Display.getWidth() - (posterpane.width + pad + contentpanel.width)) * 0.5f);
		posterpane.setX(x);
		contentpanel.setX(posterpane.getMaxX() + pad);
		
		if (Fonts.roboto.s48.getWidth(this.title) > contentpanel.width) {
			
			titleX = posterpane.x;
		}
		
		List<File> chapters = Files.getFiles(mangadir, f -> f.isDirectory() && Character.isDigit(f.getName().charAt(0)));
		Collections.sort(chapters);
		
		if (new File(chapters.get(chapters.size()-1).getAbsolutePath()+"/lock").exists()) {
			
			chapters.remove(chapters.size()-1);
		}
		
		int lastreadInd = 0;
		
		for (int i = 0; i < chapters.size() && mangaInfo.lastChapter != -1; i++) {
			
			String chname = chapters.get(i).getName();
			double chnr = -1.0;
			try { chnr = Double.parseDouble(chname.substring(0, chname.indexOf(" -"))); } catch (Exception e) {}
			
			if (chnr == mangaInfo.lastChapter) {
				
				lastreadInd = i;
				break;
			}
		}
		
		Rectangle listpanel = new Rectangle(contentpanel.x + 30f, contentpanel.y + 20f, contentpanel.width - 60f, contentpanel.height - 40f);
		
		chapterList = new MenuList<File>(chapters, listpanel, 52f * displayScale){
			
			@Override
			public void renderEntry(Graphics g, File entry, float x, float y, boolean selected, int ind) {
				
				double chap = 0;
				
				try {
					
					chap = Double.parseDouble(entry.getName().substring(0, entry.getName().indexOf(" -")));
					
				} catch (Exception e) {}
				
				if (mangaInfo.lastChapter == chap) {
					
					float x1 = pane.x + pane.width - (20f + GUIRes.reading.getWidth() + 8f) * displayScale;
					float y1 = y + (entryHeight - GUIRes.reading.getHeight() * displayScale) * 0.5f;
					
					GUIRes.reading.draw((int)x1, (int)y1, (int)(GUIRes.reading.getWidth() * displayScale + 0.5f),
										(int)(GUIRes.reading.getHeight() * displayScale + 0.5f), Color.white.copy().setAlpha(0.8f));
					
				} else if (mangaInfo.readChapters.contains(chap)) {
					
					float x1 = pane.x + pane.width - (20f + GUIRes.tick.getWidth() + 8f) * displayScale;
					float y1 = y + (entryHeight - GUIRes.tick.getHeight() * displayScale) * 0.5f;
					
					GUIRes.tick.draw((int)x1, (int)y1, (int)(GUIRes.tick.getWidth() * displayScale + 0.5f), 
									 (int)(GUIRes.tick.getHeight() * displayScale + 0.5f), Color.white.copy().setAlpha(0.8f));
					
				}
				
				super.renderEntry(g, entry, x, y, selected, ind);
			}
			
			@Override
			public void onAction(File entry) {
				
				String chname = entry.getName();
				double chnr = -1.0;
				try { chnr = Double.parseDouble(chname.substring(0, chname.indexOf(" -"))); } catch (Exception e) {}
				
				File file = null;
				
				if (chnr == mangaInfo.lastChapter && mangaInfo.lastPage != -1) {
					
					String fn = mangaInfo.lastPage+"";
					for (int i = 0, l = fn.length(); i < 3 - l; i++) { fn = "0"+fn; }
					
					file = new File(entry.getAbsolutePath()+"/"+fn+".jpg");
					if (!file.exists()) { file = new File(entry.getAbsolutePath()+"/"+fn+".png"); }
					if (!file.exists()) { file = getFirstPage(entry); }
					
				} else { file = getFirstPage(entry); }
				
				try {
					
					Scene sc = new ImageView(file);
					
					if (sc != null) {
						
						clearScene();
						Main.currentScene = sc;
					}
				} catch (Exception e) { e.printStackTrace(); }
				
			}
			
			private File getFirstPage(File chdir) {
				
				List<File> fl = Files.getFiles(chdir, f -> Character.isDigit(f.getName().charAt(0)) && ImageFormats.isSupported(f));
				Collections.sort(fl);
				
				if (fl == null || fl.isEmpty()) { return null; }
				
				return fl.get(0);
			}
			
		};
		
		chapterList.setFocus(lastreadInd);
		
		components.add(chapterList);
		setFocus(chapterList);
		
		if (mangaPoster == null) {
		
			pstrStruct = new AtomicReference<ImageStruct>();
			
			Thread t = new Thread(){
				
				@Override
				public void run() {
					
					try {
						
						File posterFile = new File(Settings.metaIn+"/"+mangaInfo.title+"/_metadata/posters/"+mangaInfo.poster);
						
						//ImageStruct data = new ImageStruct(posterFile, Image.FILTER_LINEAR);
						ImageStruct data = TJUtil.getImageStruct(posterFile);
						
						if (data != null) {
							
							pstrStruct.set(data);
						}
						
					} catch (Exception e) {}
					
				}
				
			};
			t.setDaemon(true);
			t.start();
			
		}
		
		posterPanel = new PosterPanel(mangaInfo, posterpane, mangaPoster);
		components.add(posterPanel);
		
		float width = 420f;
		float height = 360f;
		
	}
	
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);

		if (mangaPoster == null) {
		
			ImageStruct imgstr = pstrStruct.get();
			if (imgstr != null) {
				
				mangaPoster = new Image(imgstr);
				posterPanel.poster = mangaPoster;
			}
		}
		
	}
	
	
	@Override
	public void mouseInput(int button, boolean pressed) {
		
		if (pressed && button == 3) {
			
			exitView();
		}
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		if (cm != null && !cm.closed) { cm.handleInput(key, c, pressed); return; }
		
		if (pressed) {
			
			if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
				
				if (getFocus() == chapterList) {
				
					exitView();
					
				} else if (getFocus() instanceof ContextMenu) {
					
					getFocus().onClosing();
					
				}
				
			} else if ((key == Input.KEY_E && (Main.altDown || Main.ctrlDown)) || key == Input.KEY_F1) {
				
				openContextMenu();
			}
			
		}
		
		super.handleInput(key, c, pressed);
		
	}
	
	private void openContextMenu() {
		
		String chname = chapterList.entries.get(chapterList.selected).getName();
		double chnr = -1.0;
		try { chnr = Double.parseDouble(chname.substring(0, chname.indexOf(" -"))); } catch (Exception e) {}
		
		final double chapterNr = chnr;
		
		final boolean read = mangaInfo.readChapters.contains(chnr);
		String mark = read ? "unread" : "read";
		
		String open = (chnr == mangaInfo.lastChapter) ? "Continue" : "Start"; 
		
		float w = 420f * displayScale;
		float h = 220f * displayScale;
		
		Rectangle centerIn = new Rectangle(contentpanel.x + 30f * displayScale, contentpanel.y + 20f * displayScale + (chapterList.selected - chapterList.camera + 1)
												* chapterList.entryHeight, contentpanel.width - 60f * displayScale, chapterList.entryHeight - 40f * displayScale);
		
		List<String> items = new ArrayList<String>();
		
		items.add(open+" reading");
		items.add("Mark as "+mark);
		items.add("Mark as "+mark+" (and prev.)");
		
		if (mangaInfo.lastChapter == chapterNr) {
			
			items.add("Remove Bookmark");
		}
		
		items.add("Rename Chapter");
		
		cm = new ContextMenu(centerIn, items.toArray(new String[items.size()])) {
			
			@Override
			public void onAction(int selected) {
				
				if (selected == 0) {
					
					chapterList.onAction(chapterList.entries.get(chapterList.selected));
					
				} else if (selected == 1) {
					
					if (!read) { mangaInfo.readChapters.add(chapterNr); }
					else { mangaInfo.readChapters.remove(chapterNr); }
					
					saveInfo();
					
				} else if (selected == 2) {
					
					if (!read) {
						
						markAsRead(0, chapterList.selected);
						
					} else {
						
						for (Iterator<Double> it = mangaInfo.readChapters.iterator(); it.hasNext();) {
							Double d = it.next();
							
							if (d.doubleValue() <= chapterNr) {
								
								it.remove();
							}
							
						}
						
					}
					
					saveInfo();
					
				} else if (selected == 3 && items.size() == 5) {
					
					mangaInfo.lastChapter = -1;
					mangaInfo.lastPage = -1;
					
					saveInfo();
					
				} else if ((selected == 3 && items.size() == 4) || (selected == 4 && items.size() == 5)) {
					
					String name = chapterList.entries.get(chapterList.selected).getName();
					
					String prefix = name.substring(0, name.indexOf("-")).trim();
					name = name.substring(name.indexOf('-')+1).trim();
					
					final String nm = name;
					
					InputPanel sp = new InputPanel("Rename Chapter"){
						
						@Override
						public void onAction(String search) {
							search = MangaInfo.cleanTitle(search);
							search = search.replace("/", "").replace("\\", "").replace("  ", " ").trim();
							
							if (!search.equals(nm)) {
							
								File src = chapterList.entries.get(chapterList.selected);
								File dst = new File(src.getParentFile().getAbsolutePath().replace("\\", "/")+"/"+prefix+" - "+search);
								
								Files.moveDir(src, dst);
								
								File renamed = new File(dst.getAbsolutePath().replace("\\", "/")+"/.renamed");
								if (!renamed.exists()) { try { renamed.createNewFile(); } catch (Exception e) {} }
								
								chapterList.entries.set(chapterList.selected, dst);
							}
							
							tis.setFocus(chapterList);
						}
						
						@Override
						public void onCancel() {
							
							tis.setFocus(chapterList);
						}
						
					};
					
					sp.txt = name;
					
					components.add(sp);
					setFocus(sp);
				}
				
				closed = true;
				
			}
			
		};
		components.add(cm);
		
	}
	
	private void markAsRead(int start, int end) {
		
		for (int i = start; i <= end; i++) {
			
			String chname = chapterList.entries.get(i).getName();
			double chnr = -1.0;
			try { chnr = Double.parseDouble(chname.substring(0, chname.indexOf(" -"))); } catch (Exception e) {}
			
			mangaInfo.readChapters.add(chnr);
			
		}
		
	}
	
	private void saveInfo() {
		
		try { mangaInfo.save(new File(Settings.mangaDir+"/"+mangaInfo.title+"/_metadata/info.xml")); } catch (Exception e) {}
		try { mangaInfo.save(new File(Settings.metaIn+"/"+mangaInfo.title+"/_metadata/info.xml")); } catch (Exception e) {}
	}
	
	private void exitView() {
		
		Main.currentScene.clearScene();
		Main.currentScene = new MangaView(Settings.menu_mangaMode, mangaInfo.title);
	}
	
	@Override
	public void clearScene() {
		super.clearScene();
		
		try { mangaPoster.destroy(); } catch (Exception | Error e) {}
		
	}
	
}
