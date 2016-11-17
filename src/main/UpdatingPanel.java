package main;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import visionCore.geom.Color;
import visionCore.math.FastMath;

import static main.Main.displayScale;

public class UpdatingPanel extends Component {

	
	public static final float ROT_VEL = 0.003f;
	
	public static float rad;
	
	
	public UpdatingPanel() {
		
		
		
	}
	
	
	@Override
	public void update(int delta) {
		
		if (Main.mangadl.get() == null) { return; }
		
		rad += ROT_VEL * delta;
		rad = FastMath.normalizeCircular(rad, 0f, FastMath.PI2);
		
	}
	
	@Override
	public void render(Graphics g, float pX, float pY) throws SlickException {
		super.render(g, pX, pY);
		
		if (Main.mangadl.get() == null) { return; }
		
		g.setFont(Fonts.robotoBold.s24);
		
		float neededWidth = g.getFont().getWidth(Main.mangadl.get().title) + (36f + GUIRes.loading.getWidth()) * displayScale;
		
		float width = GUIRes.updatePanel.getWidth() * displayScale;
		
		float offX = neededWidth - width;
		offX = FastMath.clampToRangeC(offX, -50f * displayScale, 0f);
		
		if (neededWidth - width > 0f) {
			
			width = neededWidth;
		}
		
		GUIRes.updatePanel.draw(offX, -26 * displayScale, width, GUIRes.updatePanel.getHeight() * displayScale, Color.white.copy().setAlpha(0.75f));
		
		float h = 4f * displayScale, w = 10f * displayScale;
		
		g.setColor(Menu.flavors[Settings.menu_flavor]);
		
		g.drawString(Main.mangadl.get().title, w, h);
		
		h += g.getFont().getHeight(Main.mangadl.get().title);
		
		String status = Main.mangadl.get().status.get();
		
		g.setColor(Color.white);
		g.setFont(Fonts.robotoBold.s18);
		
		g.drawString(status, w, h);
		
		h += g.getFont().getHeight(status);
		
		
		w = width - (GUIRes.loading.getWidth() + 16f) * displayScale + offX;
		h = (GUIRes.updatePanel.getHeight() - 26f - GUIRes.loading.getHeight()) * displayScale * 0.5f;
		
		GUIRes.loading.setRotation((rad / FastMath.PI2) * 360f);
		GUIRes.loading.draw(w, h, displayScale, Menu.flavors[Settings.menu_flavor]);
		
	}
	
}
