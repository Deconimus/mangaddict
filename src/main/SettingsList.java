package main;

import static main.Main.displayScale;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.reflection.Primitives;
import visionCore.util.StringUtils;

public class SettingsList extends MenuList<Field> {

	public String prefix;
	public Component cm;
	
	public SettingsList(String prefix, Rectangle pane, boolean drawPanels) {
		super(loadEntries(prefix), pane, new Vector2f(52f * displayScale, 52f * displayScale), MenuList.MODE_VERTICAL, drawPanels);
		
		this.prefix = prefix;
		
	}
	
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (cm != null) {
			
			cm.update(delta);
			if (cm.closed) { cm = null; }
		}
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		if (cm != null) {
			
			cm.render(g, pX, pY);
		}
		
	}
	
	@Override
	public void renderEntry(Graphics g, Field entry, float x, float y, boolean selected, int ind) {
		
		if (selected) {
			
			float alpha = (hasFocus) ? 0.6f : 0.25f;
			
			GUIRes.selected.draw((int)(x - 20f * displayScale), (int)y, (int)((pane.width + 20f * displayScale) + 0.5f),
								 (int)(entryHeight), Menu.flavors[Settings.menu_flavor].copy().setAlpha(alpha));
		}
		
		String s = transformName(entry.getName());
		
		float ty = y + (entryHeight - g.getFont().getHeight("I")) * 0.5f;
		
		g.setColor(Color.white);
		g.setFont(Fonts.roboto.s30);
		g.drawString(s, (int)x, (int)ty);
		
		float nameWidth = g.getFont().getWidth(s);
		
		
		String valstr = getValueString(entry);
		if (s.toLowerCase().contains("password")) { valstr = valstr.replaceAll(".", "*"); }
		
		if (valstr.equals("true") || valstr.equals("false")) {
			
			float ry = y + (entryHeight - 20f * displayScale) * 0.5f;
			float rx = x + pane.width - 20f * displayScale - 20f * displayScale;
			
			GUIRes.drawRadioButton(rx, ry, Boolean.parseBoolean(valstr));
			
		} else {
			
			
			for (int i = 0, z = 30; i < 2 && g.getFont().getWidth(valstr) > pane.width - nameWidth - 40f * displayScale; i++) {
				
				z -= 6;
				g.setFont(Fonts.roboto.getSize(z));
			}
			
			float tx = x + pane.width - g.getFont().getWidth(valstr) - 20f * displayScale;
			ty = y + (entryHeight - g.getFont().getHeight("I")) * 0.5f;
			
			//g.setColor(Menu.flavors[Settings.menu_flavor].scaleRGBCopy(0.8f));
			g.setColor(Color.lightGray);
			g.drawString(valstr, (int)tx, (int)ty);
			
		}
		
		g.setColor(Color.white);
		
	}
	
	private String transformName(String name) {
		
		if (name.startsWith(prefix+"_")) {
			
			name = name.substring(name.indexOf(prefix+"_")+prefix.length()+1);
		}
		
		name = name.replace("_", " ");
		name = StringUtils.formatCamelCase(name, " ", false);
		name = StringUtils.capitolWords(name);
		
		return name;
	}
	
	private String getValueString(Field field) {
		
		String valstr = "null"; 
		Object val = null;
		
		try {
			
			val = field.get(null);
			valstr = val.toString();
			
		} catch (Exception e) {}
		
		if (val instanceof Integer) {
		
			String[] names = null;
			try {
				
				Field f = Settings.class.getField(field.getName()+"_names");
				
				if (f != null && f.getType().isArray()) {
					
					names = (String[])f.get(null);
					
					int i = Math.max((int)val, 0);
					
					if (i < names.length) {
						
						valstr = names[i];
					}
				}
				 
			} catch (Exception e) {}
		
		}
		
		if (valstr == "" && !prefix.equals("MAL")) {
			
			valstr = "Default";
		}
		
		return valstr;
	}
	
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		
		if (cm != null) {
			
			cm.handleInput(key, c, pressed);
			return;
		}
		
		super.handleInput(key, c, pressed);
	}
	
	@Override
	public void onAction(Field entry) {
		
		String name = entry.getName();
		
		Object value = null;
		try { value = entry.get(null); } catch (Exception e) { }
		
		if (name.startsWith("menu_") && name.toLowerCase().contains("background")) {
			
			cm = new WallpaperSelect(entry, transformName(name), (String)value){
				
				@Override
				public void onWallpaperChange() {
					
					if (name.toLowerCase().contains("download")) {
						
						GUIRes.downloadbg = cache[CACHED_BACK];
						
					} else if (name.toLowerCase().contains("settings")) {
						
						GUIRes.settingsbg = cache[CACHED_BACK];
						Menu.setBG(GUIRes.settingsbg);
						
					} else {
						
						GUIRes.menubg = cache[CACHED_BACK];
					}
					
				}
				
			};
			
		} else if (value != null) {
			
			if (value instanceof Boolean) {
			
				try { entry.set(null, !((boolean)value)); } 
				catch (Exception e) { e.printStackTrace(); }
				
				Settings.settingChanged(entry);
				
			} else if (value instanceof Integer) {
				
				String[] names = null;
				try {
					
					Field f = Settings.class.getField(name+"_names");
					
					if (f != null && f.getType().isArray()) {
						
						names = (String[])f.get(null);
					}
					 
				} catch (Exception e) {}
				
				if (names != null && names.length > 1) {
					
					Rectangle centerIn = new Rectangle(pane.x, pane.y + (selected - camera + 2) * entryHeight, pane.width, entryHeight);
					this.cm = new ContextMenu(centerIn, names){
						
						@Override
						public void onAction(int sel) {
							
							try { entry.set(null, sel); }
							catch (Exception e) { e.printStackTrace(); }
							
							Settings.settingChanged(entry);
							
							cm.closed = true;
						}
						
					};
					((ContextMenu)this.cm).list.setFocus((int)value);
					
				} else {
					
					openInputField(entry, value);
				}
				
			} else {
				
				openInputField(entry, value);
			}
			
		}
		
	}
	
	private void openInputField(Field entry, Object value) {
		
		Vector2f pos = new Vector2f(pane.x + (pane.width - InputPanel.WIDTH * displayScale) * 0.5f, 
				pane.y + (pane.height - InputPanel.HEIGHT * displayScale) * 0.5f);
		
		
		int mode = InputPanel.MODE_STRING;
		
		if (value instanceof Integer || value instanceof Long || value instanceof Short) {
			
			mode = InputPanel.MODE_INT;
			
		} else if (value instanceof Float || value instanceof Double) {
			
			mode = InputPanel.MODE_FLOAT;
		}
		
		InputPanel input = new InputPanel(transformName(entry.getName()), value.toString(), pos, mode) {
		
			@Override
			public void onAction(String str) {
				str = str.trim().replace("\\", "/");
				
				Object parsed = null;
				
				try { parsed = StringUtils.parse(entry.get(null), str); } catch (Exception | Error e) {}
				
				if (parsed == null) {
				
					Class c = Primitives.getBoxedClass(entry.getType());
					
					for (Method m : c.getMethods()) {
						
						if (m.getName().toLowerCase().startsWith("parse")) {
							
							try {
								parsed = m.invoke(null, str);
								break;
							} catch (Exception e) {}
							
						}
						
					}
				}
				
				if (parsed != null) {
				
					try { entry.set(null, parsed); }
					catch (Exception e) { e.printStackTrace(); }
					
					Settings.settingChanged(entry);
				}
				
			}
		
		};
		
		input.censor = entry.getName().toLowerCase().contains("password");
		
		this.cm = input;
		
	}
	
	private static List<Field> loadEntries(String prefix) {
		
		List<Field> fields = new ArrayList<Field>();
		
		Field[] flds = Settings.class.getFields();
		
		for (Field f : flds) {
			if (Modifier.isFinal(f.getModifiers())) { continue; }
			
			String name = f.getName();
			if (name.equals("lastUpdated")) { continue; }
			
			if (name.startsWith(prefix) || (!name.contains("_") && prefix.equals("general"))) {
				
				fields.add(f);
			}
			
		}
		
		//Collections.sort(fields, (f0, f1) -> f0.getName().toLowerCase().compareTo(f1.getName().toLowerCase()));
		
		return fields;
	}

}
