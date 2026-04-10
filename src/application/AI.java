package application;

import static application.Constants.*;

class AI {
	int depth;
	boolean playingAsWhite;
	Move bestMove;
	GameState[] history;
	int timeLimit;
	long startTime;
	boolean cancel;
	
	// Piece-square tables, from white's POV
	// Arrays below appear as boards sideways, with white playing from the left
	// This means values can be accessed by table[x][y]
	
	static final int[][] pawnTable = {
		{  0,  5,  5,  0,  5, 10, 50,  0},
		{  0, 10, -5,  0,  5, 10, 50,  0},
		{  0, 10,-10,  0, 10, 20, 50,  0},
		{  0,-20,  0, 20, 25, 30, 50,  0},
		{  0,-20,  0, 20, 25, 30, 50,  0},
		{  0, 10,-10,  0, 10, 20, 50,  0},
		{  0, 10, -5,  0,  5, 10, 50,  0},
		{  0,  5,  5,  0,  5, 10, 50,  0}
	};
	
	static final int[][] knightTable = {
		{-50,-40,-30,-30,-30,-30,-40,-50},
		{-40,-20,  5,  0,  5,  0,-20,-40},
		{-30,  0, 10, 15, 15, 10,  0,-30},
		{-30,  5, 15, 20, 20, 15,  0,-30},
		{-30,  5, 15, 20, 20, 15,  0,-30},
		{-30,  0, 10, 15, 15, 10,  0,-30},
		{-40,-20,  5,  0,  5,  0,-20,-40},
		{-50,-40,-30,-30,-30,-30,-40,-50}
	};
	
	static final int[][] bishopTable = {
		{-20,-10,-10,-10,-10,-10,-10,-20},
		{-10,  5, 10,  0,  5,  0,  0,-10},
		{-10,  0, 10, 10,  5,  5,  0,-10},
		{-10,  0, 10, 10, 10, 10,  0,-10},
		{-10,  0, 10, 10, 10, 10,  0,-10},
		{-10,  0, 10, 10,  5,  5,  0,-10},
		{-10,  5, 10,  0,  5,  0,  0,-10},
		{-20,-10,-10,-10,-10,-10,-10,-20}
	};
	
	static final int[][] rookTable = {
		{  0, -5, -5, -5, -5, -5,  5,  0},
		{  0,  0,  0,  0,  0,  0, 10,  0},
		{  0,  0,  0,  0,  0,  0, 10,  0},
		{  5,  0,  0,  0,  0,  0, 10,  0},
		{  5,  0,  0,  0,  0,  0, 10,  0},
		{  0,  0,  0,  0,  0,  0, 10,  0},
		{  0,  0,  0,  0,  0,  0, 10,  0},
		{  0, -5, -5, -5, -5, -5,  5,  0}
	};
	
	static final int[][] queenTable = {
		{-20,-10,-10,  0, -5,-10,-10,-20},
		{-10,  0,  5,  0,  0,  0,  0,-10},
		{-10,  5,  5,  5,  5,  5,  0,-10},
		{ -5,  0,  5,  5,  5,  5,  0, -5},
		{ -5,  0,  5,  5,  5,  5,  0, -5},
		{-10,  5,  5,  5,  5,  5,  0,-10},
		{-10,  0,  5,  0,  0,  0,  0,-10},
		{-20,-10,-10,  0, -5,-10,-10,-20}
	};
	
	static final int[][] kingTableMid = {
		{ 20, 20,-10,-20,-30,-30,-30,-30},
		{ 30, 20,-20,-30,-40,-40,-40,-40},
		{ 10,  0,-20,-30,-40,-40,-40,-40},
		{  0,  0,-20,-40,-50,-50,-50,-50},
		{  0,  0,-20,-40,-50,-50,-50,-50},
		{ 10,  0,-20,-30,-40,-40,-40,-40},
		{ 30, 20,-20,-30,-40,-40,-40,-40},
		{ 20, 20,-10,-20,-30,-30,-30,-30}
	};
	
	static final int[][] kingTableEnd = {
		{-50,-30,-30,-30,-30,-30,-30,-50},
		{-30,-30,-10,-10,-10,-10,-20,-40},
		{-30,  0, 20, 30, 30, 20,-10,-30},
		{-30,  0, 30, 40, 40, 30,  0,-20},
		{-30,  0, 30, 40, 40, 30,  0,-20},
		{-30,  0, 20, 30, 30, 20,-10,-30},
		{-30,-30,-10,-10,-10,-10,-20,-40},
		{-50,-30,-30,-30,-30,-30,-30,-50}
	};
	
	AI(int halfMoveDepth, boolean isWhitePlayer, GameState[] gameHistory) {
		depth = halfMoveDepth;
		playingAsWhite = isWhitePlayer;
		history = gameHistory;
		cancel = false;
		timeLimit = 15000;
	}
	
	boolean isEndgame(GameState state) {
		int sign = 0;  // White has a sign of 1, black -1 (positive evaluation is advantage to white and vice versa)
		int whiteQueens = 0;
		int whiteMinors = 0;
		boolean whiteOthers = false;
		int blackQueens = 0;
		int blackMinors = 0;
		boolean blackOthers = false;
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				if (state.type(x, y) != NONE) {
					if (state.isWhite(x, y)) sign = 1;
					else sign = -1;
					
					switch (state.type(x, y)) {
					case PAWN:
						if (sign == 1) {
							whiteOthers = true;
						} else {
							blackOthers = true;
						}
						break;
					case KNIGHT:
						if (sign == 1) {
							whiteMinors++;
						} else {
							blackMinors++;
						}
						break;
					case BISHOP:
						if (sign == 1) {
							whiteMinors++;
						} else {
							blackMinors++;
						}
						break;
					case ROOK:
						if (sign == 1) {
							whiteOthers = true;
						} else {
							blackOthers = true;
						}
						break;
					case QUEEN:
						if (sign == 1) {
							whiteQueens++;
						} else {
							blackQueens++;
						}
						break;
					}
				}
			}
		}
		
		return (whiteQueens == 0 || whiteQueens == 1 && !whiteOthers && whiteMinors <= 1) && (blackQueens == 0 || blackQueens == 1 && !blackOthers && blackMinors <= 1);
	}
	
	float evaluate(GameState state) {
		float value = 0;
		int sign = 0;  // White has a sign of 1, black -1 (positive evaluation is advantage to white and vice versa)
		int[][] filePawns = new int[8][3];  // First row for black pawns, third for white, empty middle row
		// The following are for deciding whether it's the endgame
		int whiteQueens = 0;
		int whiteMinors = 0;
		boolean whiteOthers = false;
		int blackQueens = 0;
		int blackMinors = 0;
		boolean blackOthers = false;
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				if (state.type(x, y) != NONE) {
					if (state.isWhite(x, y)) sign = 1;
					else sign = -1;
					
					switch (state.type(x, y)) {
					case PAWN:
						filePawns[x][sign + 1]++;  // Add to the total number of pawns on this file
						if (state.getPiece(x, y + sign) != NONE) value -= sign * 50;  // The pawn is blocked
						value += sign * 100;
						if (sign == 1) {
							value += pawnTable[x][y];
							whiteOthers = true;
						} else {
							value -= pawnTable[x][7 - y];
							blackOthers = true;
						}
						break;
					case KNIGHT:
						value += sign * 320;
						if (sign == 1) {
							value += knightTable[x][y];
							whiteMinors++;
						} else {
							value -= knightTable[x][7 - y];
							blackMinors++;
						}
						break;
					case BISHOP:
						value += sign * 330;
						if (sign == 1) {
							value += bishopTable[x][y];
							whiteMinors++;
						} else {
							value -= bishopTable[x][7 - y];
							blackMinors++;
						}
						break;
					case ROOK:
						value += sign * 500;
						if (sign == 1) {
							value += rookTable[x][y];
							whiteOthers = true;
						} else {
							value -= rookTable[x][7 - y];
							blackOthers = true;
						}
						break;
					case QUEEN:
						value += sign * 900;
						if (sign == 1) {
							value += queenTable[x][y];
							whiteQueens++;
						} else {
							value -= queenTable[x][7 - y];
							blackQueens++;
						}
						break;
					}
				}
			}
		}
		
		for (int x = 0; x < 8; x++) {  // Evaluating doubled pawns
			value += Math.max(filePawns[x][0] - 1, 0) * 50;  // Max int ensures having no pawns on a file isn't rewarded
			value -= Math.max(filePawns[x][2] - 1, 0) * 50;
		}
		
		for (int sign2 = -1; sign2 <= 1; sign2 += 2) {  // Evaluating isolated pawns
			if (filePawns[0][sign2 + 1] > 0) {  // a-file pawns
				if (filePawns[1][sign2 + 1] == 0) value -= sign2 * 50;
			}
			if (filePawns[7][sign2 + 1] > 0) {  // h-file pawns
				if (filePawns[6][sign2 + 1] == 0) value -= sign2 * 50;
			}
			for (int x = 1; x < 7; x++) {
				if (filePawns[x][sign2 + 1] > 0) {  // all other files (check both sides for adjacent pawns)
					if (filePawns[x - 1][sign2 + 1] == 0 && filePawns[x + 1][sign2 + 1] == 0) value -= sign2 * 50;
				}
			}
		}
		
		if ((whiteQueens == 0 || whiteQueens == 1 && !whiteOthers && whiteMinors <= 1) && (blackQueens == 0 || blackQueens == 1 && !blackOthers && blackMinors <= 1)) {  // Endgame
			value += kingTableEnd[state.wKingPos.x][state.wKingPos.y];
			value -= kingTableEnd[state.bKingPos.x][7 - state.bKingPos.y];
		} else {  // Midgame
			value += kingTableMid[state.wKingPos.x][state.wKingPos.y];
			value -= kingTableMid[state.bKingPos.x][7 - state.bKingPos.y];
		}
		
		return value;
	}
	
	float alphaBetaMax(int index, float alpha, float beta, int depthToGo) {
		if (depthToGo == 0) return evaluate(history[index]);
		else if (depthToGo == depth) startTime = System.currentTimeMillis();
		
		if (cancel) return 0;
		
		for (Move move : history[index].possibleMoves()) {
			if (move != null) {
				float value = 0;
				
				history[index + 1] = new GameState();
				history[index + 1].copyOf(history[index]);
				history[index + 1].makeMove(move);
				
				boolean mate = history[index + 1].mate();
				
				if (history[index + 1].inCheck(false) && mate)  // Seeing if black is in check
					value = 100000 * depthToGo;  // 100000 ensures a win is better than any material difference, * depthToGo prioritises faster mates
				else if (mate) value = 0;  // Draw (stalemate)
				else if (history[index + 1].repetition(history)) value = 0;  // Draw (repetition)
				else if (history[index + 1].fiftyMove()) value = 0;  // Draw (fifty-move rule)
				else if (history[index + 1].insuf()) value = 0;  // Draw (insufficient material)
				else value = alphaBetaMin(index + 1, alpha, beta, depthToGo - 1);
				
				if (depthToGo == depth) {
					if (System.currentTimeMillis() - startTime >= timeLimit) {  // Time limit of 15 seconds
						int properDepth = depth;
						depth = 4;
						alphaBetaMax(index, alpha, beta, depth);
						depth = properDepth;
						System.out.println("TIME LIMITED");
						return 0;
					}
				}
				
				if (value >= beta) return beta;  // Pruning
				else if (value > alpha) {
					alpha = value;  // The bound has changed
					if (depthToGo == depth) bestMove = move;
				}
			} else break;
		}
		
		return alpha;
	}
	
	float alphaBetaMin(int index, float alpha, float beta, int depthToGo) {
		if (depthToGo == 0) return evaluate(history[index]);
		else if (depthToGo == depth) startTime = System.currentTimeMillis();
		
		if (cancel) return 0;
		
		for (Move move : history[index].possibleMoves()) {
			if (move != null) {
				float value = 0;
				
				history[index + 1] = new GameState();
				history[index + 1].copyOf(history[index]);
				history[index + 1].makeMove(move);
				
				boolean mate = history[index + 1].mate();
				
				if (history[index + 1].inCheck(true) && mate)  // Seeing if white is in check
					value = -100000 * depthToGo;  // -100000 ensures a win is better than any material difference, * depthToGo prioritises faster mates
				else if (mate) value = 0;  // Draw (stalemate)
				else if (history[index + 1].repetition(history)) value = 0;  // Draw (repetition)
				else if (history[index + 1].fiftyMove()) value = 0;  // Draw (fifty-move rule)
				else if (history[index + 1].insuf()) value = 0;  // Draw (insufficient material)
				else value = alphaBetaMax(index + 1, alpha, beta, depthToGo - 1);
				
				if (depthToGo == depth) {
					if (System.currentTimeMillis() - startTime >= timeLimit) {  // Time limit of 15 seconds
						int properDepth = depth;
						depth = 4;
						alphaBetaMin(index, alpha, beta, depth);
						depth = properDepth;
						System.out.println("TIME LIMITED");
						return 0;
					}
				}
				
				if (value <= alpha) return alpha;  // Pruning
				else if (value < beta) {
					beta = value;  // The bound has changed
					if (depthToGo == depth) bestMove = move;
				}
			}
		}
		
		return beta;
	}
	
	Move getBestMove(int halfMoves) {
		if (playingAsWhite) alphaBetaMax(halfMoves, -100000000, 100000000, depth);
		else alphaBetaMin(halfMoves, -100000000, 100000000, depth);
		
		for (int i = halfMoves + 1; i < history.length; i++) history[i] = null;  // Clearing all the potential future board states from history
		
		return bestMove;
	}
}
