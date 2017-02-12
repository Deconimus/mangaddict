package main;

import static main.Main.displayScale;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import components.MenuList;
import components.SettingsList;
import visionCore.math.FastMath;

public class SettingsMenu extends Menu {

	public Rectangle sectionpane;
	
	public List<SettingsList> sections;
	public MenuList<String> sectionList;
	
	
	public SettingsMenu() {
		
		this.title = "SETTINGS";
		setBG(GUIRes.settingsbg);
		
		this.renderComponents = false;
		
		SettingsMenu tis = this;
		
		contentpanel.setWidth(1280f * displayScale);
		float x = (Display.getWidth() - contentpanel.width) * 0.5f;
		contentpanel.setX(x);
		
		Rectangle sectionpane = new Rectangle(contentpanel.x + 30f * displayScale, contentpanel.y + 20f * displayScale,
														250f * displayScale, contentpanel.height - 40f * displayScale);
		
		Set<String> prefixes = new HashSet<String>();
		
		Field[] fields = Settings.class.getFields();
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers())) { continue; }
			
			String fieldName = field.getName();
			
			if (fieldName.contains("_")) {
				
				prefixes.add(fieldName.substring(0, fieldName.indexOf("_")));
			} 
		}
		
		this.sectionpane = new Rectangle(sectionpane.x + sectionpane.width + 40f * displayScale, contentpanel.y + 20f * displayScale,
				contentpanel.width - 100f * displayScale - sectionpane.width, contentpanel.height - 40f * displayScale);
		
		sections = new ArrayList<SettingsList>(prefixes.size()+1);
		
		List<String> sectionNames = new ArrayList<String>();
		sectionNames.addAll(prefixes);
		Collections.sort(sectionNames);
		sectionNames.add(0, "general");
		
		if (sectionNames.contains("menu")) {
			
			sectionNames.remove("menu");
			sectionNames.add(1, "menu");
		}
		
		if (sectionNames.contains("MAL")) {
			
			sectionNames.remove("MAL");
			sectionNames.add("MAL");
		}
		
		for (String section : sectionNames) {
			
			sections.add(new SettingsList(section, this.sectionpane, false){
				
				@Override
				public void handleInput(int key, char c, boolean pressed) {
					
					if (cm == null && pressed && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
						
						tis.setFocus(sectionList);
						return;
					}
					
					super.handleInput(key, c, pressed);
				}
				
				@Override
				public boolean onLeftBorder() {
					
					tis.setFocus(sectionList);
					
					return true;
				}
				
			});
		}
		
		for (int i = 0; i < sectionNames.size(); i++) {
			
			sectionNames.set(i, sectionNames.get(i).toUpperCase());
		}
		
		sectionList = new MenuList<String>(sectionNames, sectionpane, 56f * displayScale, MenuList.MODE_VERTICAL, false){
			
			@Override
			public void onAction(String entry) {
				
				if (sections.get(selected) != null && !sections.get(selected).entries.isEmpty()) {
					
					tis.setFocus(sections.get(selected));
				}
			}
			
			@Override
			public boolean onRightBorder() {
				
				if (sections.get(selected) != null && !sections.get(selected).entries.isEmpty()) {
					
					tis.setFocus(sections.get(selected));
				}
				return true;
			}
			
		};
		
		components.add(sectionList);
		setFocus(sectionList);
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		SettingsList section = sections.get(sectionList.selected);
		if (section != null) {
			
			section.update(delta);
		}
		
	}
	
	@Override
	public void render(Graphics g) throws SlickException {
		super.render(g);
		
		GUIRes.menuSectionPanel.draw(contentpanel.x, contentpanel.y, contentpanel.width, contentpanel.height);
		
		renderComponents(g);
		
		SettingsList section = sections.get(sectionList.selected);
		if (section != null) {
			
			section.render(g, 0f, 0f);
		}
		
		if (MAL.hasUser()) {
			
			MAL.renderProfile(g, 2);
		}
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		
		if (pressed) {
		
			if (getFocus() == sectionList && (key == Input.KEY_ESCAPE || key == Input.KEY_BACK)) {
				
				Settings.save();
				
				clearScene();
				Main.currentScene = new MainMenu(2);
			}
			
		}
		
		super.handleInput(key, c, pressed);
	}
	
}
