package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;

import static main.Main.displayScale;

public class Menu extends Scene {

	
	public static Color[] flavors;
	
	static {
		
		flavors = new Color[9];
		
		flavors[0] = Color.white.copy();
		flavors[1] = Color.lightBlue.copy();
		flavors[2] = Color.cyan.copy();
		flavors[3] = Color.green.copy();
		flavors[4] = Color.mint.copy();
		flavors[5] = Color.orange.copy();
		flavors[6] = Color.yellow.copy();
		flavors[7] = Color.red.copy();
		flavors[8] = Color.magenta.copy();
		
	}
	
	private static Image bg, fadingOut;
	
	
	public List<Component> components;
	private Component focus;
	
	public Rectangle contentpanel;
	
	public String title;
	
	public float titleX, titleY;
	
	public boolean renderComponents;
	
	
	public Menu() {
		
		this.components = new ArrayList<Component>();
		this.components.add(new UpdatingPanel());
		
		this.contentpanel = new Rectangle((Display.getWidth() - 1024f * displayScale) * 0.5f, 150f * displayScale, 1024f * displayScale, 830f * displayScale);
		
		this.titleX = -1f;
		this.titleY = -1f;
		
		this.renderComponents = true;
		
	}
	
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		for (Iterator<Component> it = components.iterator(); it.hasNext();) {
			Component c = it.next();
			
			c.update(delta);
			
			if (c.closed) { it.remove(); }
		}
		
		if (fadingOut != null) {
			
			fadingOut.setAlpha(fadingOut.getAlpha() * (float)Math.pow(0.875f, (float)delta / 16f));
			
			if (fadingOut.getAlpha() <= 0.001f) {
				
				fadingOut = null;
			}
			
		}
		
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		super.render(g);
		
		if (bg != null) {
			
			float w = (float)Display.getWidth() / (float)bg.getWidth();
			float h = (float)Display.getHeight() / (float)bg.getHeight();
			
			float scale = Math.max(w, h);
			
			int x = (int)((Display.getWidth() - bg.getWidth() * scale) * 0.5f);
			int y = (int)((Display.getHeight() - bg.getHeight() * scale) * 0.5f);
			
			bg.draw(x, y, scale);

		}
		
		if (fadingOut != null) {
			
			float w = (float)Display.getWidth() / (float)fadingOut.getWidth();
			float h = (float)Display.getHeight() / (float)fadingOut.getHeight();
			
			float scale = Math.max(w, h);
			
			int x = (int)((Display.getWidth() - fadingOut.getWidth() * scale) * 0.5f);
			int y = (int)((Display.getHeight() - fadingOut.getHeight() * scale) * 0.5f);
			
			fadingOut.draw(x, y, scale);
		}
		
		float scale = (Display.getHeight() - 250f) / (float)GUIRes.contentPanel.getHeight();
		
		g.setFont(Fonts.roboto.s48);
		
		int shadow = Math.max((int)(2f * displayScale + 0.5f), 1);
		
		int x = (int)((titleX > -1) ? titleX : contentpanel.x);
		int y = (int)((titleY > -1) ? titleY : contentpanel.y - g.getFont().getHeight("I") - 10f * displayScale);
		
		titleX = x;
		titleY = y;
		
		g.setColor(Color.white);
		g.drawStringShadow(title, x, y, shadow, 0.1f);
		
		Clock.renderClock(g, -20f, 10f, Color.white, Fonts.roboto.s48, true);
		
		if (renderComponents) {
			
			renderComponents(g);
		}
		
	}
	
	protected void renderComponents(Graphics g) throws SlickException {
		
		for (Component c : components) {
			
			c.render(g, 0f, 0f);
		}
		
	}
	
	@Override
	public void mouseWheelMoved(int change) {
		super.mouseWheelMoved(change);
		
		if (focus != null) {
			
			focus.mouseWheelMoved(change);
		}
		
	}
	
	@Override
	public void mouseInput(int button, boolean pressed) {
		
		
	}

	public void handleInput(int key, char c, boolean pressed) {
		super.handleInput(key, c, pressed);
		
		if (focus != null) {
			
			focus.handleInput(key, c, pressed);
		}
		
	}
	
	public static void setBG(Image img) {
		if (img == null) { return; }
		if (bg != null && bg.equals(img)) { return; }
		
		if (bg != null && !bg.isDestroyed()) {
			
			//fadingOut.setAlpha(1f);
			fadingOut = bg;
		}
		
		img.setAlpha(1f);
		bg = img;
		
	}
	
	protected void setFocus(Component focus) {
		if (focus == null) { return; }
		
		if (this.focus != null) { this.focus.hasFocus = false; }
		
		focus.hasFocus = true;
		
		this.focus = focus;
	}
	
	protected Component getFocus() {
		
		return this.focus;
	}
	
	@Override
	public void clearScene() {
		super.clearScene();
		
		for (Component c : components) {
			
			if (c != null) {
				
				c.clear();
			}
		}
		
	}
	
}
