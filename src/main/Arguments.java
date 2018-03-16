package main;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Arguments {
	public boolean doStatistics;
	public boolean validation;
	public int flushDotInterval;
	public int intermediateDepth;
	public int maxExploreDepth;
	public int moveTreeQueueLength;
	public int parallelTops;
	public long randomSelectionInterval;
	public long statusUpdateInterval;
	public long statisticsLogInterval;
	public long tableauPrintInterval;
	public String deckString;
	public String statisticsLogName;
	private static Arguments _singleton = null;

	private final HashMap<String, Object> arguments = new HashMap<String, Object>();

	public static Arguments arguments() {
		if (_singleton == null) {
			_singleton = new Arguments();
		}
		return _singleton;
	}
	
	private Arguments() {
		
	}
	
	public void parseArgs(String[] args) {
		if (args.length == 0) {
			return;
		}

		int argIdx = 0;
		while (argIdx < args.length - 1) {
			String key = args[argIdx++].substring(1);
			while (key.charAt(0) == '-') {
				key = key.substring(1);
			}

			Field field = findField(key);
			if (field != null) {
				if (this.setField(key, field, args[argIdx])) {
					argIdx += 1;
				}
			} else {

				String a2 = null;
				if (argIdx < args.length - 1 && args[argIdx].charAt(0) != '-') {
					a2 = args[argIdx++];
				}

				if (a2 != null) {
					char lastChar = a2.charAt(a2.length() - 1);
					try {
						if (lastChar == 'l') {
							long l = Long.parseLong(a2);
							arguments.put(key, new Long(l));
						} else if (lastChar == 'i') {
							int i = Integer.parseInt(a2);
							arguments.put(key, new Integer(i));
						} else {
							arguments.put(key, a2);
						}
					} catch (NumberFormatException nfe) {
						arguments.put(key, a2);
					}
				} else {
					arguments.put(key, null);
				}
			}
		}
	}

	public String getString(String argName) {
		return (String) arguments.get(argName);
	}

	public long getLong(String argName) {
		return (long) arguments.get(argName);
	}

	public int getInt(String argName) {
		return (int) arguments.get(argName);
	}

	public boolean getBoolean(String argName) {
		return (boolean) arguments.get(argName);
	}

	public void putArg(String argName, Object what) {
		arguments.put(argName, what);
	}

	private Field findField(String key) {
		for (Field f : this.getClass().getFields()) {
			if (f.getName().equalsIgnoreCase(key)) {
				return f;
			}
		}

		return null;
	}

	private boolean setField(String key, Field field, String value) {
		boolean retVal = false;
		String fType = field.getType().getName();
		try {
			if (field.getType().isPrimitive()) {
				if (field.getType() == int.class) {
					int val = Integer.parseInt(value);
					field.setInt(this, val);
					retVal = true;
				} else if (field.getType() == long.class) {
					long val = Long.parseLong(value);
					field.setLong(this, val);
					retVal = true;
				} else if (field.getType() == boolean.class) {
					if (value.charAt(0) == '-') {
						field.setBoolean(this, true);
					} else {
						boolean val = Boolean.parseBoolean(value);
						field.setBoolean(this, val);
						retVal = true;
					}
				} else if (field.getType() == short.class) {
					short val = Short.parseShort(value);
					field.setShort(this, val);
					retVal = true;
				} else if (field.getType() == byte.class) {
					byte val = Byte.parseByte(value);
					field.setByte(this, val);
					retVal = true;
				} else if (field.getType() == char.class) {
					char val = value.charAt(0);
					field.setChar(this, val);
					retVal = true;
				} else if (field.getType() == float.class) {
					float val = Float.parseFloat(value);
					field.setFloat(this, val);
					retVal = true;
				} else if (field.getType() == double.class) {
					double val = Double.parseDouble(value);
					field.setDouble(this, val);
					retVal = true;
				} else {
					System.err.println("unhandled field type " + fType);
				}
			} else if (fType.equals("java.lang.String")) {
				field.set(this, value);
				retVal = true;
			} else {
				System.err.println("unhandled field type: " + fType);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return retVal;
	}
}
