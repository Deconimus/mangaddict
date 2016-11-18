package mangaLib;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;

import javax.imageio.ImageIO;

import visionCore.util.Files;

public class Poster {

	public static final float WIDTH = 256f, HEIGHT = 398f;
	public static final int THUMB_WIDTH = 32, THUMB_HEIGHT = 50;
	
	//public static final float[] gaussKernel = new float[]{ 0.15247f, 0.22184f, 0.25138f, 0.22184f, 0.15247f };
	public static final float[] gaussKernel = new float[]{ 0.05449f, 0.2442f, 0.40262f, 0.2442f, 0.05449f };
	
	
	public static BufferedImage getResizedImage(BufferedImage img, int width, int height) {
		
		BufferedImage imgout = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = imgout.createGraphics();
		
		float sw = (float)width / (float)img.getWidth();
		float sh = (float)height / (float)img.getHeight();
		
		float scale = Math.max(sw, sh);
		
		float w = (float)img.getWidth() * scale;
		float h = (float)img.getHeight() * scale;
		
		float x = (width - w) * 0.5f;
		float y = (height - h) * 0.5f;
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, (int)x, (int)y, (int)(w + 0.5f), (int)(h + 0.5f), null);
		
		return imgout;
	}
	
	
	public static void saveResized(BufferedImage img, File out) throws Exception {
	
		BufferedImage imgout = getResizedImage(img, (int)WIDTH, (int)HEIGHT);
		
		ImageIO.write(imgout, "jpg", out);
		
		
		String n = out.getName();
		n = n.substring(0, n.lastIndexOf("."))+".png";
		File thumbOut = new File(out.getParentFile().getAbsolutePath()+"/thumbs/"+n);
		
		saveThumb(imgout, thumbOut);
		
	}
	
	
	public static void saveThumb(BufferedImage img, File out) throws Exception {
		
		BufferedImage resized = getResizedImage(img, THUMB_WIDTH*2, THUMB_HEIGHT*2);
		
		BufferedImage blurredPhase0 = new BufferedImage(THUMB_WIDTH*2, THUMB_HEIGHT*2, BufferedImage.TYPE_INT_RGB);
		
		BufferedImageOp op = new ConvolveOp(new Kernel(1, gaussKernel.length, gaussKernel), ConvolveOp.EDGE_NO_OP, null);
		op.filter(resized, blurredPhase0);
		
		BufferedImage blurred = new BufferedImage(THUMB_WIDTH*2, THUMB_HEIGHT*2, BufferedImage.TYPE_INT_RGB);
		op = new ConvolveOp(new Kernel(gaussKernel.length, 1, gaussKernel), ConvolveOp.EDGE_NO_OP, null);
		op.filter(blurredPhase0, blurred);
		
		BufferedImage imgout = getResizedImage(blurred, THUMB_WIDTH, THUMB_HEIGHT);
		
		if (!out.getParentFile().exists()) { out.getParentFile().mkdirs(); }
		ImageIO.write(imgout, "png", out);
		
	}
	
}
