/**
 * 
 */
package deck;

/**
 * @author hhoover
 *
 */
public class MSFT_Random implements Random {
	private int seed;
	
	public MSFT_Random(int s) {
		this.seed = s;
	}
	
	public int next() {
		this.seed = this.seed * 214013 + 2531011;
		return (this.seed >> 16) & 0x7FFF;
	}
}
