package main;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ArgumentsTest {
	private static final String PARSE_BOOLEAN_VALUE = "false";
	private static final String PARSE_STRING_VALUE = "DECK_STRING_WOWEE";
	private static final String PARSE_LONG_VALUE = "1000000";
	private static final String PARSE_INT_VALUE = "1762";
	private static final String PUT_ARG_VALUE = "PutArgValue";
	private static final String PUT_ARG_KEY = "PUT_ARG_KEY";
	private static final boolean ARG_BOOLEAN_FALSE = false;
	private static final String ARG_BOOLEAN_FALSE_KEY = "testArgBooleanFalse";
	private static final boolean ARG_BOOLEAN = true;
	private static final String ARG_BOOLEAN_KEY = "testArgBoolean";
	private static final int ARG_INT = 100000;
	private static final String ARG_INT_KEY = "testArgInt";
	private static final long ARG_LONG = 100l;
	private static final String ARG_LONG_KEY = "testArgLong";
	private static final String STRING_ARG_KEY = "testArgString";
	private static final String STRING_ARG = "stringArg";
	private static final String[] TEST_PARAMETERS = {
			"-maxExploreDepth", PARSE_INT_VALUE,
			"--statusUpdateInterval", PARSE_LONG_VALUE,
			"--deckString", PARSE_STRING_VALUE,
			"-doStatistics", PARSE_BOOLEAN_VALUE
	};
	
	Arguments args = new Arguments();
	
	@Before
	public final void setup() {
		args.putArg(ARG_LONG_KEY, ARG_LONG);
		args.putArg(ARG_INT_KEY, ARG_INT);
		args.putArg(ARG_BOOLEAN_KEY, ARG_BOOLEAN);
		args.putArg(ARG_BOOLEAN_FALSE_KEY, ARG_BOOLEAN_FALSE);
		args.putArg(STRING_ARG_KEY, STRING_ARG);
		args.doStatistics = true;
	}
	
	@Test
	public final void testParseArgs() {
		args.parseArgs(TEST_PARAMETERS);
		assertEquals(Integer.parseInt(PARSE_INT_VALUE), args.maxExploreDepth);
		assertEquals(Long.parseLong(PARSE_LONG_VALUE), args.statusUpdateInterval);
		assertEquals(PARSE_STRING_VALUE, args.deckString);
		assertEquals(Boolean.parseBoolean(PARSE_BOOLEAN_VALUE), args.doStatistics);
	}

	@Test
	public final void testGetString() {
		assertEquals(STRING_ARG, args.getString(STRING_ARG_KEY));
	}

	@Test
	public final void testGetLong() {
		assertEquals(ARG_LONG, args.getLong(ARG_LONG_KEY));
	}

	@Test
	public final void testGetInt() {
		assertEquals(ARG_INT, args.getInt(ARG_INT_KEY));
	}
	
	@Test
	public final void testGetBoolean() {
		assertEquals(ARG_BOOLEAN, args.getBoolean(ARG_BOOLEAN_KEY));
	}

	@Test
	public final void testPutArg() {
		args.putArg(PUT_ARG_KEY, PUT_ARG_VALUE);
		assertEquals(PUT_ARG_VALUE, args.getString(PUT_ARG_KEY));
	}

}
