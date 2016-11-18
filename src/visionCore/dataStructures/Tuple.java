package visionCore.dataStructures;

public class Tuple <X, Y> {

	public X x;
	public Y y;
	
	public Tuple(X x, Y y) {
		
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o == this) { return true; }
		if (o == null || !(o instanceof Tuple)) { return false; }
		
		Tuple t = (Tuple)o;
		
		return t.x.equals(x) && t.y.equals(y);
	}
	
}
