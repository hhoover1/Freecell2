/**
 * 
 */
package deck;

/**
 * @author hhoover
 *
 */
public class Clib_Random_JNI {
	private int seed;
	
	native int clib_rand();
	static {
		System.loadLibrary("clib_random");
	}
	
	public Clib_Random_JNI(int s) {
		this.seed = s;
	}
	
	public int next() {
		Clib_Random_JNI clr = new Clib_Random_JNI(this.seed);
		return clr.clib_rand();
	}
}
