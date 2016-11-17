package main;

import static main.Main.displayScale;
import static main.Mangas.POSTER_HEIGHT;
import static main.Mangas.POSTER_WIDTH;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;

public class MangaPosterRow extends MangaList {

	public MangaPosterRow(List<MangaInfo> entries, File metadir, Predicate<File> dirFilter, Rectangle pane, boolean loadMetaFromDir) {
		
		this(entries, metadir, dirFilter, pane, loadMetaFromDir, false);
	}
	
	public MangaPosterRow(List<MangaInfo> entries, File metadir, Predicate<File> dirFilter, Rectangle pane, boolean loadMetaFromDir, boolean drawPanels) {
		super(entries, metadir, dirFilter, pane, MenuList.MODE_HORIZONTAL, loadMetaFromDir, drawPanels);
		
	}
	
	@Override
	public void renderEntry(Graphics g, MangaInfo entry, float x, float y, boolean selected, int ind) {
		
		float sc = selected ? 1.15f : 1f;
		
		float width = POSTER_WIDTH * 0.75f * displayScale * sc;
		float height = POSTER_HEIGHT * 0.75f * displayScale * sc;
		
		x -= (width - (width / sc)) * 0.5f;
		y -= (height - (height / sc));
		
		Color filter = Color.black.copy();
		if (selected) { filter = Menu.flavors[Settings.menu_flavor]; }
		
		GUIRes.posterGlow.draw((int)(x - (GUIRes.posterGlow.getWidth() * 0.75f * displayScale * sc - width) * 0.5f) + 1f, 
							   (int)(y - (GUIRes.posterGlow.getHeight() * 0.75f * displayScale * sc - height) * 0.5f) + 1f, 
							   (int)(GUIRes.posterGlow.getWidth() * 0.75f * displayScale * sc) - 2,
							   (int)(GUIRes.posterGlow.getHeight() * 0.75f * displayScale * sc) - 2, filter);
		
		Image pstr = posters.get(entry.title);
		if (pstr != null) {
			
			drawPoster(pstr, x, y, width, height);
			
		} else {
			
			pstr = Mangas.getThumb(entry.title);
			
			if (pstr != null) {
				
				drawPoster(pstr, x, y, width, height);
				
			} else {
				
				float tx = x + (width - GUIRes.loading.getWidth() * displayScale) * 0.5f;
				float ty = y + (height - GUIRes.loading.getHeight() * displayScale) * 0.5f;
				
				GUIRes.loading.setRotation((loadingRot / FastMath.PI2) * 360f);
				GUIRes.loading.draw(tx, ty, displayScale, Menu.flavors[Settings.menu_flavor]);
			}
		}
		
		if (selected) {
			
			// will be replace anytime soon(tm)
			if (MangaView.selectedCenter == null) { MangaView.selectedCenter = new Vector2f(0f, 0f); }
			MangaView.selectedCenter.set(x + width * 0.5f, y + height * 0.75f);
			
			Image img = null;
			
			if (entry.read) {
				
				img = GUIRes.tick;
				
			} else if (entry.lastChapter > 0) {
				
				img = GUIRes.reading;
				
			}
			
			float imgW = 0f;
			if (img != null) { imgW = img.getWidth() * displayScale; }
			
			String s = entry.title;
			g.setFont(Fonts.robotoBold.s30);
			
			x += (width - g.getFont().getWidth(s) - imgW - 10f * displayScale) * 0.5f;
			float y1 = y - (10f * displayScale + g.getFont().getHeight(s));
			
			g.drawStringShadow(s, x, y1, (int)(2f * displayScale + 0.5f), 0.15f);
			
			if (img != null) {
				
				x += g.getFont().getWidth(s) + 10f * displayScale;
				y1 = y1 + (g.getFont().getHeight(s) - img.getHeight() * displayScale) * 0.5f;
				
				float rW = (int)(img.getWidth() * displayScale + 0.5f);
				float rH = (int)(img.getHeight() * displayScale + 0.5f);
				
				img.draw((int)x, (int)y1, rW, rH, Color.white);
			}
			
		}
		
	}
	
	private void drawPoster(Image pstr, float x, float y, float width, float height) {
		
		float tW = width / pstr.texture.getWidth();
		float tH = height / pstr.texture.getHeight();
		
		if (Shaders.working && Settings.video_bicubicFiltering) {
			
			Shaders.bicubic.bind();
		}
		
		pstr.draw((int)x, (int)y, (int)(width + 0.5f), (int)(height + 0.5f));
		
		if (Shaders.working && Settings.video_bicubicFiltering) {
			
			Shaders.bicubic.unbind();
		}
	}
	
}
