package freecellState;

public class Location {
	public enum Area {
		Foundation,
		Tableau,
		Freecell
	}
	
	private final Area _area;
	private final int _column;
	private final int _offset;
	
	public Location(Area a, int col, int off) {
		_area = a;
		_column = col;
		_offset = off;
	}
	
	public Area area() {
		return _area;
	}
	
	public int column() {
		return _column;
	}
	
	public int offset() {
		return _offset;
	}
	
	@Override
	public int hashCode() {
		int result = this._area.hashCode();
		result += (this._column + 1) * 1013;
		result += this._offset;
		
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Location)) {
			return false;
		}
		
		Location l = (Location) o;
		if (this._area != l._area) {
			return false;
		}
		
		if (this._column != l._column) {
			return false;
		}
		
		return this._offset == l._offset;
	}
	
	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		sb.append(_area);
		sb.append(", ");
		sb.append(_column);
		sb.append(", ");
		sb.append(_offset);
		sb.append(')');
		return sb.toString();
	}

	public String shortName() {
		//String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(/*(cname.substring(cname.lastIndexOf('.') + 1)).substring(0, 3)*/);
		//sb.append('(');
		sb.append(_area.name().substring(0, 2));
		sb.append(_column);
		sb.append(_offset);
		//sb.append(')');
		return sb.toString();
	}
}
