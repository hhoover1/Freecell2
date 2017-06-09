package explore;

public class ArrayIterator<T> {
	private final T[] _array;
	private int _idx = 0;
	
	public ArrayIterator(T[] a) {
		_array = a;
	}
	
	public boolean hasNext() {
		return _idx < _array.length;
	}
	
	public T next() {
		return _array[_idx++];
	}
}
