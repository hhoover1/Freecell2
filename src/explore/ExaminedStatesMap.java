package explore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freecellState.TableauHash;

public class ExaminedStatesMap implements Map<TableauHash, Integer> {
	private static final int MAX_COMPACTED_BYTEARRAY_SIZE = TableauHash.COMPACT_FORM_SIZE * 1000000;
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
				byte d = ba[((index + 1) * TableauHash.COMPACT_FORM_SIZE) - 1];
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

		int totalCompacted = 0;
		int compactedSize = toCompactList.size() * TableauHash.COMPACT_FORM_SIZE;
		byte[] compacted = null;
		int partOffset = 0;
		while (compactedSize > 0) {
			int partSize = Math.min(MAX_COMPACTED_BYTEARRAY_SIZE, compactedSize);
			int partCount = partSize / TableauHash.COMPACT_FORM_SIZE;
			List<Entry<TableauHash, Integer>> partList = toCompactList.subList(partOffset, partCount);
			compactedSize -= partSize;
			compacted = new byte[partSize];
			partList.sort(new EntryKeyComparer());
			int nextEntryOffset = 0;
			for (Entry<TableauHash, Integer> entry : toCompactList) {
				int offset = nextEntryOffset * TableauHash.COMPACT_FORM_SIZE;
				if (offset >= partSize) {
					break;
				}
				byte[] compact = entry.getKey().compactForm(entry.getValue());
				System.arraycopy(compact, 0, compacted, offset, compact.length);
				nextEntryOffset += 1;
			}

			if (compacted != null && compacted.length > 0) {
				_compactedTable = Arrays.copyOf(_compactedTable, _compactedTable.length + 1);
				_compactedTable[_compactedTable.length - 1] = compacted;
			}

			totalCompacted += nextEntryOffset;
		}

		System.out.println("finished compacting examinedStates - uncompacted now " + _uncompactedHashMap.size());

		for (int ii = 0; ii < _compactedTable.length - 1; ++ii) {
			if (_compactedTable[ii].length + _compactedTable[ii + 1].length < MAX_COMPACTED_BYTEARRAY_SIZE) {
				combineCompacteds(ii);
			}
		}

		return totalCompacted;
	}

	private void combineCompacteds(int lower) {
		byte[] combined = mergeSortCompacted(_compactedTable[lower], _compactedTable[lower + 1]);
		_compactedTable[lower] = combined;
		_compactedTable[lower + 1] = null;
		if (lower < _compactedTable.length - 2) {
			System.arraycopy(_compactedTable, lower + 2, _compactedTable, lower + 1, _compactedTable.length - lower - 1);
		}
		_compactedTable = Arrays.copyOf(_compactedTable, _compactedTable.length - 1);
	}

	private byte[] mergeSortCompacted(byte[] first, byte[] second) {
		int combinedSize = first.length + second.length;
		byte[] combined = new byte[combinedSize];
		int combinedOffset = 0;
		int firstOffset = TableauHash.COMPACT_FORM_SIZE;
		byte[] firstNext = new byte[TableauHash.COMPACT_FORM_SIZE];
		int secondOffset = TableauHash.COMPACT_FORM_SIZE;
		byte[] secondNext = new byte[TableauHash.COMPACT_FORM_SIZE];
		System.arraycopy(first, 0, firstNext, 0, firstNext.length);
		System.arraycopy(second, 0, secondNext, 0, secondNext.length);
		while (combinedOffset < combined.length) {
			int cmp;
			if ((cmp = compareBits(firstNext, secondNext)) <= 0) {
				System.arraycopy(firstNext, 0, combined, combinedOffset, firstNext.length);
				if (firstOffset < first.length) {
					System.arraycopy(first, firstOffset, firstNext, 0, firstNext.length);
				} else {
					System.arraycopy(erasedBits, 0, firstNext, 0, firstNext.length);
				}
				firstOffset += TableauHash.COMPACT_FORM_SIZE;
				combinedOffset += TableauHash.COMPACT_FORM_SIZE;
			} else if (cmp > 0) {
				System.arraycopy(secondNext, 0, combined, combinedOffset, secondNext.length);
				if (secondOffset < second.length) {
					System.arraycopy(second, secondOffset, secondNext, 0, secondNext.length);
				} else {
					System.arraycopy(erasedBits, 0, secondNext, 0, secondNext.length);
				}
				secondOffset += TableauHash.COMPACT_FORM_SIZE;
				combinedOffset += TableauHash.COMPACT_FORM_SIZE;
			}
		}

		return combined;
	}

	private int compareBits(byte[] bits1, byte[] bits2) {
		int result = 0;
		for (int ii = 0; ii < bits1.length; ++ii) {
			result = bits2[ii] - bits1[ii];
			if (result != 0) {
				return result;
			}
		}

		return result;
	}

	public int[] compactedStatistics() {
		int[] result = new int[1 + _compactedTable.length];
		result[0] = _uncompactedHashMap.size();
		for (int ii = 0; ii < _compactedTable.length; ++ii) {
			result[ii + 1] = _compactedTable[ii].length;
		}

		return result;
	}

	int findKey(byte[] ba, byte[] bits) {
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
			res = ba[tableOffset + offset] - b2[offset];
			offset += 1;
			if (res != 0) {
				return res;
			}
		}

		return 0;
	}

	private class EntryKeyComparer implements Comparator<Entry<TableauHash, Integer>> {

		@Override
		public int compare(Entry<TableauHash, Integer> o1, Entry<TableauHash, Integer> o2) {
			return o1.getKey().compareTo(o2.getKey());
		}
	}
}
