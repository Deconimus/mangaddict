package components;

import static main.Main.displayScale;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import main.Fonts;
import main.GUIRes;
import main.Main;
import visionCore.geom.Color;
import visionCore.geom.Vector2f;
import visionCore.math.FastMath;

public class SearchPanel extends Component {

	
	public static final float WIDTH = 420f, HEIGHT = 240f;
	
	public static final int CURSOR_IDLE_TIME = 500;
	public static final float CURSOR_RAD_VEL = 0.004f;
	
	
	public String title;
	public Rectangle pane;
	
	public int cam, selStart, selEnd;
	public String txt;
	
	public int cursorIdle;
	public float cursorRad, cursorAlpha;
	
	
	public SearchPanel(String title) {
		
		this(title, centerPos());
	}
	
	public SearchPanel(String title, Vector2f pos) {
		
		this.title = title;
		this.pane = new Rectangle(pos.x, pos.y, WIDTH * displayScale, HEIGHT * displayScale);
		
		this.txt = "";
		this.cam = 0;
		
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
		
		g.setFont(Fonts.roboto.s24);
		g.drawString(txt, pane.x + w, pane.y + h);
		
		g.setColor(Color.white.copy().setAlpha(cursorAlpha));
		g.fillRect(pane.x + w + g.getFont().getWidth(txt.substring(0, cam)), pane.y + h, 2, g.getFont().getHeight("|"));
		
		g.setColor(Color.white.copy().setAlpha(0.25f));
		g.fillRect(pane.x + w + g.getFont().getWidth(txt.substring(0, selStart)), pane.y + h, g.getFont().getWidth(txt.substring(selStart, selEnd)), g.getFont().getHeight("|"));
		
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
				
				txt = txt.substring(0, cam)+c+txt.substring(cam);
				cam = Math.min(cam+1, txt.length());
				
				selStart = 0;
				selEnd = 0;
				
			} else {
			
				if (key == Input.KEY_ENTER) {
					
					this.onSearch(txt.trim());
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
						
						txt = txt.substring(0, Math.max(cam-1, 0))+txt.substring(cam);
						cam = Math.max(cam-1, 0);
						
					} else { deleteSelection(); }
					
					selStart = 0; selEnd = 0;
					
				} else if (key == Input.KEY_DELETE) {
					
					if (Math.abs(selStart - selEnd) == 0) {
					
						txt = txt.substring(0, cam)+txt.substring(Math.min(cam+1, txt.length()));
						
					} else { deleteSelection(); }
					
					selStart = 0; selEnd = 0;
					
				} else if (key == Input.KEY_HOME) {
					
					if (Main.shiftDown) {
						
						selStart = 0;
						if (selEnd < cam) { selEnd = cam; }
						
					} else { selStart = 0; selEnd = 0; }
					
					cam = 0;
					
				} else if (key == Input.KEY_END) {
					
					if (Main.shiftDown) {
						
						if (Math.abs(selStart - selEnd) == 0) { selStart = cam; }
						selEnd = txt.length();
						
					} else { selStart = 0; selEnd = 0; }
					
					cam = txt.length();
					
				} else if (key == Input.KEY_LEFT) {
					
					if (Main.shiftDown) {
						
						if (Math.abs(selStart - selEnd) == 0 || selStart == cam) {
							
							if (Math.abs(selStart - selEnd) == 0) { selEnd = cam; }
							selStart = Math.max(cam-1, 0);
							
						} else if (selEnd == cam) {
							
							selEnd = Math.max(cam-1, 0);
							
						}
						
					} else { selStart = 0; selEnd = 0; }
					
					cam = Math.max(cam-1, 0);
					
				} else if (key == Input.KEY_RIGHT) {
					
					if (Main.shiftDown) {
						
						if (Math.abs(selStart - selEnd) == 0 || selEnd == cam) {
							
							if (Math.abs(selStart - selEnd) == 0) { selStart = cam; }
							selEnd = Math.min(cam+1, txt.length());
							
						} else if (selEnd > cam) {
							
							selStart = Math.min(cam+1, txt.length());
							
						}
						
					} else { selStart = 0; selEnd = 0; }
					
					cam = Math.min(cam+1, txt.length());
					
				}
				
			}
			
		}
		
	}
	
	private void deleteSelection() {
		
		txt = txt.substring(0, selStart)+txt.substring(selEnd);
		
		if (selStart != cam) {
			
			cam = Math.max(cam - Math.abs(selStart-selEnd), 0);
		}
		
	}
	
	private boolean charHalal(char c) {
		
		return Character.isAlphabetic(c) || Character.isDigit(c) || c == ' ' || c == '.' || 
					c == ',' || c == '-' ||	c == '_' || c =='\'' || c == '+' || c == '*' || 
					c == '#' || c == '/' ||	c =='\\' || c == ':' || c == ';' || c == '!' || 
					c == '?' || c == '(' ||	c == ')' || c == '[' || c == ']' || c == '{' || 
					c == '}' || c == '|' ||	c == '<' || c == '>' || c =='\"' || c == '~';
	}
	
	
	public void onSearch(String search) {
		
		
		
	}
	
	public void onCancel() {
		
		
		
	}
	
	private static Vector2f centerPos() {
		
		return new Vector2f((Display.getWidth() - WIDTH * displayScale) * 0.5f, 
							(Display.getHeight() - HEIGHT * displayScale) * 0.5f);
	}
	
}
