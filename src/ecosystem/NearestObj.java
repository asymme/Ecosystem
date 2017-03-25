package ecosystem;

public class NearestObj {
	public Boolean isHit;
	public double distance;
	public int idx;
	public NearestObj() {
		// コンストラクタ
		this.isHit = false;
		this.distance = Double.MAX_VALUE;
		this.idx = -1;
	}
}
