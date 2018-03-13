package freecellState;

public class Location {
	private static final int NO_ORIG_MASK = 0x3FFFF00;

	public enum Area {
		Foundation,
		Tableau,
		Freecell
	}
	
	private final int _locationBits;
/*	
	private final Area _area;
	private final byte _column;
	private final byte _offset;
	private final byte _origColumn;
*/
	
	public Location(Area a, int col) {
		int calcBits = a.ordinal()<<24;
		calcBits |= ((byte) col) << 16;
		_locationBits = calcBits;
	}
	
	public Location(Area a, int col, int off, int origCol) {
		int calcBits = a.ordinal()<<24;
		calcBits |= ((byte) col & 0x0F) << 16;
		calcBits |= ((byte) off & 0x3F) << 8;
		calcBits |= ((byte) origCol & 0x0F);
		_locationBits = calcBits;
	}
	
	public Area area() {
		return Area.values()[(_locationBits >>> 24) & 0x3];
	}
	
	public int column() {
		return (_locationBits >>> 16) & 0x0F;
	}
	
	public int offset() {
		return (_locationBits >>> 8) & 0x3F;
	}
	
	public int originalColumn() {
		return _locationBits & 0x0F;
	}
	
	@Override
	public int hashCode() {
		int result = ((this.area().ordinal() + 1) * 1013) << 13;
		result += (this.column() + 1) * 1013;
		result += this.offset();
		
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Location)) {
			return false;
		}


		Location l = (Location) o;
/*		if (this._area != l._area) {
			return false;
		}
		
		if (this._column != l._column) {
			return false;
		}
		
		return this._offset == l._offset;
		*/
		return (this._locationBits & NO_ORIG_MASK) == (l._locationBits & NO_ORIG_MASK);
	}
	
	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		sb.append(area());
		sb.append("(");
		sb.append(originalColumn());
		sb.append("), ");
		sb.append(column());
		sb.append(", ");
		sb.append(offset());
		sb.append(')');
		return sb.toString();
	}

	public String debugShortName() {
		StringBuilder sb = new StringBuilder();
		sb.append(area().name().substring(0, 2));
		sb.append(column());
		sb.append(offset());
		return sb.toString();
	}
	
	public String shortName() {
		StringBuilder sb = new StringBuilder();
		sb.append(area().name().substring(0, 2));
		if (area() == Area.Tableau) {
			sb.append(originalColumn());
		} else {
			sb.append(column());
		}
		
		sb.append(offset());
		return sb.toString();
	}
}
