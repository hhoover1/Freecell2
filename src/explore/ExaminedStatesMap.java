package explore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import freecellState.TableauHash;

public class ExaminedStatesMap implements Map<TableauHash, Integer> {
	private static final byte[] erasedBits = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	HashMap<TableauHash, Integer> _uncompactedHashMap;
	byte[][] _compactedTable;

	public ExaminedStatesMap(int predictedSize) {
		_uncompactedHashMap = new HashMap<TableauHash, Integer>(predictedSize);
		_compactedTable = new byte[0][0];
	}

	@Override
	public int size() {
		int compactedSize = 0;
		for (byte[] subTable : _compactedTable) {
			compactedSize += subTable.length / TableauHash.COMPACT_FORM_SIZE;
		}

		return _uncompactedHashMap.size() + compactedSize;
	}

	@Override
	public boolean isEmpty() {
		return _uncompactedHashMap.isEmpty() && _compactedTable.length == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		if (_uncompactedHashMap.containsKey(key)) {
			return true;
		}

		byte[] bits = ((TableauHash) key).compactForm(0);
		for (byte[] ba : _compactedTable) {
			int index = this.findKey(ba, bits);
			return index >= 0;
		}

		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		if (_uncompactedHashMap.containsValue(value)) {
			return true;
		}

		return false;
	}

	@Override
	public Integer get(Object key) {
		if (_uncompactedHashMap.containsKey(key)) {
			return _uncompactedHashMap.get(key);
		}

		byte[] bits = ((TableauHash) key).compactForm(0);
		for (byte[] ba : _compactedTable) {
			int index = this.findKey(ba, bits);
			if (index >= 0) {
				byte d = ba[index * TableauHash.COMPACT_FORM_SIZE - 1];
				return new Integer(d);
			}
		}

		return null;
	}

	@Override
	public Integer put(TableauHash key, Integer value) {
		byte[] bits = key.compactForm(0);
		for (byte[] ba : _compactedTable) {
			int oldValue = -1;
			int index = this.findKey(ba, bits);
			if (index >= 0) {
				int offset = ((index + 1) * TableauHash.COMPACT_FORM_SIZE) - 1;
				oldValue = ba[offset];
				ba[offset] = (byte) (int) value;
			}

			if (oldValue >= 0) {
				return new Integer(oldValue);
			}
		}

		return _uncompactedHashMap.put(key, value);
	}

	@Override
	public Integer remove(Object key) {
		if (_uncompactedHashMap.containsKey(key)) {
			return (Integer) _uncompactedHashMap.remove(key);
		}

		for (byte[] ba : _compactedTable) {
			byte[] bits = ((TableauHash) key).compactForm(0);
			int index = this.findKey(ba, bits);
			if (index >= 0) {
				int offset = (index * TableauHash.COMPACT_FORM_SIZE);
				byte depth = ba[offset + TableauHash.COMPACT_FORM_SIZE - 1];
				System.arraycopy(erasedBits, 0, ba, offset, erasedBits.length);
				return new Integer(depth);
			}
		}

		return null;
	}

	@Override
	public void putAll(Map<? extends TableauHash, ? extends Integer> m) {
	}

	@Override
	public void clear() {
		_uncompactedHashMap.clear();
		_compactedTable = new byte[0][0];
	}

	@Override
	public Set<TableauHash> keySet() {
		return null;
	}

	@Override
	public Collection<Integer> values() {
		return null;
	}

	@Override
	public Set<Entry<TableauHash, Integer>> entrySet() {
		return null;
	}

	public int compactExaminedStates(int maxDepth) {
		// first, create a list of Entry ordered by Entry.value (depth)
		// next, create a huge byte array - number of entries *
		// TableauHash.COMPACT_FORM_SIZE;
		// finally, take each TableauHash, get compact form, copy to huge array.
		// if the huge array is too big, we can split it into pages of a few megabytes
		// each?
		ArrayList<Entry<TableauHash, Integer>> toCompactList = new ArrayList<Entry<TableauHash, Integer>>(
				_uncompactedHashMap.size());
		ArrayList<Entry<TableauHash, Integer>> notCompactedList = new ArrayList<Entry<TableauHash, Integer>>(
				_uncompactedHashMap.size());
		for (Entry<TableauHash, Integer> entry : _uncompactedHashMap.entrySet()) {
			if (entry.getValue() <= maxDepth) {
				toCompactList.add(entry);
			} else {
				notCompactedList.add(entry);
			}
		}

		_uncompactedHashMap.clear();
		for (Entry<TableauHash, Integer> entry : notCompactedList) {
			_uncompactedHashMap.put(entry.getKey(), entry.getValue());
		}

		byte[] compacted = new byte[toCompactList.size() * TableauHash.COMPACT_FORM_SIZE];
		toCompactList.sort(new EntryComparer());
		int nextEntryOffset = 0;
		for (Entry<TableauHash, Integer> entry : toCompactList) {
			int offset = nextEntryOffset * TableauHash.COMPACT_FORM_SIZE;
			byte[] compact = entry.getKey().compactForm(entry.getValue());
			System.arraycopy(compact, 0, compacted, offset, TableauHash.COMPACT_FORM_SIZE);
			nextEntryOffset += 1;
		}

		_compactedTable = Arrays.copyOf(_compactedTable, _compactedTable.length + 1);
		_compactedTable[_compactedTable.length - 1] = compacted;

		System.out.println("finished compacting examinedStates - now " + _uncompactedHashMap.size());

		return nextEntryOffset;
	}
	
	public int[] compactedStatistics() {
		int[] result = new int[1+_compactedTable.length];
		result[0] = _uncompactedHashMap.size();
		for (int ii = 0; ii < _compactedTable.length; ++ii) {
			result[ii + 1] = _compactedTable[ii].length;
		}
		
		return result;
	}

	private int findKey(byte[] ba, byte[] bits) {
		int upper = (ba.length / TableauHash.COMPACT_FORM_SIZE) - 1;
		int lower = 0;
		while (lower <= upper) {
			int probe = lower + Math.floorDiv(upper - lower, 2);
			int cmp = this.compareCompacts(ba, probe, bits);
			if (cmp < 0) {
				lower = probe + 1;
			} else if (cmp > 0) {
				upper = probe - 1;
			} else {
				return probe;
			}
		}

		return -1;
	}

	private int compareCompacts(byte[] ba, int i1, byte[] b2) {
		int res = 0;
		int offset = 0;
		int tableOffset = i1 * TableauHash.COMPACT_FORM_SIZE;
		while (res == 0 && offset < b2.length - 1) { // don't compare the depth byte.
			res = b2[offset] - ba[tableOffset + offset];
			offset += 1;
			if (res != 0) {
				return res;
			}
		}

		return 0;
	}

	private class EntryComparer implements Comparator<Entry<TableauHash, Integer>> {

		@Override
		public int compare(Entry<TableauHash, Integer> o1, Entry<TableauHash, Integer> o2) {
			return o1.getValue() - o2.getValue();
		}
	}
}
