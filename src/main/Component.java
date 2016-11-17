package main;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class Component {

	public float pX, pY;
	public boolean closed, hasFocus;
	
	public Component() {
		
		this.pX = 0f;
		this.pY = 0f;
		this.closed = false;
		this.hasFocus = false;
	}
	
	public void update(int delta) throws SlickException {
		
		
	}
	
	public void render(Graphics g, float pX, float pY) throws SlickException {
		this.pX = pX;
		this.pY = pY;
		
	}
	
	public void mouseWheelMoved(int change) {
		
		
	}
	
	public void handleInput(int key, char c, boolean pressed) {
		
		
	}
	
	public void onClosing() {
		
		
	}
	
	public void clear() {
		
		
	}
	
}
