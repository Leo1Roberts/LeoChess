package application;

final class Constants {
	private Constants() {};
	
	// Piece types
	static final int NONE = 0;
	static final int PAWN = 1;
	static final int KNIGHT = 2;
	static final int BISHOP = 3;
	static final int ROOK = 4;
	static final int QUEEN = 5;
	static final int KING = 6;
	
	// Game states
	static final int NORMAL = 10;
	static final int CHECK = 11;
	static final int CHECKMATE = 12;
	static final int STALEMATE = 13;
	static final int REPETITION = 14;
	static final int FIFTYMOVE = 15;
	static final int INSUF = 16;
	static final int WHITETIMEOUT = 17;
	static final int BLACKTIMEOUT = 18;
	static final int WHITERESIGNED = 19;
	static final int BLACKRESIGNED = 20;
	static final int DRAWAGREED = 21;
	
	// Bitmasks
	static final int BM_TYPE = 0xff;  // Lowest byte
	static final int BM_SPRITE = 0xff00;  // Second lowest byte
	static final int BM_ISWHITE = 0x10000;  // First bit of the third byte
	
	// GameSetup options
	static final int WHITE = 30;
	static final int BLACK = 31;
	static final int WHITETOGGLE = 32;
	static final int BLACKTOGGLE = 33;
	static final int RANDOM = 34;
	
	static final int HINTOFF = 40;
	static final int HINTPARTIAL = 41;
	static final int HINTFULL = 42;
}