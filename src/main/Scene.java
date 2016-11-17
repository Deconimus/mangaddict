package main;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class Scene {

	public long sceneFrames, sceneTime;
	
	public void init() throws SlickException {
		
		
	}
	
	public void update(int delta) throws SlickException {
		
		sceneFrames += 1L;
		sceneTime += delta;
		
	}
	
	public void render(Graphics g) throws SlickException {
		
		
	}
	
	public void handleInput(int key, char c, boolean pressed) {
		
		
	}
	
	public void mouseInput(int button, boolean pressed) {
		
		
		
	}
	
	public void mouseWheelMoved(int change) {
		
		
		
	}
	
	public void clearScene() {
		
		
	}
	
}
