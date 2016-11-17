package main;

import static main.Main.displayScale;

import java.util.Arrays;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;
import visionCore.math.FastMath;

public class ContextMenu extends Component {

	
	public Rectangle pane;
	public MenuList<String> list;
	
	public Component focus;
	
	
	public ContextMenu(String[] items) {
		
		this(null, items);
	}
	
	public ContextMenu(Rectangle centerIn, String[] items) {
		
		if (centerIn == null) { centerIn = new Rectangle(0f, 0f, Display.getWidth(), Display.getHeight()); }
		
		String longest = Arrays.asList(items).stream().max((s0, s1) -> Integer.compare(s0.length(), s1.length())).get();
		
		this.pane = new Rectangle(0f, 0f, Fonts.roboto.s30.getWidth(longest) + 90f * displayScale, (items.length * 48f + 40f) * displayScale);
		this.pane.setLocation(centerIn.x + (centerIn.width - pane.width) * 0.5f, centerIn.y + (centerIn.height - pane.height) * 0.5f);
		
		this.pane.setX(FastMath.clampToRangeC(pane.x, 20f * displayScale, Display.getWidth() - pane.width - 20f * displayScale));
		this.pane.setY(FastMath.clampToRangeC(pane.y, 20f * displayScale, Display.getHeight() - pane.height - 20f * displayScale));
		
		Rectangle listPane = new Rectangle(pane.x + 30f * displayScale, pane.y + 20f * displayScale, pane.width - 40f * displayScale, pane.height - 40f * displayScale);
		list = new MenuList<String>(Arrays.asList(items), listPane, 48f * displayScale, MenuList.MODE_VERTICAL, false);
		list.drawEntriesCentered = true;
		list.hasFocus = true;
		
		this.focus = null;
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		
		if (focus != null) {
			
			focus.update(delta);
			
			if (focus.closed) { this.closed = true; }
			
			return;
		}
		
		list.update(delta);
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		
		g.setColor(Color.white);
		
		if (focus != null) { focus.render(g, 0f, 0f); return; }
		
		GUIRes.drawContextPane(g, pane);
		
		list.render(g, 0f, 0f);
		
	}
	
	public void onAction(int selected) {
		
		
		
	}
	
	public void onCancel() {
		
		
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		if (focus != null) { focus.handleInput(key, c, pressed); return; }
		
		super.handleInput(key, c, pressed);
		
		list.handleInput(key, c, pressed);
		
		if (pressed) {
		
			if (key == Input.KEY_ESCAPE || key == Input.KEY_BACK) {
				
				onCancel();
				closed = true;
				
			} else if (key == Input.KEY_ENTER) {
				
				onAction(list.selected);
				
			}
			
		}
		
	}
	
}
