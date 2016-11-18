package visionCore.dataStructures;

public class Triplet <X, Y, Z> {

	public X x;
	public Y y;
	public Z z;
	
	public Triplet(X x, Y y, Z z) {
		
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o == this) { return true; }
		if (o == null || !(o instanceof Triplet)) { return false; }
		
		@SuppressWarnings("rawtypes")
		Triplet t = (Triplet)o;
		
		return t.x.equals(x) && t.y.equals(y) && t.z.equals(z);
	}
	
}
