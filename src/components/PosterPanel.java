package components;

import static main.Main.displayScale;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import main.Fonts;
import main.GUIRes;
import main.Mangas;
import main.Menu;
import main.Settings;
import main.Shaders;
import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.math.FastMath;

public class PosterPanel extends Component {
	
	
	public Rectangle pane;
	
	public MangaInfo info;
	
	public Image poster;
	
	public float loadingRot;
	
	
	public PosterPanel(MangaInfo info, Rectangle pane, Image poster) {
		
		this.info = info;
		this.pane = pane;
		this.poster = poster;
		
	}
	
	
	public void set(MangaInfo info, Image poster) {
		
		this.info = info;
		this.poster = poster;
	}
	
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (poster == null) {
			
			loadingRot += UpdatingPanel.ROT_VEL * delta;
			loadingRot = FastMath.normalizeCircular(loadingRot, 0f, FastMath.PI2);
		}
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		
		GUIRes.posterPanel.draw(pane.x, pane.y, pane.width, pane.height);
		
		if (poster != null) {
			
			if (Shaders.working && Settings.video_bicubicFiltering) {
				
				Shaders.bicubic.bind();
			}
			
			poster.draw(pane.x + 10f * displayScale, pane.y + 10f * displayScale, 256f * displayScale, 398f * displayScale);
			
			if (Shaders.working && Settings.video_bicubicFiltering) {
				
				Shaders.bicubic.unbind();
			}
			
		} else {
			
			Image img = Mangas.getThumb(info.title);
			
			if (img != null) {
				
				if (Shaders.working && Settings.video_bicubicFiltering) {
					
					Shaders.bicubic.bind();
				}
				
				img.draw(pane.x + 10f * displayScale, pane.y + 10f * displayScale, 256f * displayScale, 398f * displayScale);
				
				if (Shaders.working && Settings.video_bicubicFiltering) {
					
					Shaders.bicubic.unbind();
				}
				
			} else {
			
				float lx = pane.x + 10f * displayScale + (256f - GUIRes.loading.getWidth()) * displayScale * 0.5f;
				float ly = pane.y + 10f * displayScale + (398f - GUIRes.loading.getHeight()) * displayScale * 0.5f;
				
				GUIRes.loading.setRotation((loadingRot / FastMath.PI2) * 360f);
				GUIRes.loading.draw((int)lx, (int)ly, (int)(GUIRes.loading.getWidth() * displayScale), 
									(int)(GUIRes.loading.getHeight() * displayScale), Menu.flavors[Settings.menu_flavor]);
				
			}
		}
		
		GUIRes.posterFrame.draw(pane.x + 10f * displayScale, pane.y + 10f * displayScale, displayScale);
		
		GUIRes.glass.draw(pane.x + 10f * displayScale, pane.y + 10f * displayScale, displayScale, Color.white.copy().setAlpha(Settings.menu_glassAlpha));
		
		float w = pane.x + 15f * displayScale, h = pane.y + (10f + 398f + 20f) * displayScale;
		
		GUIRes.splitter.draw((int)(w - 15f * displayScale), (int)h, (int)(pane.width), (int)(2f * displayScale), new Color(1f, 1f, 1f, 0.6f));
		
		h += 10f * displayScale;
		
		g.setColor(Color.white);
		g.setFont(Fonts.roboto.s21);
		
		float hpad = g.getFont().getHeight("I");
		
		g.drawString("Author: "+info.author, (int)w, (int)h);
		
		h += hpad;
		
		g.drawString("Artist: "+info.artist, (int)w, (int)h);
		
		h += hpad;
		
		g.drawString("Released: "+info.released, (int)w, (int)h);
		
		h += hpad;
		
		g.drawString("Status: "+info.status, (int)w, (int)h);
		
		h += hpad;
		
		String gstring = "Genres: ";
		g.drawString(gstring, (int)w, (int)h);
		
		int gstringPad = g.getFont().getWidth(gstring);
		
		gstring = "";
		for (String genre : info.genres) { gstring += " "+genre+","; }
		gstring = gstring.substring(0, gstring.length()-1).trim();
		
		int gpady = (int)hpad;
		
		g.setFont(Fonts.roboto.s18);
		
		gpady = gpady - g.getFont().getHeight("I") - ((displayScale >= 1f) ? 2 : 0);
		h += gpady;
		
		for (int i = 0; gstring.trim().length() > 0; i++) {
			
			String add = "";
			
			int indent = (int)((i > 0) ? 30 * displayScale : gstringPad);
			
			while (gstring.contains(" ") && g.getFont().getWidth(gstring) > pane.width - 30f * displayScale - indent) {
				
				String s = gstring.substring(0, gstring.lastIndexOf(" "));
				add = gstring.substring(s.length()).trim()+" "+add;
				gstring = s.trim();
			}
			
			g.drawString(gstring, (int)(w + indent), (int)(h));
			h += hpad;
			
			gstring = add;
			
		}
		
		g.setFont(Fonts.roboto.s21);
		
		if (info.lastChapter > 0.0) {
			
			String lr = info.lastChapter+"";
			if (info.lastChapter == Math.floor(info.lastChapter)) { lr = ((int)info.lastChapter)+""; }
			
			lr = "Ch."+lr;
			
			if (info.lastPage > 0) {
				
				lr = info.lastPage + " " + lr;
			}
			
			g.drawString("Last read: "+lr, (int)w, (int)h);
			
			h += hpad;
			
		}
		
	}
	
}
