package main;

import static main.Main.displayScale;

import java.io.File;
import java.util.ArrayList;
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
import visionCore.util.ArrayUtils;
import visionCore.util.Files;

public class MangaInfoPanel extends Component {

	
	public static final float WIDTH = 1024f, HEIGHT = 830f;
	public static final float POSTER_WIDTH = 329f, POSTER_HEIGHT = 512f;
	
	
	public MangaInfo info;
	public Image poster;
	public File metadir;
	
	public Rectangle pane;
	
	public MenuList<String> buttons;
	
	public Component cm;
	
	public float rot;
	
	
	public MangaInfoPanel(MangaInfo info, File metadir, String[] buttons) {
		
		this.info = info;
		this.poster = null;
		
		this.metadir = metadir;
		
		this.pane = new Rectangle((int)((Display.getWidth() - WIDTH * displayScale) * 0.5f), 
								  (int)(150f * displayScale + (Display.getHeight() - 250f * displayScale - HEIGHT * displayScale) * 0.5f), 
								  (int)(WIDTH * displayScale), (int)(HEIGHT * displayScale));
		
		float bw = 0f;
		for (String str : buttons) {
			
			bw = Math.max(bw, Fonts.robotoBold.s30.getWidth(str) + 80f * displayScale);
		}
		
		float bsx = (bw + 25f * displayScale) * buttons.length - 25f * displayScale;
		float offx = (pane.width - bsx) * 0.5f;
		
		MangaInfoPanel tis = this;
		
		Rectangle listpane = new Rectangle(pane.x + offx, pane.y + pane.height - 72f * displayScale, pane.width - 2f * offx, 60f * displayScale);
		
		List<String> bs = new ArrayList<String>();
		for (String s : buttons) { bs.add(s); }
		
		this.buttons = new MenuList<String>(bs, listpane, new Vector2f(bw, 52f * displayScale), MenuList.MODE_HORIZONTAL, false){
			
			@Override
			public void renderEntry(Graphics g, String entry, float x, float y, boolean selected, int i) {
				
				g.setFont(Fonts.robotoBold.s30);
				
				g.setColor(Color.grayTone(32).setAlpha(0.925f));
				g.fillRect((int)x, (int)y, (int)entryWidth, (int)entryHeight);
				
				g.setColor(Color.grayTone(160));
				
				if (Mangas.get(info.title) != null && entry.toLowerCase().trim().equals("download")) {
					
					g.setColor(Color.grayTone(120));
				}
				
				if (selected) {
					
					GUIRes.buttonSelected.draw((int)x, (int)y, (int)entryWidth, (int)entryHeight, Menu.flavors[Settings.menu_flavor].copy());
				}
				
				g.drawRect((int)x, (int)y, (int)entryWidth, (int)entryHeight, 1);
				
				float sx = x + (entryWidth - g.getFont().getWidth(entry)) * 0.5f;
				float sy = y + (entryHeight - g.getFont().getHeight("I")) * 0.5f;
				
				g.setColor(Color.grayTone((selected) ? 255 : 200));
				
				if (Mangas.get(info.title) != null && entry.toLowerCase().trim().equals("download")) {
					
					g.setColor(Color.grayTone(120));
				}
				
				g.drawString(entry, (int)sx, (int)sy);
				
			}
			
			@Override
			public void onAction(String entry) {
				entry = entry.trim().toLowerCase();
				
				if (entry.equals("cancel")) {
					
					tis.onClosing();
					tis.closed = true;
					
				} else if (entry.equals("download") && Mangas.get(info.title) == null) {
					
					if (Main.mangadl.get() == null || Main.mangadl.get().finished.get()) {
						
						Main.mangadl.set(MangaDL.downloadManga(info.title));
						
						if (Settings.MAL_sync && MAL.canAuthenticate()) {
						
							new Thread(){
								
								@Override
								public void run() {
									
									MAL.updateInList(info);
								}
								
							}.start();
							
						}
						
					} else {
						
						MangaDL.addToQueue("Downloading "+info.title, new String[]{ "-d", "\""+info.title+"\"", "--noinput" });
						
					}
					
					tis.onClosing();
					tis.closed = true;
					
				} else if (entry.equals("plan to read")) {
					
					if (Settings.MAL_sync && MAL.canAuthenticate()) {
					
						new Thread(){
							
							@Override
							public void run() {
								
								MAL.updateInList(info);
							}
							
						}.start();
						
					}
					
					tis.onClosing();
					tis.closed = true;
					
				} else {
					
					onButton(entry);
				}
				
			}
			
		};
		
		if (ArrayUtils.contains(buttons, "cancel")) {
			
			this.buttons.setFocus(ArrayUtils.indexOf(buttons, "cancel"));
			
		} else {
			
			this.buttons.setFocus(0);
		}
		
	}
	
	public void onButton(String entry) {
		
		
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		this.rot += UpdatingPanel.ROT_VEL * delta;
		this.rot = FastMath.normalizeCircular(this.rot, 0f, FastMath.PI2);
		
		if (poster == null) {
			
			loadPoster();
		}
		
		this.buttons.update(delta);
		
		if (cm != null) {
			
			cm.update(delta);
			if (cm.closed) { cm = null; }
		}
		
	}
	
	private void loadPoster() {
		
		File notdone = new File(metadir.getAbsolutePath()+"/posters/notdone");
		if (notdone.exists()) { return; }
		
		File imgF = new File(metadir.getAbsolutePath()+"/posters/"+info.poster);
		if (!imgF.exists()) { return; }
		
		Image img = null;
		try {
			
			img = new Image(imgF.getAbsolutePath());
			img.setFilter(Image.FILTER_LINEAR);
			
		} catch (Exception e) { e.printStackTrace(); }
		
		if (img != null) {
			
			poster = img;
		}
		
	}
	
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		g.setColor(Color.white);
		
		GUIRes.drawContextPane(g, pane);
		
		g.setFont(Fonts.roboto.s36);
		
		float w = pane.x + 20f * displayScale;
		float h = pane.y + 20f * displayScale;
		
		int pw = (int)(POSTER_WIDTH * displayScale);
		int ph = (int)(POSTER_HEIGHT * displayScale);
		
		if (poster != null && !poster.isDestroyed()) {
			
			poster.draw((int)(w), (int)(h), pw, ph);
			
		} else {
			
			float lx = w + (pw - GUIRes.loading.getWidth() * displayScale) * 0.5f;
			float ly = h + (ph - GUIRes.loading.getHeight() * displayScale) * 0.5f;
			
			GUIRes.loading.setRotation((rot / FastMath.PI2) * 360f);
			GUIRes.loading.draw((int)lx, (int)ly, displayScale, Menu.flavors[Settings.menu_flavor].copy().setAlpha(0.8f));
		}
		
		g.setColor(Color.grayTone(136));
		g.drawRect((int)(w), (int)(h), pw, ph, 2);
		
		w += pw + 20 * displayScale;
		
		g.setColor(Color.white);
		
		String s = info.title;
		
		float th = h;
		float fh = g.getFont().getHeight("I");
		
		for (int j = 0, f = 30; j < 3 && g.getFont().getWidth(s) > pane.width - (w - pane.x) - 20f * displayScale; j++) {
			
			f -= 4;
			g.setFont(Fonts.roboto.getSize(f));
			
			th = h + (fh - g.getFont().getHeight(s) - 5f * displayScale);
		}
		
		while (g.getFont().getWidth(s) > pane.width - (w - pane.x) - 20f * displayScale && s.length() > 3) { s = s.substring(0, s.length()-4)+"..."; }
		
		g.drawString(s, (int)w, (int)th);
		
		h += g.getFont().getHeight("I") + 5f * displayScale;
		
		GUIRes.splitter.draw((int)(w - 20f * displayScale), (int)h, (int)(pane.width - 60f * displayScale - pw), (int)(2f * displayScale + 0.5f));
		
		h += 10f * displayScale;
		
		s = "by "+info.author;
		
		if (!info.author.trim().toLowerCase().equals(info.artist.trim().toLowerCase())) {
			
			s += " & "+info.artist;
		}
		
		g.setFont(Fonts.robotoBold.s18);
		g.drawString(s, (int)w, (int)h);
		
		h += g.getFont().getHeight("I") * 2f;
		
		s = "Released:";
		g.drawString(s, (int)w, (int)h);
		
		w += g.getFont().getWidth(s+"  ");
		
		g.setFont(Fonts.roboto.s18);
		s = info.released+"";
		g.drawString(s, (int)w, (int)h);
		
		w += g.getFont().getWidth(s+"  ") + 40f * displayScale;
		
		g.setFont(Fonts.robotoBold.s18);
		s = "Status:";
		g.drawString(s, (int)w, (int)h);
		
		w += g.getFont().getWidth(s+"  ");
		
		g.setFont(Fonts.roboto.s18);
		s = info.status+"";
		g.drawString(s, (int)w, (int)h);
		
		g.setFont(Fonts.robotoBold.s18);
		
		h += g.getFont().getHeight("I") * 1.25f;
		w = pane.x + 40f * displayScale + pw;
		
		g.drawString("Genres:", w, h);
		
		w += g.getFont().getWidth(s+" ");
		
		float width = pane.width - pw - 60f - g.getFont().getWidth(s+" ");
		
		g.setFont(Fonts.roboto.s18);
		s = "";
		
		for (int i = 0, size = info.genres.size(); i < size; i++) {
			
			if (g.getFont().getWidth(s+", "+info.genres.get(i)) > width) {
				
				g.drawString(s, (int)w, (int)h);
				
				s = "";
				h += g.getFont().getHeight("I");
			}
			
			s += info.genres.get(i) + ((i < size-1) ? ", " : "");
		}
		
		g.drawString(s, (int)w, (int)h);
		
		g.setFont(Fonts.robotoBold.s18);
		
		float p = g.getFont().getHeight("I") * 1.25f;
		
		h += g.getFont().getHeight("I") * 2f;
		
		float tmp = (30f * displayScale + ph - h) / p;
		h += p * (tmp - FastMath.floor(tmp));
		
		w = pane.x + 40f * displayScale + pw;
		
		s = "Synopsis:";
		g.drawString(s, (int)w, (int)h);
		
		h += g.getFont().getHeight("I") * 1.25f;
		
		g.setFont(Fonts.roboto.s18);
		g.beginQuad();
		
		width = pane.width - 80f * displayScale - pw;
		
		s = info.synopsis;
		
		for (String add = ""; h + p < pane.y + pane.height - 72f * displayScale && s.length() > 0; s = add, add = "") {
			
			if (h >= pane.y + ph + 30f * displayScale) {
				
				width = pane.width - 40f * displayScale;
				w = pane.x + 20f * displayScale;
			}
			
			while (g.getFont().getWidth(s) > width && s.contains(" ")) {
				
				String str = s.substring(s.lastIndexOf(" "));
				add = str.trim()+" "+add.trim();
				s = s.substring(0, s.lastIndexOf(" "));
			}
			
			if (h + p * 2 >= pane.y + pane.height - 72f * displayScale && add.trim().length() > 0) {
				
				s = s.substring(0, s.length()-3)+"...";
			}
			
			g.drawStringEmbedded(s, (int)w, (int)h);
			h += p;
		}
		
		g.endQuad();
		
		this.buttons.render(g, 0f, 0f);
		
		if (cm != null) {
			
			cm.render(g, pX, pY);
		}
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		
		if (cm != null) {
			
			cm.handleInput(key, c, pressed);
			return;
		}
		
		if (pressed) {
			
			if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
				
				onClosing();
				this.closed = true;
				return;
			}
			
		}
		
		this.buttons.handleInput(key, c, pressed);
		
		super.handleInput(key, c, pressed);
	}
	
}
