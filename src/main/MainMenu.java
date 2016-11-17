package main;

import static main.Main.displayScale;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;
import visionCore.math.FastMath;

public class MainMenu extends Menu {
	
	
	public MenuList<String> list;
	
	
	public MainMenu() {
		
		this(0);
	}
	
	public MainMenu(int selected) {
		
		this.title = "MANGADDICT";
		this.titleX = (Display.getWidth() - Fonts.roboto.s48.getWidth(title)) * 0.5f;
		
		setBG(GUIRes.menubg);
		
		List<String> items = new ArrayList<String>();
		items.add("MANGA");
		items.add("GET NEW");
		items.add("SETTINGS");
		
		float eh = 160f * displayScale;
		float lh = eh * 3f;
		float ly = 150f * displayScale + (Display.getHeight() - 250f * displayScale - lh) * 0.5f;
		
		Rectangle listpane = new Rectangle(contentpanel.x, ly, contentpanel.width, lh);
		
		list = new MenuList<String>(items, listpane, eh, MenuList.MODE_VERTICAL, false) {
			
			@Override
			public void renderEntry(Graphics g, String entry, float x, float y, boolean selected, int ind) {
				
				float w = 512f * displayScale;
				
				g.setFont(Fonts.robotoBold.s36);
				g.setColor(Color.white);
				
				Image img = GUIRes.contextPanel;
				if (img != null) {
					
					float bx = x + (pane.width - w) * 0.5f;
					
					Image sub = img.getSubImage(0, 0, 32, img.getHeight());
					sub.draw(bx, y, sub.getWidth() * displayScale, entryHeight, Color.white.copy().setAlpha(0.95f));
					
					sub = img.getSubImage(32, 0, img.getWidth()-64, img.getHeight());
					sub.draw(bx + 32 * displayScale, y, (w - 64f * displayScale), entryHeight, Color.white.copy().setAlpha(0.95f));
					
					sub = img.getSubImage(img.getWidth()-32, 0, 32, img.getHeight());
					sub.draw(bx + (w - 32f * displayScale), y, 32f * displayScale, entryHeight, Color.white.copy().setAlpha(0.95f));
				}
				
				if (selected) {
					
					img = GUIRes.selected;
					
					int bx = (int)((x + (pane.width - w) * 0.5f) + 16f * displayScale);
					int by = (int)(y + 16f * displayScale);
					
					img.draw(bx, by, (int)(w - 32f * displayScale), (int)(entryHeight - 32f * displayScale),
							Menu.flavors[Settings.menu_flavor].copy().setAlpha(0.6f));
					
					if (displayScale == 1f) {
						
						g.setColor(Color.grayTone(136));
						g.fillRect(bx + 32, by, (int)w - 128, 1);
						g.setColor(Color.white);
					}
					
				}
				
				float ty = y + (entryHeight - g.getFont().getHeight("I")) * 0.5f;
				float tx = x + (pane.width - g.getFont().getWidth(entry)) * 0.5f;
				
				g.drawString(entry, (int)tx, (int)ty);
				
			}
			
			@Override
			public void onAction(String entry) {
				
				Scene scene = null;
				
				if (selected == 0) {
					
					MangaView mv = null;
					try { mv = new MangaView(Settings.menu_mangaMode, Mangas.lastRead); } catch (Exception e) {}
					
					scene = mv;
					
				} else if (selected == 1) {
					
					DownloadMenu m = null;
					try { m = new DownloadMenu(); } catch (Exception e) {}
					
					scene = m;
					
				} else if (selected == 2) {
					
					SettingsMenu m = null;
					try { m = new SettingsMenu(); } catch (Exception e) {}
					
					scene = m;
					
				}
				
				if (scene != null) {
					
					clearScene();
					Main.currentScene = scene;
					
				}
				
			}
			
		};
		list.drawSplitter = false;
		list.setFocus(selected);
		
		components.add(list);
		setFocus(list);
		
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		super.render(g);
		
		if (MAL.hasUser()) {
			
			MAL.renderProfile(g, 2);
		}
		
	}
	
}
