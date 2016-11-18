package visionCore.dataStructures;

public class Quad <X, Y, Z, W> {

	public X x;
	public Y y;
	public Z z;
	public W w;
	
	public Quad(X x, Y y, Z z, W w) {
		
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o == this) { return true; }
		if (o == null || !(o instanceof Quad)) { return false; }
		
		@SuppressWarnings("rawtypes")
		Quad t = (Quad)o;
		
		return t.x.equals(x) && t.y.equals(y) && t.z.equals(z) && t.w.equals(w);
	}
	
}
