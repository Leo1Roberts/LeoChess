package application;

import static application.Constants.*;
import static application.Settings.*;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

class Utilities {
	static GameState fenToBoard(String fen) {
		try {
			GameState board = new GameState();
			// Format of split FEN: [board state, colour to move, castling rights, en passant target, half-move clock, full move number]
			String[] fenSplit = fen.split(" ");
			if (fenSplit.length != 6) return null;  // Invalid FEN
			
			int c;  // The current character being looked at
			int charIndex = 0;  // The index of this character
			
			// Castling rights
			int length = fenSplit[2].length();
			if (length < 1 || length > 4) return null;  // Invalid FEN
			board.wKingCastle = false;
			board.wQueenCastle = false;
			board.bKingCastle = false;
			board.bQueenCastle = false;
			boolean wKCDone = true;
			boolean wQCDone = true;
			boolean bKCDone = true;
			boolean bQCDone = true;
			while (charIndex < length) {
				switch (fenSplit[2].charAt(charIndex)) {
				case '-':
					if (length != 1) return null;  // Invalid FEN
					break;
				case 'K':
					board.wKingCastle = true;
					wKCDone = false;
					break;
				case 'Q':
					board.wQueenCastle = true;
					wQCDone = false;
					break;
				case 'k':
					board.bKingCastle = true;
					bKCDone = false;
					break;
				case 'q':
					board.bQueenCastle = true;
					bQCDone = false;
					break;
				default:
					return null;  // Invalid FEN
				}
				charIndex++;
			}
			charIndex = 0;
			
			// Board layout
			int square = 0;
			int spriteId = 0;
			int wKings = 0;
			int bKings = 0;
			boolean expectSlash = false;
			boolean expectNotNumber = false;
			while (charIndex < fenSplit[0].length()) {
				c = fenSplit[0].charAt(charIndex);
	
				if (square % 8 == 0 && charIndex != 0 && square != 64) {
					if (expectSlash) expectSlash = false;
					else expectSlash = true;
				}
				
				if (c == '/') {
					if (!expectSlash) return null;  // Invalid FEN
					if (square % 8 != 0) return null;  // Invalid FEN
					else {
						charIndex++;
						expectNotNumber = false;
						continue;
					}
				} else if (expectSlash) return null;  // Invalid FEN
				
				if (Character.isDigit(c)) {
					int emptySquares = Character.getNumericValue(c);
					if (emptySquares > 0 && emptySquares <= 8 && !expectNotNumber) {
						square += emptySquares;
						charIndex++;
						expectNotNumber = true;
						continue;
					}
					return null;  // Invalid FEN
				}
				
				int type;
				boolean isWhite = Character.isUpperCase(c);
				c = Character.toLowerCase(c);
				
				switch (c) {
				case 'p':
					type = PAWN;
					if (square / 8 == 7 || square / 8 == 0) return null;  // Invalid FEN
					break;
				case 'n':
					type = KNIGHT;
					break;
				case 'b':
					type = BISHOP;
					break;
				case 'r':
					type = ROOK;
					if (isWhite) {
						if (board.wKingCastle && square % 8 == 7 && 7 - (square / 8) == 0) {
							wKCDone = true;
						}
						if (board.wQueenCastle && square % 8 == 0 && 7 - (square / 8) == 0) {
							wQCDone = true;
						}
					} else {
						if (board.bKingCastle && square % 8 == 7 && 7 - (square / 8) == 7) {
							bKCDone = true;
						}
						if (board.bQueenCastle && square % 8 == 0 && 7 - (square / 8) == 7) {
							bQCDone = true;
						}
					}
					break;
				case 'q':
					type = QUEEN;
					break;
				case 'k':
					if (isWhite) {
						if (++wKings > 1) return null;  // Invalid FEN
						board.wKingPos.setXY(square % 8, 7 - (square / 8));
						if (board.wKingCastle || board.wQueenCastle) {
							if (!board.wKingPos.equals(4, 0)) return null;  // Invalid FEN
						}
					} else {
						if (++bKings > 1) return null;  // Invalid FEN
						board.bKingPos.setXY(square % 8, 7 - (square / 8));
						if (board.bKingCastle || board.bQueenCastle) {
							if (!board.bKingPos.equals(4, 7)) return null;  // Invalid FEN
						}
					}
					type = KING;
					break;
				default:
					return null;  // Invalid FEN
				}
				board.setPiece(square % 8, 7 - (square / 8), type, spriteId++, isWhite);
				square++;
				charIndex++;
				expectNotNumber = false;
			}
			if (square != 64) return null;  // Invalid FEN
			if (wKings != 1 || bKings != 1) return null;  // Invalid FEN
			if (!(wKCDone && wQCDone && bKCDone && bQCDone)) return null;  // Invalid FEN
			charIndex = 0;
			
			// Colour to move
			if (fenSplit[1].equals("w")) board.whiteToMove = true;
			else if (fenSplit[1].equals("b")) board.whiteToMove = false;
			else return null;  // Invalid FEN
			
			if (board.inCheck(!board.whiteToMove)) return null;  // Invalid FEN
			
			// En passant target
			int targetX = -1;
			int targetY = -1;
			board.enPassantTarget.setXY(-1, -1);
			c = fenSplit[3].charAt(0);
			if (c >= 'a' && c <= 'h' && fenSplit[3].length() == 2) {
				targetX = c - 'a';
				
				c = fenSplit[3].charAt(1);
				if (c >= '1' && c <= '8') targetY = Character.getNumericValue(c) - 1;
				else return null;  // Invalid FEN
				
				// Check if there is an opposite coloured pawn in the correct place to be captured via en passant
				// Then check if the squares behind the opposite coloured pawn are empty
				if (board.whiteToMove) {
					if (targetY != 5 || board.type(targetX, targetY - 1) != PAWN || board.isWhite(targetX, targetY - 1)) return null;  // Invalid FEN
					if (board.type(targetX, targetY) != NONE || board.type(targetX, targetY + 1) != NONE) return null;  // Invalid FEN
				} else {
					if (targetY != 2 || board.type(targetX, targetY + 1) != PAWN || !board.isWhite(targetX, targetY + 1)) return null;  // Invalid FEN
					if (board.type(targetX, targetY) != NONE || board.type(targetX, targetY - 1) != NONE) return null;  // Invalid FEN
				}
				
				if (targetX > 0) {
					if (board.whiteToMove) {
						if (board.type(targetX - 1, targetY - 1) == PAWN && board.isWhite(targetX - 1, targetY - 1)) {
							board.enPassantTarget.setXY(targetX, targetY);
						}
					} else {
						if (board.type(targetX - 1, targetY + 1) == PAWN && !board.isWhite(targetX - 1, targetY + 1)) {
							board.enPassantTarget.setXY(targetX, targetY);
						}
					}
				}
				if (targetX < 7) {
					if (board.whiteToMove) {
						if (board.type(targetX + 1, targetY - 1) == PAWN && board.isWhite(targetX + 1, targetY - 1)) {
							board.enPassantTarget.setXY(targetX, targetY);
						}
					} else {
						if (board.type(targetX + 1, targetY + 1) == PAWN && !board.isWhite(targetX + 1, targetY + 1)) {
							board.enPassantTarget.setXY(targetX, targetY);
						}
					}
				}
			} else if (!(c == '-' && fenSplit[3].length() == 1)) return null;  // Invalid FEN
			
			// Fifty-move rule counter
			try {
				board.fiftyMoveCount = Integer.parseInt(fenSplit[4]);
			} catch (NumberFormatException e) {
				return null;  // Invalid FEN
			}
			if (board.fiftyMoveCount < 0) return null;  // Invalid FEN
			
			// Move number
			int fullMoves;
			try {
				fullMoves = Integer.parseInt(fenSplit[5]);
			} catch (NumberFormatException e) {
				return null;  // Invalid FEN
			}
			if (fullMoves < 1) return null;  // Invalid FEN
			if (board.whiteToMove) board.halfMoves = (fullMoves - 1) * 2;
			else board.halfMoves = (fullMoves - 1) * 2 + 1;
			
			if (board.fiftyMoveCount > board.halfMoves) return null;  // Invalid FEN
			
			return board;
		} catch (Exception e) {
			return null;  // Catchall for invalid FEN
		}
	}
	
	static String boardToFen(GameState board) {
		String fen = "";
		
		for (int y = 7; y >= 0; y--) {
			int emptyCount = 0;
			for (int x = 0; x < 8; x++) {
				if (x == 0 && y != 7) fen += '/';
				
				char piece;
				switch (board.type(x, y)) {
				case NONE:
					emptyCount++;
					if (x == 7 || board.type(x + 1, y) != NONE) fen += emptyCount;
					continue;
				case PAWN:
					piece = 'p';
					break;
				case KNIGHT:
					piece = 'n';
					break;
				case BISHOP:
					piece = 'b';
					break;
				case ROOK:
					piece = 'r';
					break;
				case QUEEN:
					piece = 'q';
					break;
				case KING:
					piece = 'k';
					break;
				default:
					piece = '?';
				}
				if (board.isWhite(x, y)) piece = Character.toUpperCase(piece);
				fen += piece;
				emptyCount = 0;
			}
		}
		
		if (board.whiteToMove) fen += " w ";
		else fen += " b ";

		if (board.wKingCastle || board.wQueenCastle || board.bKingCastle || board.bQueenCastle) {
			if (board.wKingCastle) fen += 'K';
			if (board.wQueenCastle) fen += 'Q';
			if (board.bKingCastle) fen += 'k';
			if (board.bQueenCastle) fen += 'q';
		} else fen += '-';
		fen += ' ';
		
		if (board.enPassantTarget.x == -1) fen += "- ";
		else {
			fen += (char) (board.enPassantTarget.x + 'a');
			fen += (board.enPassantTarget.y + 1) + " ";
		}
		
		fen += board.fiftyMoveCount + " ";
		
		if (board.whiteToMove) fen += board.halfMoves / 2 + 1;
		else fen += (board.halfMoves - 1) / 2 + 1;
		
		return fen;
	}
	
	static void installTooltip(Node node, String text) {
		if (Settings.showTooltips) {
			Tooltip t = new Tooltip(text);
			t.setFont(Font.font("Arial Rounded MT Bold", 12));
			Tooltip.install(node, t);
		}
	}
	
	static void installSpriteTooltip(Sprite sprite) {
		if (!hideBoardTooltips) {
			String text;
			
			if (sprite.captured) {
				if (sprite.isWhite) text = "Captured white ";
				else text = "Captured black ";
			} else {
				if (sprite.isWhite) text = "White ";
				else text = "Black ";
			}
			
			switch (sprite.type) {
			case PAWN:
				text += "pawn";
				break;
			case KNIGHT:
				text += "knight";
				break;
			case BISHOP:
				text += "bishop";
				break;
			case ROOK:
				text += "rook";
				break;
			case QUEEN:
				text += "queen";
				break;
			case KING:
				text += "king";
			}
			
			if (!sprite.captured) {
				text += " on ";
				
				switch (sprite.pos.x) {
				case 0:
					text += 'a';
					break;
				case 1:
					text += 'b';
					break;
				case 2:
					text += 'c';
					break;
				case 3:
					text += 'd';
					break;
				case 4:
					text += 'e';
					break;
				case 5:
					text += 'f';
					break;
				case 6:
					text += 'g';
					break;
				case 7:
					text += 'h';
				}
				
				text += (sprite.pos.y + 1);
			}
			
			installTooltip(sprite, text);
		}
	}
	
	static void installDepthTooltip(Node node, int depth) {
		String text = "Depth " + depth + ": ";
		
		if (depth == 1) text += "banzai";
		else if (depth == 2) text += "beginner";
		else if (depth <= 4) text += "learner";
		else text += "intermediate";
		
		installTooltip(node, text);
	}
	
	static String moveToSAN(GameState[] gameHistory, Move move, int index) {
		String moveSAN = "";
		
		int type = move.pieceType;
		switch (type) {
		case PAWN:
			moveSAN = "";
			break;
		case KNIGHT:
			moveSAN = "N";
			break;
		case BISHOP:
			moveSAN = "B";
			break;
		case ROOK:
			moveSAN = "R";
			break;
		case QUEEN:
			moveSAN = "Q";
			break;
		case KING:
			if (move.isCastling) {
				if (move.endSq.x == 6) moveSAN = "O-O";
				else moveSAN = "O-O-O";
			} else moveSAN = "K";
		}
		
		if (type != PAWN && type != KING) {
			boolean duplicatePiece = false;
			boolean fileUnique = true;
			boolean rankUnique = true;
			for (Move currentMove : gameHistory[index].possibleMovesUnordered()) {
				if (currentMove == null) break;
				if (!currentMove.startSq.equals(move.startSq) && currentMove.endSq.equals(move.endSq) && currentMove.pieceType == type) {
					duplicatePiece = true;  // There is another piece of the same type that can move to the square
					if (currentMove.startSq.x == move.startSq.x) fileUnique = false;
					if (currentMove.startSq.y == move.startSq.y) rankUnique = false;
				}
			}
			if (duplicatePiece) {
				if (fileUnique || !fileUnique && !rankUnique) moveSAN += (char) ('a' + move.startSq.x);
				if (!fileUnique) moveSAN += (move.startSq.y + 1);
			}
		}
		
		if (move.isCapture) {
			if (type == PAWN) moveSAN += (char) ('a' + move.startSq.x);
			moveSAN += "x";
		}
		
		if (!move.isCastling) {
			moveSAN += (char) ('a' + move.endSq.x);
			moveSAN += (move.endSq.y + 1);
		}
		
		if (move.finalType != type) {
			switch (move.finalType) {
			case KNIGHT:
				moveSAN += "=N";
				break;
			case BISHOP:
				moveSAN += "=B";
				break;
			case ROOK:
				moveSAN += "=R";
				break;
			case QUEEN:
				moveSAN += "=Q";
				break;
			}
		}
		
		int state = gameHistory[index + 1].updateState(gameHistory);
		if (state == CHECK) moveSAN += "+";
		else if (state == CHECKMATE) moveSAN += "#";
		
		return moveSAN;
	}
	
	static String[] gameToSAN(GameState[] gameHistory, Move[] moveHistory) {
		int firstIndex = 0;
		for (int i = 0; i < gameHistory.length; i++) {
			if (gameHistory[i] != null) {
				firstIndex = i;
				break;
			}
			if (i == gameHistory.length - 1) return null;  // Whole history is empty
		}
		int lastIndex = 0;
		for (int i = firstIndex + 1; i < gameHistory.length; i++) {
			if (gameHistory[i] == null) {
				lastIndex = i - 1;
				break;
			}
		}
		String[] moves = new String[lastIndex];  // There may be null moves at the start if the game started from a non-standard position
		
		for (int i = firstIndex; i < lastIndex; i++) moves[i] = moveToSAN(gameHistory, moveHistory[i], i);
		
		return moves;
	}
	
	static String colToHex(Color col) {
		return String.format("#%02x%02x%02x", Math.round(col.getRed() * 255), Math.round(col.getGreen() * 255), Math.round(col.getBlue() * 255));
	}
}
