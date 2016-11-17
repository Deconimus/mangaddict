package main;

import static main.Main.displayScale;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;

public class InputPanel extends Component {

	
	public static final float WIDTH = 420f, HEIGHT = 240f;
	
	public static final int CURSOR_IDLE_TIME = 500;
	public static final float CURSOR_RAD_VEL = 0.004f;
	
	public static final int MODE_STRING = 0, MODE_INT = 1, MODE_FLOAT = 2;
	
	
	public String title;
	public Rectangle pane;
	
	public int cursor, selStart, selEnd, cam, charsPerLine;
	public String txt;
	
	public int cursorIdle;
	public float cursorRad, cursorAlpha;
	
	public int mode;
	public boolean censor;
	
	
	public InputPanel(String title) {
		
		this(title, "", centerPos());
	}
	
	public InputPanel(String title, Vector2f pos) {
		
		this(title, "", pos);
	}
	
	public InputPanel(String title, String txt, Vector2f pos) {
		
		this(title, txt, pos, MODE_STRING);
	}
	
	public InputPanel(String title, String txt, Vector2f pos, int mode) {
		
		this.title = title;
		this.pane = new Rectangle(pos.x, pos.y, WIDTH * displayScale, HEIGHT * displayScale);
		
		this.mode = mode;
		this.censor = false;
		
		this.charsPerLine = 26;
		
		if (txt == null) { this.txt = ""; }
		else { this.txt = txt.trim(); }
		
		this.cursor = this.txt.length();
		this.cam = Math.max(this.txt.length() - charsPerLine, 0);
		
		this.cursorIdle = 0;
		this.cursorRad = FastMath.PI * 0.5f;
		this.cursorAlpha = 1f;
		
		this.selStart = 0;
		this.selEnd = 0;
		
	}
	
	@Override
	public void update(int delta) throws SlickException {
		super.update(delta);
		
		if (cursorIdle < CURSOR_IDLE_TIME) {
			
			cursorIdle += delta;
			cursorAlpha = 1f;
			cursorRad = FastMath.PI * 0.5f;
			
		} else {
			
			cursorRad = FastMath.normalizeCircular(cursorRad + CURSOR_RAD_VEL * delta, 0f, FastMath.PI2);
			cursorAlpha = Math.min((FastMath.sin(cursorRad) + 1f) * 0.5f + 0.1f, 1f);
			
		}
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		g.setColor(Color.white);
		
		Image img = GUIRes.searchPanel;
		
		if (img != null) {
			
			img.draw(pane.x - 16f * displayScale, pane.y - 16f * displayScale,
					 pane.width + 32f * displayScale, pane.height + 32f * displayScale);
		}
		
		g.setFont(Fonts.roboto.s24);
		
		float w = 20f * displayScale, h = 10f * displayScale;
		
		if (title != null && title.trim().length() > 0) {
			
			g.drawString(title, pane.x + w, pane.y + h);
		}
		
		h = (62f + 2f) * displayScale;
		h += (43f * displayScale - g.getFont().getHeight("I")) * 0.5f;
		w += 10f * displayScale;
		
		String s = txt.substring(Math.max(cam, 0), (int)Math.min(cam + charsPerLine, txt.length()));
		if (censor) { s = s.replaceAll(".", "*"); }
		
		g.setFont(Fonts.robotoMono24);
		g.drawString(s, pane.x + w, pane.y + h);
		
		g.setColor(Color.white.copy().setAlpha(cursorAlpha));
		g.fillRect(pane.x + w + g.getFont().getWidth(txt.substring(cam, cursor)), pane.y + h, 2, g.getFont().getHeight("|"));
		
		float width = Math.abs((selEnd - selStart) * g.getFont().getWidth("x"));
		float shift = Math.min(selStart - cursor, 0) * g.getFont().getWidth("x");
		
		g.setColor(Color.white.copy().setAlpha(0.25f));
		g.fillRect(pane.x + w + g.getFont().getWidth(txt.substring(cam, cursor)) + shift, pane.y + h,
					Math.abs(width), g.getFont().getHeight("|"));
		
	}
	
	@Override
	public void handleInput(int key, char c, boolean pressed) {
		super.handleInput(key, c, pressed);
		
		if (pressed) {
		
			cursorIdle = 0;
			
			if (charHalal(c)) {
				
				if (Math.abs(selStart-selEnd) > 0) {
					
					deleteSelection();
				}
				
				txt = txt.substring(0, cursor)+c+txt.substring(cursor);
				cursor = Math.min(cursor+1, txt.length());
				cam = Math.max(Math.min(cam+1, txt.length()-charsPerLine), 0);
				
				selStart = 0;
				selEnd = 0;
				
			} else {
			
				if (key == Input.KEY_ENTER) {
					
					this.onAction(txt.trim());
					this.closed = true;
					
					selStart = 0; selEnd = 0;
					
					return;
					
				} else if (key == Input.KEY_ESCAPE) {
					
					this.onCancel();
					this.closed = true;
					
					selStart = 0; selEnd = 0;
					
					return;
					
				} else if (key == Input.KEY_BACK) {
					
					if (Math.abs(selStart - selEnd) == 0) {
						
						txt = txt.substring(0, Math.max(cursor-1, 0))+txt.substring(cursor);
						cursor = Math.max(cursor-1, 0);

						cam = Math.max(Math.min(cam-1, txt.length()-charsPerLine), 0);
						
					} else { deleteSelection(); }
					
					selStart = 0; selEnd = 0;
					
				} else if (key == Input.KEY_DELETE) {
					
					if (Math.abs(selStart - selEnd) == 0) {
					
						txt = txt.substring(0, cursor)+txt.substring(Math.min(cursor+1, txt.length()));
						
					} else { deleteSelection(); }
					
					selStart = 0; selEnd = 0;
					
					cam = Math.max(Math.min(cam, txt.length()-charsPerLine), 0);
					
				} else if (key == Input.KEY_HOME) {
					
					if (Main.shiftDown) {
						
						selStart = 0;
						if (selEnd < cursor) { selEnd = cursor; }
						
					} else { selStart = 0; selEnd = 0; }
					
					cursor = 0;
					
					cam = 0;
					
				} else if (key == Input.KEY_END) {
					
					if (Main.shiftDown) {
						
						if (Math.abs(selStart - selEnd) == 0) { selStart = cursor; }
						selEnd = txt.length();
						
					} else { selStart = 0; selEnd = 0; }
					
					cursor = txt.length();
					
					cam = Math.max(txt.length()-charsPerLine, 0);
					
				} else if (key == Input.KEY_LEFT) {
					
					if (Main.shiftDown) {
						
						if (Math.abs(selStart - selEnd) == 0 || selStart == cursor) {
							
							if (Math.abs(selStart - selEnd) == 0) { selEnd = cursor; }
							selStart = Math.max(cursor-1, 0);
							
						} else if (selEnd == cursor) {
							
							selEnd = Math.max(cursor-1, 0);
							
						}
						
					} else { selStart = 0; selEnd = 0; }
					
					cursor = Math.max(cursor-1, 0);
					
					if (cursor - cam <= 0) {
						
						cam = Math.max(cam-1, 0);
					}
					
				} else if (key == Input.KEY_RIGHT) {
					
					if (Main.shiftDown) {
						
						if (Math.abs(selStart - selEnd) == 0 || selEnd == cursor) {
							
							if (Math.abs(selStart - selEnd) == 0) { selStart = cursor; }
							selEnd = Math.min(cursor+1, txt.length());
							
						} else if (selEnd > cursor) {
							
							selStart = Math.min(cursor+1, txt.length());
							
						}
						
					} else { selStart = 0; selEnd = 0; }
					
					cursor = Math.min(cursor+1, txt.length());
					
					if (cursor - cam > charsPerLine) {
						
						cam = Math.max(Math.min(cam+1, txt.length()-charsPerLine), 0);
					}
					
				} else if (key == Input.KEY_UP) {
					
					if (mode == MODE_INT) {
						
						try { txt = (Integer.parseInt(txt.trim())+1)+""; } catch (Exception e) {}
					}
					
				} else if (key == Input.KEY_DOWN) {
					
					if (mode == MODE_INT) {
						
						try { txt = (Integer.parseInt(txt.trim())-1)+""; } catch (Exception e) {}
					}
					
				}
				
			}
			
		}
		
	}
	
	private void deleteSelection() {
		
		txt = txt.substring(0, selStart)+txt.substring(selEnd);
		
		if (selStart != cursor) {
			
			cursor = Math.max(cursor - Math.abs(selStart-selEnd), 0);
			
			cam -= Math.abs(selStart-selEnd);
		}
		
		cam = Math.max(Math.min(cam, txt.length()-charsPerLine), 0);
		
	}
	
	private boolean charHalal(char c) {
		
		if (mode == MODE_INT) {
			
			return Character.isDigit(c);
		}
		
		if (mode == MODE_FLOAT) {
			
			return Character.isDigit(c) || (c == '.' && !txt.contains("."));
		}
		
		return Character.isAlphabetic(c) || Character.isDigit(c) || c == ' ' || c == '.' || 
					c == ',' || c == '-' ||	c == '_' || c =='\'' || c == '+' || c == '*' || 
					c == '#' || c == '/' ||	c =='\\' || c == ':' || c == ';' || c == '!' || 
					c == '?' || c == '(' ||	c == ')' || c == '[' || c == ']' || c == '{' || 
					c == '}' || c == '|' ||	c == '<' || c == '>' || c =='\"' || c == '~';
	}
	
	
	public void onAction(String txt) {
		
		
		
	}
	
	public void onCancel() {
		
		
		
	}
	
	private static Vector2f centerPos() {
		
		return new Vector2f((Display.getWidth() - WIDTH * displayScale) * 0.5f, 
							(Display.getHeight() - HEIGHT * displayScale) * 0.5f);
	}
	
}
