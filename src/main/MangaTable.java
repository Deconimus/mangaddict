package main;

import static main.Main.displayScale;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

import mangaLib.MangaInfo;
import visionCore.geom.Color;
import visionCore.math.FastMath;

public class MangaTable extends MangaList {
	
	
	public MangaTable(File metadir, Rectangle pane, boolean loadMetaFromDir) {
		
		this(new ArrayList<MangaInfo>(), metadir, null, pane, loadMetaFromDir, false);
	}
	
	public MangaTable(File metadir, Predicate<File> dirFilter, Rectangle pane, boolean loadMetaFromDir) {
		
		this(new ArrayList<MangaInfo>(), metadir, dirFilter, pane, loadMetaFromDir, false);
	}
	
	public MangaTable(List<MangaInfo> entries, File metadir, Predicate<File> dirFilter, Rectangle pane, boolean loadMetaFromDir, boolean drawPanels) {
		super(entries, metadir, dirFilter, pane, MenuList.MODE_TABLE, loadMetaFromDir, drawPanels);
		
		
		
	}
	
	
	@Override
	public void renderEntry(Graphics g, MangaInfo entry, float x, float y, boolean selected, int i) {
		
		if (selected) {
			
			float alpha = (hasFocus) ? 0.4f : 0f;
			
			GUIRes.selected.draw((int)(x - 10f * displayScale), (int)(y), entryWidth,
					 			 (int)(240f * displayScale), Menu.flavors[Settings.menu_flavor].copy().setAlpha(alpha));
		}
		
		g.setColor(Color.white);
		
		Image pstr = posters.get(entry.title);
		if (pstr != null) {
			
			pstr.draw((int)x, (int)y, (int)(155f * displayScale), (int)(240f * displayScale));
			
		} else {
			
			pstr = Mangas.getThumb(entry.title);
			
			if (pstr != null && entry.poster.startsWith(pstr.getName())) {
				
				pstr.draw((int)x, (int)y, (int)(155f * displayScale), (int)(240f * displayScale));
				
			} else {
				
				float tx = x + (155f - GUIRes.loading.getWidth()) * displayScale * 0.5f;
				float ty = y + (240f - GUIRes.loading.getHeight()) * displayScale * 0.5f;
				
				GUIRes.loading.setRotation((loadingRot / FastMath.PI2) * 360f);
				GUIRes.loading.draw(tx, ty, displayScale, Menu.flavors[Settings.menu_flavor]);
			}
			
		}
		
		g.setColor(Color.grayTone(136));
		g.drawRect((int)x, (int)y, 155f * displayScale, 240f * displayScale, 2);
		
		GUIRes.splitter.draw(x + 155f * displayScale, y + 40f * displayScale, entryWidth - (155f + 10f) * displayScale, (int)(2f * displayScale + 0.5f));
		
		if (selected && hasFocus) {
			
			g.drawRect(x, y, entryWidth - 10f * displayScale, 240f * displayScale, 2);
		}
		
		if (entry == null || entry.title == null) { return; }
		
		String s = entry.title;
		
		g.setColor(Color.white);
		g.setFont(Fonts.roboto.s30);
		
		float ty = y + (36f * displayScale - g.getFont().getHeight(s));
		
		for (int j = 0, f = 30; j < 3 && g.getFont().getWidth(s) > entryWidth - 170f * displayScale; j++) {
			
			f -= 4;
			g.setFont(Fonts.roboto.getSize(f));
			
			ty = y + (36f * displayScale - g.getFont().getHeight(s));
		}
		
		while (g.getFont().getWidth(s) > entryWidth - 175f * displayScale && s.length() > 3) { s = s.substring(0, s.length()-4)+"..."; }
		
		g.drawString(s, (int)(x + (155f + 10f) * displayScale), (int)ty);
		
		
		g.setFont(Fonts.roboto.s18);
		g.beginQuad();
		
		int w = (int)(x + 165f * displayScale + 0.5f);
		int h = (int)(y + 46f * displayScale + 0.5f);
		int p = g.getFont().getHeight("I");
		
		s = "by "+entry.author;
		if (!entry.author.trim().toLowerCase().equals(entry.artist.trim().toLowerCase())) {
			
			s += " & "+entry.artist;
		}
		
		while (g.getFont().getWidth(s) > entryWidth - 175f * displayScale && s.length() > 3) { s = s.substring(0, s.length()-4)+"..."; }
		
		g.drawStringEmbedded(s, w, h);
		
		h += p;
		
		s = entry.status;
		
		g.drawStringEmbedded(s, w, h);
		
		h += p * 2;
		
		s = entry.synopsis.trim();
		
		for (String add = ""; h + p - y < 240f * displayScale && s.length() > 0; s = add, add = "") {
			
			while (g.getFont().getWidth(s) > entryWidth - 175f * displayScale && s.contains(" ")) {
				
				String str = s.substring(s.lastIndexOf(" "));
				add = str.trim()+" "+add.trim();
				s = s.substring(0, s.lastIndexOf(" "));
			}
			
			if (h + p * 2 - y >= 240f * displayScale && add.trim().length() > 0 && s.length() > 3) {
				
				s = s.substring(0, s.length()-3)+"...";
			}
			
			g.drawStringEmbedded(s, w, h);
			
			h += p;
		}
		
		g.endQuad();
		
	}
	
}
