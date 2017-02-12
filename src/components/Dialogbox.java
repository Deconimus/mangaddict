package components;

import static main.Main.displayScale;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import main.Fonts;
import main.GUIRes;
import main.Menu;
import main.Settings;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.util.ArrayUtils;

public class Dialogbox extends Component {
	
	
	public Rectangle pane;
	
	public MenuList<String> buttons;
	
	public String txt, title;
	
	
	public Dialogbox(String title, String txt, String[] buttons) {
		
		float w = 420f * displayScale;
		float h = 280f * displayScale;
		
		this.txt = txt;
		this.title = title;
		
		Dialogbox tis = this;
		
		pane = new Rectangle((Display.getWidth() - w) * 0.5f, (Display.getHeight() - h) * 0.5f, w, h);
		
		float bw = 0f;
		for (String str : buttons) {
			
			bw = Math.max(bw, Fonts.robotoBold.s30.getWidth(str) + 80f * displayScale);
		}
		
		float bsx = (bw + 25f * displayScale) * buttons.length - 25f * displayScale;
		float offx = (pane.width - bsx) * 0.5f;
		Rectangle listpane = new Rectangle(pane.x + offx, pane.y + pane.height - 72f * displayScale, pane.width - offx * 2f, 60f * displayScale);
		
		List<String> bs = new ArrayList<String>();
		for (String s : buttons) { bs.add(s); }
		
		this.buttons = new MenuList<String>(bs, listpane, new Vector2f(bw * displayScale, 52f * displayScale), MenuList.MODE_HORIZONTAL, false){
			
			@Override
			public void renderEntry(Graphics g, String entry, float x, float y, boolean selected, int i) {
				
				g.setFont(Fonts.robotoBold.s30);
				
				g.setColor(Color.grayTone(32).setAlpha(0.925f));
				g.fillRect((int)x, (int)y, (int)entryWidth, (int)entryHeight);
				
				g.setColor(Color.grayTone(160));
				
				if (selected) {
					
					GUIRes.buttonSelected.draw((int)x, (int)y, (int)entryWidth, (int)entryHeight, Menu.flavors[Settings.menu_flavor].copy());
				}
				
				g.drawRect((int)x, (int)y, (int)entryWidth, (int)entryHeight, 1);
				
				float sx = x + (entryWidth - g.getFont().getWidth(entry)) * 0.5f;
				float sy = y + (entryHeight - g.getFont().getHeight("I")) * 0.5f;
				
				g.setColor(Color.grayTone((selected) ? 255 : 200));
				
				g.drawString(entry, (int)sx, (int)sy);
				
			}
			
			@Override
			public void onAction(String entry) {
				entry = entry.trim().toLowerCase();
				
				onButton(entry);
			}
			
			@Override
			protected void down(boolean byButton) { super.down(false); }
			
			@Override
			protected void up(boolean byButton) { super.up(false); }
			
		};
		
		if (ArrayUtils.contains(buttons, "no")) {
			
			this.buttons.setFocus(ArrayUtils.indexOf(buttons, "no"));
		}
		
	}
	
	public void onButton(String button) {
		
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		this.buttons.update(delta);
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		GUIRes.drawContextPane(g, pane);
		
		float h = pane.y + 20f * displayScale;
		float w = pane.x + 30f * displayScale;
		
		g.setFont(Fonts.robotoBold.s30);
		
		g.drawString(title, w, h);
		
		h += g.getFont().getHeight(title) + 10f * displayScale;
		
		g.setFont(Fonts.roboto.s24);
		
		float p = g.getFont().getHeight("I") * 1.05f;
		
		float width = pane.getWidth() - 40f * displayScale;
		
		String s = txt.trim();
		
		g.beginQuad();
		
		for (String add = ""; h + p < pane.y + pane.height - 72f * displayScale && s.length() > 0; s = add, add = "") {
			
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
		
		this.buttons.render(g, pX, pY);
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		
		if (pressed && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
			
			onClosing();
			closed = true;
			
			return;
		}
		
		this.buttons.handleInput(key, c, pressed);
	}
	
}
