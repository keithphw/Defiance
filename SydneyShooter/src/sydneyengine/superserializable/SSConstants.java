package sydneyengine.superserializable;

interface SSConstants{

	//static final Object SKIP_FIELD = new Object();	// used by SSObjectInputStream.attemptReadNonSS(int);

	static final int NULL_VALUE = -1;
	static final int FLOAT = -2;
	static final int INT = -3;
	static final int BOOLEAN = -4;
	static final int LONG = -5;
	static final int DOUBLE = -6;
	static final int SHORT = -7;
	static final int BYTE = -8;
	static final int CHARACTER = -9;

	static final int IGNORE_THESE = -10;	// in read/writeSS methods, signifies that the following SSObjects should be ignored by having their read/writeStatus set as true
	static final int IGNORE_END = -11;		// signifies the end of the above

	static final int STRING_VALUE = -12;
	//static final int STRING_BUILDER_VALUE = -13;
	static final int ARRAY_VALUE = -14;
	static final int POINT2D_FLOAT_VALUE = -15;
	static final int COLOR_VALUE = -16;
	static final int FLOAT_VALUE = -17;
	static final int INT_VALUE = -18;
	static final int BOOLEAN_VALUE = -19;
	static final int LONG_VALUE = -20;
	static final int DOUBLE_VALUE = -21;
	static final int SHORT_VALUE = -22;
	static final int BYTE_VALUE = -23;
	static final int CHARACTER_VALUE = -24;

	static final short CLASS_INDEX_START_AUTO = -30;	// all values less than this number are reserved for class-indexes
	static final short CLASS_INDEX_START_INSTALL = Short.MIN_VALUE;
}