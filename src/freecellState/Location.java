package freecellState;

public class Location implements Comparable<Location> {
	public enum Area {
		Foundation, Tableau, Freecell
	}

	private final Area _area;
	private final byte _column;
	private final byte _offset;
	private final byte _origColumn;

	public Location(Area a, int col) {
		_area = a;
		_column = (byte) col;
		_offset = 0;
		_origColumn = -1;
	}

	public Location(Area a, int col, int off, int origCol) {
		_area = a;
		_column = (byte) col;
		_offset = (byte) off;
		_origColumn = (byte) origCol;
	}
	
	public Location(Location other) {
		_area = other._area;
		_column = other._column;
		_offset = other._offset;
		_origColumn = other._origColumn;
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

	public int originalColumn() {
		return _origColumn;
	}

	@Override
	public int hashCode() {
		int result = ((this._area.ordinal() + 1) * 1013) << 13;
		result += (_column + 1) * 1013 << 5;
		result += _offset;

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
		sb.append(area());
		if (_origColumn != -1) {
			sb.append("(");
			sb.append(_origColumn);
			sb.append("), ");
		} else {
			sb.append(' ');
		}
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
			sb.append(_origColumn);
		} else {
			sb.append(column());
		}

		sb.append(offset());
		return sb.toString();
	}

	@Override
	public int compareTo(Location o) {
		int res = _area.ordinal() - o._area.ordinal();
		if (res != 0) {
			return res;
		}
		
		res = _column - o._column;
		if (res != 0) {
			return res;
		}
		
		res = _offset - o._offset;
		return res;
	}
}
