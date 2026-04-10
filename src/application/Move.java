package application;

class Move {
	int pieceType;
	Vector startSq;
	Vector endSq;
	boolean isCapture;
	boolean isEnPassant;
	boolean isCastling;
	int finalType;
	
	Move(int type, Vector start, Vector end, boolean capture,  boolean enPassant, boolean castling, int promoteTo) {  // Constructor
		pieceType = type;
		startSq = new Vector();
		startSq.copyOf(start);
		endSq = new Vector();
		endSq.copyOf(end);
		isCapture = capture;
		isEnPassant = enPassant;
		isCastling = castling;
		finalType = promoteTo;
	}
}