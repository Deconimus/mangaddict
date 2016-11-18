package visionCore.geom;

import java.io.Serializable;
import java.nio.FloatBuffer;

import visionCore.math.FastMath;

/**
 * A simple wrapper round the values required for a colour
 * 
 * @author Kevin Glass
 */
public class Color implements Serializable {
	
	/** The version ID for this class  */
	private static final long serialVersionUID = 1393939L;
	
    public static final Color transparent	= new Color(0.0f,0.0f,0.0f,0.0f);
	public static final Color white			= new Color(1f, 1f, 1f, 1f);
	public static final Color yellow		= new Color(1.0f,1.0f,0,1.0f);
	public static final Color red			= new Color(1.0f,0,0,1.0f);
	public static final Color blue			= new Color(0,0,1.0f,1.0f);
	public static final Color lightBlue		= new Color(133, 220, 255);
	public static final Color green			= new Color(0,1.0f,0,1.0f);
	public static final Color mint			= new Color(154, 255, 154);
	public static final Color black 		= new Color(0,0,0,1.0f);
	public static final Color gray 			= new Color(0.5f,0.5f,0.5f,1.0f);
	public static final Color cyan 			= new Color(0,1.0f,1.0f,1.0f);
	public static final Color darkGray		= Color.grayTone(0.3f);
	public static final Color lightGray		= Color.grayTone(0.7f);
	public static final Color lighterGray	= Color.grayTone(220);
    public static final Color pink			= new Color(255, 175, 175, 255);
    public static final Color orange		= new Color(255, 165, 0, 255);
    public static final Color magenta		= new Color(255, 0, 255, 255);
    
    
    public static final Color yellowLight = new Color(235, 235, 175);
    public static final Color moonLight = new Color(133, 220, 255);

    
	/** The red component of the colour */
	public float r;
	/** The green component of the colour */
	public float g;
	/** The blue component of the colour */
	public float b;
	/** The alpha component of the colour */
	public float a = 1.0f;
	
	/**
	 * Copy constructor
	 * 
	 * @param color The color to copy into the new instance
	 */
	public Color(Color color) {
		this(color.r, color.g, color .b, color.a);
	}

	/**
	 * Create a component based on the first 4 elements of a float buffer
	 * 
	 * @param buffer The buffer to read the color from
	 */
	public Color(FloatBuffer buffer) {
		this(buffer.get(), buffer.get(), buffer.get(), buffer.get());
	}
	
	/**
	 * Create a 3 component colour
	 * 
	 * @param r The red component of the colour (0.0 -> 1.0)
	 * @param g The green component of the colour (0.0 -> 1.0)
	 * @param b The blue component of the colour (0.0 -> 1.0)
	 */
	public Color(float r,float g,float b) {
		this(r, g, b, 1f);
	}

	/**
	 * Create a 4 component colour
	 * 
	 * @param r The red component of the colour (0.0 -> 1.0)
	 * @param g The green component of the colour (0.0 -> 1.0)
	 * @param b The blue component of the colour (0.0 -> 1.0)
	 * @param a The alpha component of the colour (0.0 -> 1.0)
	 */
	public Color(float r, float g, float b, float a) {
		
		this.r = FastMath.clampToRange(r, 0f, 1f);
		this.g = FastMath.clampToRange(g, 0f, 1f);
		this.b = FastMath.clampToRange(b, 0f, 1f);
		this.a = FastMath.clampToRange(a, 0f, 1f);
		
	}

	/**
	 * Create a 3 component colour
	 * 
	 * @param r The red component of the colour (0 -> 255)
	 * @param g The green component of the colour (0 -> 255)
	 * @param b The blue component of the colour (0 -> 255)
	 */
	public Color(int r,int g,int b) {
		this(r, g, b, 255);
	}

	/**
	 * Create a 4 component colour
	 * 
	 * @param r The red component of the colour (0 -> 255)
	 * @param g The green component of the colour (0 -> 255)
	 * @param b The blue component of the colour (0 -> 255)
	 * @param a The alpha component of the colour (0 -> 255)
	 */
	public Color(int r, int g,int b,int a) {
		this(r / 255f , g / 255f, b / 255f, a / 255f);
	}
	
	/**
	 * Create a colour from an evil integer packed 0xAARRGGBB. If AA 
	 * is specified as zero then it will be interpreted as unspecified
	 * and hence a value of 255 will be recorded.
	 * 
	 * @param value The value to interpret for the colour
	 */
	public Color(int value) {
		
		this((value & 0x00FF0000) >> 16, (value & 0x0000FF00) >> 8, (value & 0x000000FF), FastMath.normalizeCircular((value & 0xFF000000) >> 24, 0, 255));
		
	}
	
	/**
	 * Decode a number in a string and process it as a colour
	 * reference.
	 * 
	 * @param nm The number string to decode
	 * @return The color generated from the number read
	 */
	public static Color decode(String nm) {
		return new Color(Integer.decode(nm).intValue());
	}
	
	
	public void set(Color color) {
		
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		
	}
	
	public void setRGB(Color color) {
		
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		
	}
	
	public void set(float r, float g, float b) {
		
		set(r, g, b, this.a);
	}
	
	public void set(float r, float g, float b, float a) {
		
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
	}
	
	public Color setRed(float r) { this.r = r; return this; }
	public Color setGreen(float g) { this.g = g; return this; }
	public Color setBlue(float b) { this.b = b; return this; }
	public Color setAlpha(float a) { this.a = a; return this; }
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ((int) (r+g+b+a)*255);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof Color) {
			Color o = (Color) other;
			return ((o.r == r) && (o.g == g) && (o.b == b) && (o.a == a));
		}
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[Color r="+r+"; g="+g+"; b="+b+"; a="+a+";]";
	}
	
	public Color copy() {
		
		return new Color(this);
	}

	/**
	 * Get the red byte component of this colour
	 * 
	 * @return The red component (range 0-255)
	 */
	public int getRed() {
		return (int) (r * 255);
	}

	/**
	 * Get the green byte component of this colour
	 * 
	 * @return The green component (range 0-255)
	 */
	public int getGreen() {
		return (int) (g * 255);
	}

	/**
	 * Get the blue byte component of this colour
	 * 
	 * @return The blue component (range 0-255)
	 */
	public int getBlue() {
		return (int) (b * 255);
	}

	/**
	 * Get the alpha byte component of this colour
	 * 
	 * @return The alpha component (range 0-255)
	 */
	public int getAlpha() {
		return (int) (a * 255);
	}
	
	/**
	 * Get the red byte component of this colour
	 * 
	 * @return The red component (range 0-255)
	 */
	public int getRedByte() {
		return (int) (r * 255);
	}

	/**
	 * Get the green byte component of this colour
	 * 
	 * @return The green component (range 0-255)
	 */
	public int getGreenByte() {
		return (int) (g * 255);
	}

	/**
	 * Get the blue byte component of this colour
	 * 
	 * @return The blue component (range 0-255)
	 */
	public int getBlueByte() {
		return (int) (b * 255);
	}

	/**
	 * Get the alpha byte component of this colour
	 * 
	 * @return The alpha component (range 0-255)
	 */
	public int getAlphaByte() {
		return (int) (a * 255);
	}
	
	/**
	 * Multiply this color by another
	 *
	 * @param c the other color
	 * @return product of the two colors
	 */
	public Color multiply(Color c) {
		return new Color(r * c.r, g * c.g, b * c.b, a * c.a);
	}

	/**
	 * Add another colour to this one
	 * 
	 * @param c The colour to add 
	 */
	public Color add(Color c) {
		
		r += c.r;
		g += c.g;
		b += c.b;
		a += c.a;
		
		return this;
	}
	
	/**
	 * Scale the components of the colour by the given value
	 * 
	 * @param value The value to scale by
	 */
	public Color scale(float value) {
		
		r *= value;
		g *= value;
		b *= value;
		a *= value;
		
		return this;
	}
	
	public Color scaleRGB(float value) {
		
		r *= value;
		g *= value;
		b *= value;
		
		return this;
	}
	
	/**
	 * Add another colour to this one
	 * 
	 * @param c The colour to add 
	 * @return The copy which has had the color added to it
	 */
	public Color addToCopy(Color c) {
		Color copy = new Color(r,g,b,a);
		copy.r += c.r;
		copy.g += c.g;
		copy.b += c.b;
		copy.a += c.a;
		
		return copy;
	}
	
	/**
	 * Scale the components of the colour by the given value
	 * 
	 * @param value The value to scale by
	 * @return The copy which has been scaled
	 */
	public Color scaleCopy(float value) {
		Color copy = new Color(r,g,b,a);
		copy.r *= value;
		copy.g *= value;
		copy.b *= value;
		copy.a *= value;
		
		return copy;
	}
	
	public Color scaleRGBCopy(float value) {
		Color copy = new Color(r,g,b,a);
		copy.r *= value;
		copy.g *= value;
		copy.b *= value;
		
		return copy;
	}
	
	public int getValue() {
		
		return 0xff000000 | ((int)(r * 255f) << 16) | ((int)(g * 255) << 8) | ((int)(b * 255f) << 0);
	}
	
	/**
	 * Converts an HSL color value to RGB. Conversion formula
	 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
	 * Assumes h, s, and l are contained in the set [0, 1] and
	 * returns r, g, and b in the set [0, 1].
	 * http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
	 *
	 * @param   Number  hue     The hue
	 * @param   Number  sat     The saturation
	 * @param   Number  light   The lightness
	 * @return  Array           The RGB representation
	 */
	public static Color fromHSL(float hue, float sat, float light) {
		
		return new Color(java.awt.Color.HSBtoRGB(hue, sat, light));
	}
	
	public static Color fromHue(float hue) {
		
		return fromHSL(hue, 1f, 1f);		
	}
	
	public float getHue() {
		
		return java.awt.Color.RGBtoHSB((int)(r * 255), (int)(g * 255), (int)(b * 255), null)[0];
	}
	
	public float getSaturation() {
		
		return java.awt.Color.RGBtoHSB((int)(r * 255), (int)(g * 255), (int)(b * 255), null)[1];
	}
	
	public float getLightness() {
		
		return java.awt.Color.RGBtoHSB((int)(r * 255), (int)(g * 255), (int)(b * 255), null)[2];
	}
	
	public static Color parse(String str) {
		
		str = str.trim();
		
		if (!str.startsWith("[Color")) { return null; }
		
		float r = 1f;
		float g = 1f;
		float b = 1f;
		float a = 1f;
		
		String s = "";
		
		try {
			s = str.substring(str.indexOf("r=")+2, str.indexOf(";", str.indexOf("r=")));
			r = (float)(Double.parseDouble(s));
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		try {
			s = str.substring(str.indexOf("g=")+2, str.indexOf(";", str.indexOf("g=")));
			g = (float)(Double.parseDouble(s));
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		try {
			s = str.substring(str.indexOf("b=")+2, str.indexOf(";", str.indexOf("b=")));
			b = (float)(Double.parseDouble(s));
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		try {
			s = str.substring(str.indexOf("a=")+2, str.indexOf(";", str.indexOf("a=")));
			a = (float)(Double.parseDouble(s));
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		return new Color(r, g, b, a);
		
	}
	
	public static Color grayTone(float gray) {
		
		return new Color(gray, gray, gray, 1f);
	}
	
	public static Color grayTone(float gray, float alpha) {
		
		return new Color(gray, gray, gray, alpha);
	}
	
	public static Color grayTone(int gray) {
		
		return new Color(gray, gray, gray, 255);
	}
	
	public static Color grayTone(int gray, int alpha) {
		
		return new Color(gray, gray, gray, alpha);
	}

}
