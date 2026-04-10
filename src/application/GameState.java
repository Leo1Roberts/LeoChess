package application;

import static application.Constants.*;

class GameState {
	int[] board;
	boolean whiteToMove;
	int halfMoves;
	int fiftyMoveCount;
	Vector wKingPos;
	Vector bKingPos;
	Vector enPassantTarget;
	boolean wKingCastle;
	boolean wQueenCastle;
	boolean bKingCastle;
	boolean bQueenCastle;
	
	static final Vector[] slidingVecs = {new Vector(0, 1), new Vector(1, 1), new Vector(1, 0), new Vector(1, -1), new Vector(0, -1), new Vector(-1, -1), new Vector(-1, 0), new Vector(-1, 1)};
	static final Vector[] knightVecs = {new Vector(1, 2), new Vector(2, 1), new Vector(2, -1), new Vector(1, -2), new Vector(-1, -2), new Vector(-2, -1), new Vector(-2, 1), new Vector(-1, 2)};
	
	GameState() {  // Constructor
		board = new int[64];
		wKingPos = new Vector();
		bKingPos = new Vector();
		enPassantTarget = new Vector();
	}
	
	GameState(GameState copyee) {  // Constructor
		board = new int[64];
		wKingPos = new Vector();
		bKingPos = new Vector();
		enPassantTarget = new Vector();
		copyOf(copyee);
	}
	
	void copyOf(GameState src) {  // Makes this GameState an exact duplicate of the source
		board = src.board.clone();  // .clone() creates an array copied by value, not reference
		whiteToMove = src.whiteToMove;
		halfMoves = src.halfMoves;
		fiftyMoveCount = src.fiftyMoveCount;
		wKingPos.copyOf(src.wKingPos);
		bKingPos.copyOf(src.bKingPos);
		enPassantTarget.copyOf(src.enPassantTarget);
		wKingCastle = src.wKingCastle;
		wQueenCastle = src.wQueenCastle;
		bKingCastle = src.bKingCastle;
		bQueenCastle = src.bQueenCastle;
	}

	void setPiece(int x, int y, int type, int spriteId, boolean isWhite) {
		int piece = type;  // Piece type goes in the lowest byte
		piece |= spriteId << 8;  // Sprite ID goes in the second lowest byte
		if (isWhite) piece |= BM_ISWHITE;
		board[y * 8 + x] = piece;
	}
	void setPiece(Vector pos, int type, int spriteId, boolean isWhite) {
		int piece = type;
		piece |= spriteId << 8;
		if (isWhite) piece |= BM_ISWHITE;
		board[pos.y * 8 + pos.x] = piece;
	}
	void setPiece(int x, int y, int piece) {
		board[y * 8 + x] = piece;
	}
	void setPiece(Vector pos, int piece) {
		board[pos.y * 8 + pos.x] = piece;
	}
	
	int getPiece(int x, int y) {
		return board[y * 8 + x];
	}
	int getPiece(Vector pos) {
		return board[pos.y * 8 + pos.x];
	}
	
	int type(int x, int y) {
		return board[y * 8 + x] & BM_TYPE;
	}
	int type(Vector pos) {
		return board[pos.y * 8 + pos.x] & BM_TYPE;
	}
	int type(int piece) {
		return piece & BM_TYPE;
	}
	
	int getSprite(int x, int y) {
		return (board[y * 8 + x] & BM_SPRITE) >> 8;
	}
	int getSprite(Vector pos) {
		return (board[pos.y * 8 + pos.x] & BM_SPRITE) >> 8;
	}
	int getSprite(int piece) {
		return (piece & BM_SPRITE) >> 8;
	}
	
	boolean isWhite(int x, int y) {
		return (board[y * 8 + x] & BM_ISWHITE) != 0;
	}
	boolean isWhite(Vector pos) {
		return (board[pos.y * 8 + pos.x] & BM_ISWHITE) != 0;
	}
	boolean isWhite(int piece) {
		return (piece & BM_ISWHITE) != 0;
	}
	
	boolean occupied(int x, int y) {  // Returns true if there's a piece on the square
		return board[y * 8 + x] != 0;
	}
	boolean occupied(Vector pos) {
		return board[pos.y * 8 + pos.x] != 0;
	}
	
	void empty(int x, int y) {
		//assert (x >= 0 && x < 8 && y >= 0 && y < 8) : "x/y out of range";
		board[y * 8 + x] = 0;
	}
	void empty(Vector pos) {
		board[pos.y * 8 + pos.x] = 0;
	}
	
	void init() {  // Sets up the standard starting position
		setPiece(0, 0, ROOK, 0, true);
		setPiece(1, 0, KNIGHT, 1, true);
		setPiece(2, 0, BISHOP, 2, true);
		setPiece(3, 0, QUEEN, 3, true);
		setPiece(4, 0, KING, 4, true);
		setPiece(5, 0, BISHOP, 5, true);
		setPiece(6, 0, KNIGHT, 6, true);
		setPiece(7, 0, ROOK, 7, true);
		
		setPiece(0, 1, PAWN, 8, true);
		setPiece(1, 1, PAWN, 9, true);
		setPiece(2, 1, PAWN, 10, true);
		setPiece(3, 1, PAWN, 11, true);
		setPiece(4, 1, PAWN, 12, true);
		setPiece(5, 1, PAWN, 13, true);
		setPiece(6, 1, PAWN, 14, true);
		setPiece(7, 1, PAWN, 15, true);
		
		setPiece(0, 6, PAWN, 16, false);
		setPiece(1, 6, PAWN, 17, false);
		setPiece(2, 6, PAWN, 18, false);
		setPiece(3, 6, PAWN, 19, false);
		setPiece(4, 6, PAWN, 20, false);
		setPiece(5, 6, PAWN, 21, false);
		setPiece(6, 6, PAWN, 22, false);
		setPiece(7, 6, PAWN, 23, false);
		
		setPiece(0, 7, ROOK, 24, false);
		setPiece(1, 7, KNIGHT, 25, false);
		setPiece(2, 7, BISHOP, 26, false);
		setPiece(3, 7, QUEEN, 27, false);
		setPiece(4, 7, KING, 28, false);
		setPiece(5, 7, BISHOP, 29, false);
		setPiece(6, 7, KNIGHT, 30, false);
		setPiece(7, 7, ROOK, 31, false);
		
		whiteToMove = true;
		halfMoves = 0;
		fiftyMoveCount = 0;
		wKingPos.setXY(4, 0);
		bKingPos.setXY(4, 7);
		enPassantTarget.setXY(-1, -1);
		wKingCastle = true;
		wQueenCastle = true;
		bKingCastle = true;
		bQueenCastle = true;
	}
	
	boolean inCheck(boolean whiteKing) {
		Vector kingPos;
		if (whiteKing) kingPos = wKingPos;
		else kingPos = bKingPos;
		Vector attackerPos = new Vector();
		
		boolean diagonal = false;  // Used to distinguish between rook-style and bishop-style moves
		// Sending out 'rays' from the king's square to find attackers
		for (Vector dir : slidingVecs) {
			attackerPos.copyOf(kingPos);
			attackerPos.add(dir);
			while (attackerPos.onBoard()) {
				int type = type(attackerPos);
				if (type != NONE) {  // Square is occupied
					if (isWhite(attackerPos) != whiteKing) {  // Piece is opposite colour to king
						if (type == QUEEN || (type == ROOK && !diagonal) || (type == BISHOP && diagonal)) {
							return true;  // The king is in check
						}
					}
					break;  // Move on to next ray if piece is same colour as king; ray is blocked
				}
				attackerPos.add(dir);  // Look at the next square along the ray
			}
			diagonal = !diagonal;  // Vectors are in (clockwise) rotating order so toggle between diagonal and not
		}
		for (Vector dir : knightVecs) {
			attackerPos.copyOf(kingPos);
			attackerPos.add(dir);
			if (attackerPos.onBoard()) {
				if (type(attackerPos) == KNIGHT) {
					if (isWhite(attackerPos) != whiteKing) {
						return true;
					}
				}
			}
		}
		if (whiteKing) {  // Pawns have direction, so vectors are different for white and black
			attackerPos.copyOf(kingPos);
			attackerPos.add(-1, 1);
			if (attackerPos.onBoard()) {
				if (type(attackerPos) == PAWN) {
					if (isWhite(attackerPos) != whiteKing) {
						return true;
					}
				}
			}
			attackerPos.copyOf(kingPos);
			attackerPos.add(1, 1);
			if (attackerPos.onBoard()) {
				if (type(attackerPos) == PAWN) {
					if (isWhite(attackerPos) != whiteKing) {
						return true;
					}
				}
			}
		} else {  // Black king; white pawns
			attackerPos.copyOf(kingPos);
			attackerPos.add(-1, -1);
			if (attackerPos.onBoard()) {
				if (type(attackerPos) == PAWN) {
					if (isWhite(attackerPos) != whiteKing) {
						return true;
					}
				}
			}
			attackerPos.copyOf(kingPos);
			attackerPos.add(1, -1);
			if (attackerPos.onBoard()) {
				if (type(attackerPos) == PAWN) {
					if (isWhite(attackerPos) != whiteKing) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	Move[] pawnMoves(Vector startSq) {
		Move[] allMoves = new Move[13];  // 12 is the maximum number of possible moves for a pawn
		if (isWhite(startSq) != whiteToMove) return allMoves;  // No legal moves if it's not your turn
		int index = 0;  // Current empty slot in allMoves to add a move
		boolean inCheck = inCheck(whiteToMove);
		Vector endSq = new Vector();
		int originalPawn;
		int capturedPiece;
		boolean tryDouble = true;  // For optimisation; bother testing moves where the pawn moves 2 squares vertically
		
		if (whiteToMove) {  // White pawns move 'up' the board (y increases)
			endSq.copyOf(startSq);
			endSq.add(0, 1);
			if (endSq.onBoard() && !occupied(endSq)) {
				originalPawn = getPiece(startSq);
				setPiece(endSq, originalPawn);  // Move the pawn
				empty(startSq);  // Start square is now empty, the pawn has moved
				if (!inCheck(true)) {  // The move is legal, add it to the list
					if (endSq.y == 7) {  // Special case for promotion
						// Sprite IDs don't matter for these (they are temporary), so they are all 0
						setPiece(endSq, QUEEN, 0, true);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, QUEEN);
						setPiece(endSq, ROOK, 0, true);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, ROOK);
						setPiece(endSq, KNIGHT, 0, true);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, KNIGHT);
						setPiece(endSq, BISHOP, 0, true);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, BISHOP);
					} else {
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, PAWN);
					}
					if (inCheck) tryDouble = false;  // The move blocked a check, so a double-push will be illegal
				} else {
					if (!inCheck) tryDouble = false;  // The pawn is pinned, so a double-push will be illegal
				}
				setPiece(startSq, originalPawn);  // Move the pawn back
				empty(endSq);  // Remove its potential 'future self'
				if (tryDouble && startSq.y == 1) {  // Double-push move
					endSq.add(0, 1);
					if (!occupied(endSq)) {
						setPiece(endSq, originalPawn);  // Move the pawn
						empty(startSq);
						if (!inCheck(true)) {  // The move is legal, add it to the list
							allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, PAWN);
						}
						setPiece(startSq, originalPawn);  // Move the pawn back
						empty(endSq);  // Remove its potential 'future self'
					}
				}
			}
			endSq.copyOf(startSq);
			endSq.add(-1, 1);  // 'Left' capture
			if (endSq.onBoard()) {
				if (occupied(endSq) && (isWhite(endSq) == false)) {  // White pawns capture black pieces
					capturedPiece = getPiece(endSq);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					if (!inCheck(true)) {  // The move is legal, add it to the list
						if (endSq.y == 7) {  // Special case for promotion
							// Sprite IDs don't matter for these (they are temporary), so they are all 0
							setPiece(endSq, QUEEN, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, QUEEN);
							setPiece(endSq, ROOK, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, ROOK);
							setPiece(endSq, KNIGHT, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, KNIGHT);
							setPiece(endSq, BISHOP, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, BISHOP);
						} else {
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, PAWN);
						}
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq, capturedPiece);  // Replace the captured piece
				} else if (endSq.equals(enPassantTarget)) {
					capturedPiece = getPiece(endSq.x, startSq.y);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					empty(endSq.x, startSq.y);  // Capturing the en-passanted piece
					if (!inCheck(true)) {  // The move is legal, add it to the list
						allMoves[index++] = new Move(PAWN, startSq, endSq, true, true, false, PAWN);
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq.x, startSq.y, capturedPiece);  // Replace the captured piece
					empty(endSq);  // Remove its potential 'future self'
				}
			}
			endSq.add(2, 0);  // 'Right' capture
			if (endSq.onBoard()) {
				if (occupied(endSq) && (isWhite(endSq) == false)) {  // White pawns capture black pieces
					capturedPiece = getPiece(endSq);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					if (!inCheck(true)) {  // The move is legal, add it to the list
						if (endSq.y == 7) {  // Special case for promotion
							// Sprite IDs don't matter for these (they are temporary), so they are all 0
							setPiece(endSq, QUEEN, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, QUEEN);
							setPiece(endSq, ROOK, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, ROOK);
							setPiece(endSq, KNIGHT, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, KNIGHT);
							setPiece(endSq, BISHOP, 0, true);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, BISHOP);
						} else {
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, PAWN);
						}
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq, capturedPiece);  // Replace the captured piece
				} else if (endSq.equals(enPassantTarget)) {
					capturedPiece = getPiece(endSq.x, startSq.y);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					empty(endSq.x, startSq.y);  // Capturing the en-passanted piece
					if (!inCheck(true)) {  // The move is legal, add it to the list
						allMoves[index++] = new Move(PAWN, startSq, endSq, true, true, false, PAWN);
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq.x, startSq.y, capturedPiece);  // Replace the captured piece
					empty(endSq);  // Remove its potential 'future self'
				}
			}
		} else {  // Black pawns move 'down' the board (y decreases)
			endSq.copyOf(startSq);
			endSq.add(0, -1);
			if (endSq.onBoard() && !occupied(endSq)) {
				originalPawn = getPiece(startSq);
				setPiece(endSq, originalPawn);  // Move the pawn
				empty(startSq);  // Start square is now empty, the pawn has moved
				if (!inCheck(false)) {  // The move is legal, add it to the list
					if (endSq.y == 0) {  // Special case for promotion
						// Sprite IDs don't matter for these (they are temporary), so they are all 0
						setPiece(endSq, QUEEN, 0, false);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, QUEEN);
						setPiece(endSq, ROOK, 0, false);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, ROOK);
						setPiece(endSq, KNIGHT, 0, false);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, KNIGHT);
						setPiece(endSq, BISHOP, 0, false);
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, BISHOP);
					} else {
						allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, PAWN);
					}
					if (inCheck) tryDouble = false;  // The move blocked a check, so a double-push will be illegal
				} else {
					if (!inCheck) tryDouble = false;  // The pawn is pinned, so a double-push will be illegal
				}
				setPiece(startSq, originalPawn);  // Move the pawn back
				empty(endSq);  // Remove its potential 'future self'
				if (tryDouble && startSq.y == 6) {  // Double-push move
					endSq.add(0, -1);
					if (!occupied(endSq)) {
						setPiece(endSq, originalPawn);  // Move the pawn
						empty(startSq);
						if (!inCheck(false)) {  // The move is legal, add it to the list
							allMoves[index++] = new Move(PAWN, startSq, endSq, false, false, false, PAWN);
						}
						setPiece(startSq, originalPawn);  // Move the pawn back
						empty(endSq);  // Remove its potential 'future self'
					}
				}
			}
			endSq.copyOf(startSq);
			endSq.add(-1, -1);  // 'Right' capture
			if (endSq.onBoard()) {
				if (occupied(endSq) && (isWhite(endSq) == true)) {  // Black pawns capture white pieces
					capturedPiece = getPiece(endSq);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					if (!inCheck(false)) {  // The move is legal, add it to the list
						if (endSq.y == 0) {  // Special case for promotion
							// Sprite IDs don't matter for these (they are temporary), so they are all 0
							setPiece(endSq, QUEEN, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, QUEEN);
							setPiece(endSq, ROOK, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, ROOK);
							setPiece(endSq, KNIGHT, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, KNIGHT);
							setPiece(endSq, BISHOP, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, BISHOP);
						} else {
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, PAWN);
						}
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq, capturedPiece);  // Replace the captured piece
				} else if (endSq.equals(enPassantTarget)) {
					capturedPiece = getPiece(endSq.x, startSq.y);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					empty(endSq.x, startSq.y);  // Capturing the en-passanted piece
					if (!inCheck(false)) {  // The move is legal, add it to the list
						allMoves[index++] = new Move(PAWN, startSq, endSq, true, true, false, PAWN);
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq.x, startSq.y, capturedPiece);  // Replace the captured piece
					empty(endSq);  // Remove its potential 'future self'
				}
			}
			endSq.add(2, 0);  // 'Left' capture
			if (endSq.onBoard()) {
				if (occupied(endSq) && (isWhite(endSq) == true)) {  // Black pawns capture white pieces
					capturedPiece = getPiece(endSq);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					if (!inCheck(false)) {  // The move is legal, add it to the list
						if (endSq.y == 0) {  // Special case for promotion
							// Sprite IDs don't matter for these (they are temporary), so they are all 0
							setPiece(endSq, QUEEN, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, QUEEN);
							setPiece(endSq, ROOK, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, ROOK);
							setPiece(endSq, KNIGHT, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, KNIGHT);
							setPiece(endSq, BISHOP, 0, false);
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, BISHOP);
						} else {
							allMoves[index++] = new Move(PAWN, startSq, endSq, true, false, false, PAWN);
						}
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq, capturedPiece);  // Replace the captured piece
				} else if (endSq.equals(enPassantTarget)) {
					capturedPiece = getPiece(endSq.x, startSq.y);
					originalPawn = getPiece(startSq);
					setPiece(endSq, originalPawn);  // Move the pawn
					empty(startSq);
					empty(endSq.x, startSq.y);  // Capturing the en-passanted piece
					if (!inCheck(false)) {  // The move is legal, add it to the list
						allMoves[index++] = new Move(PAWN, startSq, endSq, true, true, false, PAWN);
					}
					setPiece(startSq, originalPawn);  // Move the pawn back
					setPiece(endSq.x, startSq.y, capturedPiece);  // Replace the captured piece
					empty(endSq);  // Remove its potential 'future self'
				}
			}
		}
		return allMoves;
	}
	
	Move[] knightMoves(Vector startSq) {
		Move[] allMoves = new Move[9];  // 8 is the maximum number of possible moves for a knight
		if (isWhite(startSq) != whiteToMove) return allMoves;  // No legal moves if it's not your turn
		int index = 0;  // Current empty slot in allMoves to add a move
		boolean inCheck = inCheck(whiteToMove);
		Vector endSq = new Vector();
		boolean isCapture;
		int movingKnight = getPiece(startSq);
		int capturedPiece;
		boolean pinned = false;  // For optimisation
		
		for (Vector dir : knightVecs) {
			endSq.copyOf(startSq);
			endSq.add(dir);
			if (endSq.onBoard()) {
				if (occupied(endSq)) {
					if (isWhite(endSq) == whiteToMove) continue;  // End square occupied by same colour piece, move is impossible
					isCapture = true;
				} else {
					isCapture = false;
				}
				capturedPiece = getPiece(endSq);  // Save what was on the end square
				setPiece(endSq, movingKnight);  // Move the knight
				empty(startSq);  // Start square is now empty, the knight has moved
				
				if (inCheck(whiteToMove)) {  // The turning player is is check after moving, move is illegal
					if (!inCheck) pinned = true;  // If a knight move puts yourself in check, all moves with that knight will
				} else {  // The move is legal, add it to the list
					allMoves[index++] = new Move(KNIGHT, startSq, endSq, isCapture, false, false, KNIGHT);
				}
				setPiece(startSq, movingKnight);  // Move the knight back
				setPiece(endSq, capturedPiece);  // Replace the captured piece (may be empty)
				if (pinned) break;  // Optimisation - no more knight moves are looked at, all will be illegal
			}
		}
		return allMoves;
	}
	
	int rayMoves(Vector startSq, int type, Vector dir, Move[] allMoves, int index) {
		if (isWhite(startSq) != whiteToMove) return index;  // No legal moves if it's not your turn
		boolean inCheck = inCheck(whiteToMove);
		Vector endSq = new Vector();
		boolean isCapture;
		int movingPiece = getPiece(startSq);
		int capturedPiece = 0;
		boolean maybeIllegal = true;  // For optimisation
		boolean fullExit = false;  // For optimisation
		boolean halfExit;
		
		for (int pass = 0; pass < 2; pass++) {
			halfExit = false;
			endSq.copyOf(startSq);
			endSq.add(dir);  // Adding tests one 'half' of the ray
			while (endSq.onBoard()) {
				if (!occupied(endSq)) {  // Square is empty
					isCapture = false;
					capturedPiece = 0;
					setPiece(endSq, movingPiece);  // Move the piece
					empty(startSq);  // Start square is now empty, the piece has moved
				} else {
					if (isWhite(endSq) == whiteToMove) {  // Ray blocked by same colour piece
						break;
					} else {  // Capture move
						isCapture = true;
						capturedPiece = getPiece(endSq);
						setPiece(endSq, movingPiece);  // Move the piece
						empty(startSq);  // Start square is now empty, the piece has moved
						halfExit = true;  // Cannot move further along this 'half' of the ray
					}
				}
				if (maybeIllegal) {
					if (!inCheck(whiteToMove)) {
						if (inCheck) {  // Stopped a check, this must be the only legal move on the ray
							allMoves[index++] = new Move(type, startSq, endSq, isCapture, false, false, type);
							fullExit = true;
						} else {
							maybeIllegal = false;  // The piece is not pinned, so there's no point checking for self-checks in future
							allMoves[index++] = new Move(type, startSq, endSq, isCapture, false, false, type);
						}
					} else {
						if (!inCheck) fullExit = true; // Moved into check, all moves on the ray are illegal
					}
				} else {
					allMoves[index++] = new Move(type, startSq, endSq, isCapture, false, false, type);
				}
				setPiece(startSq, movingPiece);  // Move the piece back
				setPiece(endSq, capturedPiece);  // Replace the captured piece (may be empty)
				
				if (halfExit) break;
				if (fullExit) return index;
				
				endSq.add(dir);  // Look at the next square along the ray
			}
			dir.flip();  // Negates direction of movement along the ray
		}
		return index;
	}
	
	Move[] kingMoves(Vector startSq) {
		Move[] allMoves = new Move[9];  // 8 is the maximum number of possible moves for a king
		if (isWhite(startSq) != whiteToMove) return allMoves;  // No legal moves if it's not your turn
		int index = 0;  // Current empty slot in allMoves to add a move
		Vector endSq = new Vector();
		boolean isCapture;
		int king = getPiece(startSq);
		int capturedPiece;
		Vector kingPos;
		Vector oppKingPos;
		if (whiteToMove) {
			kingPos = wKingPos;  // Important that this is copying by reference
			oppKingPos = bKingPos;
		}
		else {
			kingPos = bKingPos;  // Important that this is copying by reference
			oppKingPos = wKingPos;
		}
		
		for (Vector dir : slidingVecs) {
			endSq.copyOf(startSq);
			endSq.add(dir);
			// Cannot move to be 'touching' the opposing king
			if (Math.abs(endSq.x - oppKingPos.x) < 2 && Math.abs(endSq.y - oppKingPos.y) < 2) continue;
			if (endSq.onBoard()) {
				if (occupied(endSq)) {
					if (isWhite(endSq) == whiteToMove) continue;  // End square occupied by same colour piece, move is impossible
					isCapture = true;
				} else {
					isCapture = false;
				}
				capturedPiece = getPiece(endSq);  // Save what was on the end square
				setPiece(endSq, king);  // Move the king
				empty(startSq);  // Start square is now empty, the king has moved
				kingPos.copyOf(endSq);  // Needs to be updated for inCheck to work
				
				if (!inCheck(whiteToMove)) {
					allMoves[index++] = new Move(KING, startSq, endSq, isCapture, false, false, KING);
				}
				setPiece(startSq, king);  // Move the king back
				setPiece(endSq, capturedPiece);  // Replace the captured piece (may be empty)
				kingPos.copyOf(startSq);
			}
		}
		if (whiteToMove) {
			// Queenside castling
			if (wQueenCastle && !occupied(1, 0) && !occupied(2, 0) && !occupied(3, 0)) {
				boolean canCastle = true;
				endSq.copyOf(startSq);
				for (int step = 0; step < 3; step++) {  // Move the king along step-by-step, checking for checks
					if (inCheck(whiteToMove)) {
						canCastle = false;
						break;
					}
					if (Math.abs(endSq.x - oppKingPos.x) < 2 && Math.abs(endSq.y - oppKingPos.y) < 2) {
						canCastle = false;
						break;
					}
					
					empty(endSq);
					if (step < 2) endSq.add(-1, 0);
					setPiece(endSq, king);
					kingPos.copyOf(endSq);
				}
				if (canCastle) allMoves[index++] = new Move(KING, startSq, endSq, false, false, true, KING);
				empty(endSq);  // Remove the king's potential 'future self'
				setPiece(startSq, king);  // Move the king back
				kingPos.copyOf(startSq);
			}
			// Kingside castling
			if (wKingCastle && !occupied(6, 0) && !occupied(5, 0)) {
				boolean canCastle = true;
				endSq.copyOf(startSq);
				for (int step = 0; step < 3; step++) {  // Move the king along step-by-step, checking for checks
					if (inCheck(whiteToMove)) {
						canCastle = false;
						break;
					}
					if (Math.abs(endSq.x - oppKingPos.x) < 2 && Math.abs(endSq.y - oppKingPos.y) < 2) {
						canCastle = false;
						break;
					}
					
					empty(endSq);
					if (step < 2) endSq.add(1, 0);
					setPiece(endSq, king);
					kingPos.copyOf(endSq);
				}
				if (canCastle) allMoves[index++] = new Move(KING, startSq, endSq, false, false, true, KING);
				empty(endSq);  // Remove the king's potential 'future self'
				setPiece(startSq, king);  // Move the king back
				kingPos.copyOf(startSq);
			}
		} else {
			// Queenside castling
			if (bQueenCastle && !occupied(1, 7) && !occupied(2, 7) && !occupied(3, 7)) {
				boolean canCastle = true;
				endSq.copyOf(startSq);
				for (int step = 0; step < 3; step++) {  // Move the king along step-by-step, checking for checks
					if (inCheck(whiteToMove)) {
						canCastle = false;
						break;
					}
					if (Math.abs(endSq.x - oppKingPos.x) < 2 && Math.abs(endSq.y - oppKingPos.y) < 2) {
						canCastle = false;
						break;
					}
					
					empty(endSq);
					if (step < 2) endSq.add(-1, 0);
					setPiece(endSq, king);
					kingPos.copyOf(endSq);
				}
				if (canCastle) allMoves[index++] = new Move(KING, startSq, endSq, false, false, true, KING);
				empty(endSq);  // Remove the king's potential 'future self'
				setPiece(startSq, king);  // Move the king back
				kingPos.copyOf(startSq);
			}
			// Kingside castling
			if (bKingCastle && !occupied(6, 7) && !occupied(5, 7)) {
				boolean canCastle = true;
				endSq.copyOf(startSq);
				for (int step = 0; step < 3; step++) {  // Move the king along step-by-step, checking for checks
					if (inCheck(whiteToMove)) {
						canCastle = false;
						break;
					}
					if (Math.abs(endSq.x - oppKingPos.x) < 2 && Math.abs(endSq.y - oppKingPos.y) < 2) {
						canCastle = false;
						break;
					}
					
					empty(endSq);
					if (step < 2) endSq.add(1, 0);
					setPiece(endSq, king);
					kingPos.copyOf(endSq);
				}
				if (canCastle) allMoves[index++] = new Move(KING, startSq, endSq, false, false, true, KING);
				empty(endSq);  // Remove the king's potential 'future self'
				setPiece(startSq, king);  // Move the king back
				kingPos.copyOf(startSq);
			}
		}
		return allMoves;
	}
	
	Move[] genMoves(int column, int row) {
		return genMoves(new Vector(column, row));
	}
	Move[] genMoves(Vector startSq) {
		switch (type(startSq)) {
		case PAWN:
			return pawnMoves(startSq);
		case KNIGHT:
			return knightMoves(startSq);
		case BISHOP:
			Move[] bishopMoves = new Move[14];  // 13 is the maximum number of possible moves for a bishop
			int indexB = 0;
			indexB = rayMoves(startSq, BISHOP, new Vector(1, 1), bishopMoves, indexB);
			rayMoves(startSq, BISHOP, new Vector(1, -1), bishopMoves, indexB);
			return bishopMoves;
		case ROOK:
			Move[] rookMoves = new Move[15];  // 14 is the maximum number of possible moves for a rook
			int indexR = 0;
			indexR = rayMoves(startSq, ROOK, new Vector(0, 1), rookMoves, indexR);
			rayMoves(startSq, ROOK, new Vector(1, 0), rookMoves, indexR);
			return rookMoves;
		case QUEEN:
			Move[] queenMoves = new Move[28];  // 27 is the maximum number of possible moves for a queen
			int indexQ = 0;
			indexQ = rayMoves(startSq, QUEEN, new Vector(0, 1), queenMoves, indexQ);
			indexQ = rayMoves(startSq, QUEEN, new Vector(1, 1), queenMoves, indexQ);
			indexQ = rayMoves(startSq, QUEEN, new Vector(1, 0), queenMoves, indexQ);
			rayMoves(startSq, QUEEN, new Vector(1, -1), queenMoves, indexQ);
			return queenMoves;
		case KING:
			return kingMoves(startSq);
		default:
			return new Move[1];
		}
	}
	
	Move[] possibleMovesUnordered() {  // Basic, unordered list of all possible moves
		Move[] allMoves = new Move[219];  // 218 is the maximum number of moves, +1 so there is always a null Move at the end
		int index = 0;
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Move[] pieceMoves = genMoves(x, y);
				for (Move move : pieceMoves) {
					if (move != null) {
						allMoves[index] = move;
						index++;
					} else break;
				}
			}
		}
		return allMoves;
	}
	
	Move[] possibleMoves() {  // Uses MVV-LVA
		int[][] currentIndex = new int[6][6];  // 6 is the number of possible victim types, 6 is the number of possible aggressors
		Move[][][] allMoves = new Move[6][6][73];  // 72 is at least the maximum number of possible moves with the same attacker and victim type, i.e. 9 queens each with 8 possible captures of the same piece type (9 * 8 = 72)
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Move[] pieceMoves = genMoves(x, y);
				for (Move move : pieceMoves) {
					if (move != null) {
						int victimIndex = 0;
						if (move.isCapture) {
							victimIndex = type(move.endSq);
						}
						int attackerIndex = move.finalType - 1;
						allMoves[victimIndex][attackerIndex][currentIndex[victimIndex][attackerIndex]++] = move;
					} else break;
				}
			}
		}
		
		int index = 0;
		Move[] sortedMoves = new Move[219];
		
		for (int victim = 5; victim >= 0; victim--) {
			for (int attacker = 0; attacker < 6; attacker++) {
				for (Move move : allMoves[victim][attacker]) {
					if (move != null) {
						sortedMoves[index++] = move;
					} else break;
				}
			}
		}
		
		return sortedMoves;
	}
	
	void makeMove(Move move) {
		halfMoves++;
		fiftyMoveCount++;
		enPassantTarget.setXY(-1, -1);
		int startX = move.startSq.x;
		int startY = move.startSq.y;
		int endX = move.endSq.x;
		int endY = move.endSq.y;
		int movingPiece = getPiece(startX, startY);
		if (move.isCapture) {
			fiftyMoveCount = 0;
			if (move.isEnPassant) {
				empty(endX, startY);  // Have to manually remove piece since it won't be overwritten by the capturing piece
			}
			if (whiteToMove) {
				if (move.endSq.equals(0, 7)) bQueenCastle = false;
				else if (move.endSq.equals(7, 7)) bKingCastle = false;
			} else {
				if (move.endSq.equals(0, 0)) wQueenCastle = false;
				else if (move.endSq.equals(7, 0)) wKingCastle = false;
			}
		}
		if (move.pieceType == PAWN) {
			fiftyMoveCount = 0;
			if (Math.abs(endY - startY) == 2) {  // Pawn moved two squares from starting position = potential en passant target
				if (endX > 0) {
					if ((type(endX - 1, endY) == PAWN) && (isWhite(endX - 1, endY) != whiteToMove)) {
						enPassantTarget.setXY(endX, (startY + endY) / 2);
					}
				}
				if (endX < 7) {
					if ((type(endX + 1, endY) == PAWN) && (isWhite(endX + 1, endY) != whiteToMove)) {
						enPassantTarget.setXY(endX, (startY + endY) / 2);
					}
				}
			}
		} else if (move.pieceType == ROOK) {
			if (whiteToMove) {
				if (move.startSq.equals(0, 0)) wQueenCastle = false;
				else if (move.startSq.equals(7, 0)) wKingCastle = false;
			} else {
				if (move.startSq.equals(0, 7)) bQueenCastle = false;
				else if (move.startSq.equals(7, 7)) bKingCastle = false;
			}
		} else if (move.pieceType == KING) {
			if (whiteToMove) {
				wKingPos = move.endSq;  // Copying by reference OK since a Move never changes
				wKingCastle = false;
				wQueenCastle = false;
			} else {
				bKingPos = move.endSq;  // Copying by reference OK since a Move never changes
				bKingCastle = false;
				bQueenCastle = false;
			}
			if (move.isCastling) {  // Castling move
				if (endX == 6) {  // Kingside castling
					board[endY * 8 + 5] = board[endY * 8 + 7];  // Move the rook
					empty(7, endY);
				} else {  // Queenside castling
					board[endY * 8 + 3] = board[endY * 8 + 0];  // Move the rook
					empty(0, endY);
				}
			}
		}
		setPiece(move.endSq, move.finalType, getSprite(movingPiece), whiteToMove);  // Move the piece
		empty(move.startSq);  // Remove the old instance of the piece from the starting square
		whiteToMove = !whiteToMove;
	}
	
	boolean mate() {
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Move[] pieceMoves = genMoves(x, y);
				for (Move move : pieceMoves) {
					if (move != null) {
						return false;
					} else break;
				}
			}
		}
		return true;
	}
	
	boolean repetition(GameState[] history) {
		int seenBefore = 0;
		
		int firstState = 0;
		while (history[firstState] == null) firstState++;
		
		for (int i = firstState; i < halfMoves; i++) {  // Loop through previous board positions
			if (history[i].whiteToMove != whiteToMove) continue;  // Different
			if (!history[i].enPassantTarget.equals(enPassantTarget)) continue;  // Different
			if (history[i].wKingCastle != wKingCastle || history[i].wQueenCastle != wQueenCastle || history[i].bKingCastle != bKingCastle || history[i].bQueenCastle != bQueenCastle) continue;  // Different
			
			for (int s = 0; s < 64; s++) {  // Compare each square
				if (type(history[i].board[s]) != type(board[s]) || isWhite(history[i].board[s]) != isWhite(board[s])) break;  // Different
				if (true);
				if (s == 63) {
					seenBefore += 1;  // Every square the same, identical board state has been seen before
					if (seenBefore == 2) return true;
				}
			}
		}
		return false;
	}
	
	boolean fiftyMove() {
		if (fiftyMoveCount >= 100) return true;
		return false;
	}
	
	boolean insuf() {
		int minorPieces = 0;
		int darkSqBishops = 0;
		int lightSqBishops = 0;
		int type;
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				type = type(x, y);
				if (type == PAWN || type == ROOK || type == QUEEN) return false;  // Sufficient checkmating material present
				else if (type == KNIGHT) {
					minorPieces++;
					if (minorPieces > 1) return false;  // Sufficient checkmating material present
				}
				else if (type == BISHOP) {
					minorPieces++;
					if (x % 2 == y % 2) darkSqBishops++;
					else lightSqBishops++;
				}
			}
		}
		if (minorPieces <= 1 || (minorPieces == lightSqBishops || minorPieces == darkSqBishops)) return true;  // Insufficient checkmating material present
		return false;  // Sufficient checkmating material present
	}
	
	boolean insufTimeout() {
		int minorPieces = 0;
		int type;
		
		for (int piece : board) {
			type = type(piece);
			if (type != NONE && isWhite(piece) != whiteToMove) {
				if (type == PAWN || type == ROOK || type == QUEEN) return false;  // Sufficient checkmating material present
				else if (type == KNIGHT || type == BISHOP) if (++minorPieces > 1) return false;  // Sufficient checkmating material present
			}
		}
		return true;  // Insufficient checkmating material present
	}
	
	int updateState(GameState[] history) {
		int state = NORMAL;
		
		if (inCheck(whiteToMove)) state = CHECK;
		if (mate()) {
			if (state == CHECK) state = CHECKMATE;
			else state = STALEMATE;
		}
		else if (repetition(history)) state = REPETITION;
		else if (fiftyMove()) state = FIFTYMOVE;
		else if (insuf()) state = INSUF;
		
		return state;
	}
}