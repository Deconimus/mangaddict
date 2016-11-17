package main;

import static main.Main.displayScale;

import java.io.File;
import java.util.List;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;

public class MenuList<T> extends Component {

	public static final int MODE_VERTICAL = 0, MODE_HORIZONTAL = 1, MODE_TABLE = 2;
	
	public List<T> entries;
	public Rectangle pane;
	public float entryWidth, entryHeight, padH;
	
	public int mode, camera, selected;
	
	private static final int KEY_HOLD = 450, KEY_FIRE_WAIT = 50;
	private int downHeld, upHeld;
	
	private int cutFromName;
	
	public boolean onScrollbar, drawPanels, drawSplitter, drawEntriesCentered;
	
	public MenuList(List<T> entries, Rectangle pane, float entryHeight) {
		
		this(entries, pane, entryHeight, MODE_VERTICAL);
	}
	
	public MenuList(List<T> entries, Rectangle pane, float entryHeight, int mode) {
		
		this(entries, pane, entryHeight, mode, true);
	}
	
	public MenuList(List<T> entries, Rectangle pane, float entryHeight, int mode, boolean drawPanels) {
		
		this(entries, pane, new Vector2f(entryHeight, entryHeight), mode, drawPanels);
	}
	
	public MenuList(List<T> entries, Rectangle pane, Vector2f entrySize, int mode, boolean drawPanels) {
		
		this.mode = mode;
		this.drawPanels = drawPanels;
		this.drawSplitter = true;
		this.drawEntriesCentered = false;
		
		this.entries = entries;
		this.pane = pane;
		
		this.entryWidth = entrySize.x;
		this.entryHeight = entrySize.y;
		
		this.camera = 0;
		
		this.downHeld = -1;
		this.upHeld = -1;
		
		this.cutFromName = 0;
		
		this.padH = 25f * displayScale;
		
		this.onScrollbar = false;
		
		try {
			
			T last = entries.get(entries.size()-1);
			
			if (last instanceof File) {
				File f = (File)last;
				
				String fnrstr = f.getName().substring(0, f.getName().indexOf(" -"));
				
				double d = Math.floor(Double.parseDouble(fnrstr));
				String dstr = d+"";
				
				if (dstr.contains(".")) { dstr = dstr.substring(0, dstr.indexOf(".")); }
				if (fnrstr.contains(".")) { fnrstr = fnrstr.substring(0, fnrstr.indexOf(".")); }
				
				cutFromName = fnrstr.length() - dstr.length();
				
			}
			
		} catch (Exception e) {}
		
		if (mode == MODE_HORIZONTAL) {
			
			int n = (int)((pane.width + padH) / (entryWidth + padH));
			
			padH = (pane.width - n * entryWidth) / (float)(n-1);
		}
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (downHeld >= 0) {
			
			downHeld += delta;
			
			if (downHeld > KEY_HOLD) {
				
				down(false);
				downHeld = KEY_HOLD - KEY_FIRE_WAIT;
			}
			
		}
		
		if (upHeld >= 0) {
			
			upHeld += delta;
			
			if (upHeld > KEY_HOLD) {
				
				up(false);
				upHeld = KEY_HOLD - KEY_FIRE_WAIT;
			}
			
		}
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		g.setColor(Color.white);
		
		int maxEntries = getMaxEntries();
		
		if (entries.isEmpty()) {
			
			g.setFont(Fonts.roboto.s30);
			g.drawString("Nothing here.", (int)(pane.x + 0.5f), (int)(pane.y + 0.5f));
		}
		
		if (drawPanels && (mode == MODE_VERTICAL || mode == MODE_TABLE)) {
			
			Color transp = new Color(1f, 1f, 1f, 0.85f);
			
			GUIRes.contentPanel.draw((int)pane.x - 30f, (int)pane.y - 20f, pane.width + 60f, pane.height + 40f, transp);
		}
		
		if (mode == MODE_VERTICAL) {
		
			Color transp = new Color(1f, 1f, 1f, 0.6f);
			
			if (drawSplitter) {
			
				GUIRes.splitter.startUse();
				
				float space = pane.height - maxEntries * entryHeight;
				
				for (int i = camera, end = Math.min(camera + maxEntries, entries.size()); i < ((space < 20f * displayScale || end == entries.size()) ? end-1 : end); i++) {
					
					GUIRes.splitter.drawEmbedded((int)(-30f * displayScale + pane.x), (int)(pane.y + entryHeight * (i - camera + 1) - 1f), 
							(int)(pane.width + 40f * displayScale), (int)(GUIRes.splitter.getHeight() * displayScale + 0.5f), transp);
				}
				
				GUIRes.splitter.endUse();
				
			}
			
		}
		
		if (mode == MODE_VERTICAL || mode == MODE_TABLE) {
			
			Color transp = new Color(1f, 1f, 1f, 0.6f);
			
			if (hasScrollbar()) {
				
				GUIRes.scrollbar.startUse();
				
				GUIRes.scrollbar.getSubImage(0, 0, 20, 12).draw((int)(pX + pane.x + pane.width - 20f * displayScale), 
														 (int)(pY + pane.y), 20f * displayScale, 12f * displayScale, transp);
	
				GUIRes.scrollbar.getSubImage(0, 12, 20, 16).draw((int)(pX + pane.x + pane.width - 20f * displayScale), 
							(int)(pY + pane.y + 12f * displayScale), 20f * displayScale, pane.height - 24f * displayScale, transp);
				
				GUIRes.scrollbar.getSubImage(0, 40-12, 20, 12).draw((int)(pX + pane.x + pane.width - 20f * displayScale), 
								(int)(pY + pane.y + pane.height - 12f * displayScale), 20f * displayScale, 12f * displayScale, transp);
				
				float y = (pane.height / (float)entries.size()) * camera;
				float h = (pane.height / (float)entries.size()) * getMaxEntries();
				
				if (mode == MODE_TABLE) {
					
					y = (pane.height / FastMath.ceil((float)entries.size() / (float)getEntriesH())) * camera;
					h = (pane.height / FastMath.ceil((float)entries.size() / (float)getEntriesH())) * getEntriesV();
				}
				
				float ha = Math.min(h * 0.5f, 12f * displayScale);
				
				transp = Color.grayTone(0.7f).setAlpha(0.6f);
				if (onScrollbar) { transp = Menu.flavors[Settings.menu_flavor].copy().setAlpha(0.6f); }
				
				GUIRes.scrollbar.getSubImage(20, 0, 20, 12).draw(pX + pane.x + pane.width - 20f * displayScale, pY + pane.y + y,
															20f * displayScale, ha, transp);
				
				if (h - ha * 2f > 0.00001f) {
					
					GUIRes.scrollbar.getSubImage(20, 12, 20, 16).draw(pX + pane.x + pane.width - 20f * displayScale,
																pY + pane.y + y + ha, 20f * displayScale, h - 2f * ha, transp);
				}
				
				GUIRes.scrollbar.getSubImage(20, 40-12, 20, 12).draw(pX + pane.x + pane.width - 20f * displayScale, pY + pane.y + y + (h - ha),
															20f * displayScale, ha, transp);
				
				GUIRes.scrollbar.endUse();
				
			}
			
		}
		
		g.setFont(Fonts.roboto.s30);
		
		if (mode == MODE_VERTICAL || mode == MODE_HORIZONTAL) {
		
			for (int i = Math.max(camera, 0), end = Math.min(camera + maxEntries, entries.size()); i < end; i++) {
				
				float x = pX + pane.x;
				float y = pY + pane.y;
				
				if (mode == MODE_VERTICAL) { y += entryHeight * (i - camera); }
				else if (mode == MODE_HORIZONTAL) { x += (entryWidth + padH) * (i - camera); }
				
				if (mode != MODE_HORIZONTAL || i != selected) {
					
					renderEntry(g, entries.get(i), x, y, i == selected && !onScrollbar, i);
				}
			}
			
		} else if (mode == MODE_TABLE) {
			
			int entriesH = Math.min((int)(pane.width / entryWidth), entries.size());
			int entriesV = (int)(pane.height / entryHeight);
			
			float padH = (float)(pane.width - 20f - entryWidth * getEntriesH()) / (float)(entriesH + 1f);
			//float padV = (float)(pane.height - entryHeight * entriesV) / (float)(entriesV + 1f);
			
			for (int i = Math.max(camera * entriesH, 0), end = Math.min(camera * entriesH + entriesH * entriesV, entries.size()); i < end; i++) {
				
				int indX = (i % entriesH);
				int indY = (int)((float)i / (float)entriesH);
				
				float x = pX + pane.x + padH + indX * (entryWidth + padH);
				float y = pY + pane.y + (indY - camera) * entryHeight;
				
				renderEntry(g, entries.get(i), x, y, i == selected && !onScrollbar, i);
			}
			
		}
		
		if (mode == MODE_HORIZONTAL) {
			
			float aw = GUIRes.arrows.getWidth() * displayScale;
			float ah = GUIRes.arrows.getHeight() * displayScale;
			
			Color atransp = Menu.flavors[Settings.menu_flavor].copy().setAlpha(0.9f);
			
			if (camera > 0) {
				
				GUIRes.arrows.getFlippedCopy(true, false)
							 .draw(pX + pane.x - padH - aw, pY + pane.y + (pane.height - ah) * 0.5f, displayScale, atransp);
			}
			
			if (camera + getMaxEntries() < entries.size()) {
				
				GUIRes.arrows.draw(pX + pane.x + pane.width + padH, pY + pane.y + (pane.height - ah) * 0.5f, displayScale, atransp);
			}
			
			if (!entries.isEmpty()) {
				
				renderEntry(g, entries.get(selected), pX + pane.x + (entryWidth + padH) * (selected - camera), pY + pane.y, true, selected);
			}
		}
		
	}
	
	public void renderEntry(Graphics g, T entry, float x, float y, boolean selected, int ind) {
		
		if (selected) {
			
			float alpha = (hasFocus) ? 0.6f : 0.2f;
			
			GUIRes.selected.draw((int)(x - 20f * displayScale), (int)y, (int)((pane.width + 20f * displayScale) + 0.5f),
								 (int)(entryHeight), Menu.flavors[Settings.menu_flavor].copy().setAlpha(alpha));
		}
		
		float ty = y + (entryHeight - g.getFont().getHeight("I")) * 0.5f;
		
		String s = "";
		
		if (entry instanceof String) { s = (String)entry; } 
		else if (entry instanceof File) { s = ((File)(entry)).getName().substring(cutFromName); }
		else if (entry instanceof MangaInfo) { s = ((MangaInfo)entry).title; }
		
		Color tmpCol = g.getColor();
		
		if (s.startsWith("//")) {
			
			s = s.substring(2);
			
			g.setColor(Color.gray);
			
		}
		
		while (g.getFont().getWidth(s) > pane.width - 20f * displayScale) { s = s.substring(0, s.length()-4); s += "..."; }
		
		float tx = x;
		if (drawEntriesCentered) { tx += (pane.width - 20f - g.getFont().getWidth(s)) * 0.5f; }
		
		g.drawString(s, (int)tx, (int)ty);
		
		g.setColor(tmpCol);
		
	}
	
	@Override
	public void mouseWheelMoved(int change) {
		
		if (change >= 0) { up(); } 
		else { down(); }
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		super.handleInput(key, c, pressed);
		
		if (pressed) {
			
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				
				cycleEntriesChar(c);
			}
			
			if (mode == MODE_VERTICAL) {
				
				verticalNav(key);
				
			} else if (mode == MODE_HORIZONTAL) {
				
				horizontalNav(key);
				
			} else if (mode == MODE_TABLE) {
				
				tableNav(key);
				
			}
			
			if (key == Input.KEY_ENTER) {
				
				if (!onScrollbar) {
					
					onAction(entries.get(selected));
				}
				
			} else if (key == Input.KEY_HOME) {
				
				setFocus(0);
				
			} else if (key == Input.KEY_END) {
				
				setFocus(entries.size()-1);
				
			}
			
		} else {
			
			if (key == Input.KEY_DOWN || key == Input.KEY_RIGHT) {
				
				downHeld = -1;
				
			} else if (key == Input.KEY_UP || key == Input.KEY_LEFT) {
				
				upHeld = -1;
				
			}
			
		}
		
	}
	
	private void verticalNav(int key) {
		
		if (key == Input.KEY_DOWN) {
			
			down();
			downHeld = 0;
			
		} else if (key == Input.KEY_UP) {
			
			up();
			upHeld = 0;
			
		} else if (key == Input.KEY_RIGHT) {
			
			if (onScrollbar || !hasScrollbar()) { onRightBorder(); }
			else if (hasScrollbar()) { onScrollbar = true; }
			
		} else if (key == Input.KEY_LEFT) {
			
			if (!onScrollbar || !hasScrollbar()) { onLeftBorder(); }
			onScrollbar = false;
			
		}
		
	}
	
	private void horizontalNav(int key) {
		
		if (key == Input.KEY_RIGHT) {
			
			down();
			downHeld = 0;
			
		} else if (key == Input.KEY_LEFT) {
			
			up();
			upHeld = 0;
			
		}
		
	}
	
	private void tableNav(int key) {
		
		int entriesH = getEntriesH();
		int entriesV = getEntriesV();
		
		int selX = selected % entriesH;
		int selY = (int)((float)selected / (float)entriesH);
		
		if (key == Input.KEY_RIGHT) {
			
			if (!(onScrollbar || (!hasScrollbar() && selX >= entriesH-1)) || !onRightBorder()) {
				
				if ((selX < entriesH-1 && selected < entries.size()-1) || !hasScrollbar()) {
					
					tableRight(true);
					
				} else {
					
					onScrollbar = true;
				}
				
			}
			
		} else if (key == Input.KEY_LEFT) {
			
			if (!onScrollbar) {
				
				if (!(selX <= 0) || !onLeftBorder()) {
					
					tableLeft(true);
				}
				
			} else { onScrollbar = false; }
			
		} else if (key == Input.KEY_DOWN) {
			
			down(true);
			downHeld = 0;
			
		} else if (key == Input.KEY_UP) {
			
			up(true);
			upHeld = 0;
			
		}
		
	}
	
	private void cycleEntriesChar(char c) {
		
		c = Character.toLowerCase(c);
		
		outer:
		for (int cycle = 0; cycle < 2; cycle++) {
		
			int startInd = (cycle == 0) ? selected+1 : 0;
			
			for (int i = startInd, size = entries.size(); i < size; i++) {
				T entry = entries.get(i);
				
				String name = "";
				if (entry instanceof String) { name = (String)entry; }
				else if (entry instanceof File) { name = ((File)entry).getName(); }
				else if (entry instanceof MangaInfo) { name = ((MangaInfo)entry).title; }
				
				name = name.toLowerCase().replaceAll("^the ", "").trim();
				
				if (name.startsWith(c+"")) {
					
					setFocus(i);
					break outer;
				}
				
			}
			
		}
		
	}
	
	public void onAction(T entry) {
		
		
		
	}
	
	public boolean onLeftBorder() {
		
		return false;
	}
	
	public boolean onRightBorder() {
		
		return false;
	}
	
	public void tableLeft(boolean byButton) {
		
		if (onScrollbar) {
			
			int selX = selected % getEntriesH();
			int selY = (int)((float)selected / (float)getEntriesH());
			
			selY = Math.max(selY - ((selY - camera) + getEntriesV()), 0);
			
			selected = Math.max(selY * getEntriesH() + selX, selY * getEntriesH() + (getEntriesH()-1));
			
		} else { selected -= 1; }
		
		if (byButton && !onScrollbar) {
			
			if (selected < 0) { setFocus(entries.size()-1); }
			
		} else { selected = Math.max(selected, 0); }
		
		
		camUp();
		
	}
	
	public void tableRight(boolean byButton) {
		
		if (onScrollbar) {
			
			int selX = selected % getEntriesH();
			int selY = (int)((float)selected / (float)getEntriesH());
			
			selY = Math.min(selY + (camera + getMaxEntries()-1 - selY + getEntriesV()), FastMath.ceilInt((float)entries.size() / (float)getEntriesH()));
			
			selected = Math.min(selY * getEntriesH() + selX, entries.size()-1);
			
		} else { selected += 1; }
		
		if (byButton && !onScrollbar) {
			
			if (selected > entries.size()-1) { setFocus(0); }
			
		} else { selected = Math.min(selected, entries.size()-1); }
		
		
		camDown();
		
	}
	
	public void up() { up(true); }
	protected void up(boolean byButton) {
		
		if (mode == MODE_TABLE) {
			
			int entriesH = getEntriesH();
			
			int selX = selected % entriesH;
			int selY = (int)((float)selected / (float)entriesH);
			
			if (onScrollbar) {
				
				selY = Math.max(selY - ((selY - camera) + getEntriesV()), 0);
				
				selected = Math.max(selY * getEntriesH() + selX, selY * getEntriesH() + (getEntriesH()-1));
				
			} else {
				
				selected = (selY - 1) * entriesH + selX;
				
				if (selected < 0) { 
					
					if (byButton) {
					
						setFocus(entries.size()-1);
						
					} else { setFocus(0); }
					
				}
				
			}
			
		} else {
		
			selected -= onScrollbar ? (selected - camera) + getMaxEntries() : 1;
			
		}
		
		if (byButton && !onScrollbar) {
			
			if (selected < 0) { setFocus(entries.size()-1); }
			
		} else { selected = Math.max(selected, 0); }
		
		
		camUp();
		
	}
	
	public void down() { down(true); }
	protected void down(boolean byButton) {
		
		if (mode == MODE_TABLE) {
			
			int entriesH = getEntriesH();
			
			int selX = selected % entriesH;
			int selY = (int)((float)selected / (float)entriesH);
			
			if (onScrollbar) {
				
				selY = Math.min(selY + (camera + getMaxEntries()-1 - selY + getEntriesV()), FastMath.ceilInt((float)entries.size() / (float)getEntriesH()));
				
				selected = Math.min(selY * getEntriesH() + selX, entries.size()-1);
				
			} else { 
				
				int maxY = FastMath.ceilInt((float)entries.size() / (float)getEntriesH());
				
				selected = (selY + 1) * entriesH + selX;
				
				if (selected >= entries.size()) { 
					
					if (!byButton || selY + 1 < maxY) {
						
						setFocus(entries.size()-1);
						
					} else { setFocus(0); }
					
				}
				
			}
			
		} else {
		
			selected += onScrollbar ? (camera + getMaxEntries()-1) - selected + getMaxEntries() : 1;
		}
		
		if (byButton && !onScrollbar) {
			
			if (selected > entries.size()-1) { setFocus(0); }
			
		} else { selected = Math.min(selected, entries.size()-1); }
		
		
		camDown();
		
	}
	
	protected void camUp() {
		
		if (mode == MODE_TABLE) {
			
			int selY = (int)((float)selected / (float)getEntriesH());
			
			if (camera >= selY) {
				
				camera = selY;
			}
			
			camera = FastMath.clampToRange(camera, 0, Math.max(FastMath.ceilInt((float)entries.size() / (float)getEntriesH()) - getEntriesV(), 0));
			
		} else {
		
			if (camera >= selected) { camera = selected; }
			
			camera = FastMath.clampToRange(camera, 0, Math.max(entries.size() - getMaxEntries(), 0));
			
		}
		
	}
	
	protected void camDown() {
		
		if (mode == MODE_TABLE) {
			
			int selY = (int)((float)selected / (float)getEntriesH());
			
			if (camera + (getEntriesV()-1) < selY) {
				
				camera = Math.max(selY - (getEntriesV()-1), 0);
			}
			
			camera = FastMath.clampToRange(camera, 0, Math.max(FastMath.ceilInt((float)entries.size() / (float)getEntriesH()) - getEntriesV(), 0));
			
		} else {
		
			if (camera + (getMaxEntries()-1) < selected) { camera = Math.max(selected - (getMaxEntries()-1), 0); }
			
			camera = FastMath.clampToRange(camera, 0, Math.max(entries.size() - getMaxEntries(), 0));
			
		}
		
	}
	
	public void setFocus(int index) {
		
		if (mode == MODE_TABLE) {
			
			camera = FastMath.clampToRangeC(index / getEntriesH(), 0, Math.max(FastMath.ceilInt((float)entries.size() / (float)getEntriesH()) - getEntriesV(), 0));
			
		} else {
			
			camera = FastMath.clampToRangeC(index - 2, 0, Math.max(entries.size()-getMaxEntries(), 0));
		}
		
		selected = FastMath.clampToRangeC(index, 0, entries.size()-1);
		
	}
	
	private int getEntriesH() {
		
		return (int)(pane.width / entryWidth);
	}
	
	private int getEntriesV() {
		
		return (int)(pane.height / entryHeight);
	}
	
	protected int getMaxEntries() {
		
		if (mode == MODE_HORIZONTAL) {
			
			return (int)((pane.width + padH) / (entryHeight + padH) + 0.5f);
		}
		
		if (mode == MODE_TABLE) {
			
			return (int)(pane.height / entryHeight);
		}
		
		return (int)(pane.height / entryHeight);
	}
	
	public boolean hasScrollbar() {
		
		if (mode == MODE_TABLE) {
			
			return entries.size() / getEntriesH() > getEntriesV();
		}
		
		return entries.size() > getMaxEntries();
	}
	
}
