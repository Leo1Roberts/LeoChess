package application;

import static application.Sizes.Game.*;
import static application.Settings.*;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

class Sprite extends ImageView {
	Game game;
	Game.Board board;
	private double mouseX;
	private double mouseY;
	int idNum;
	Vector pos;
	boolean isWhite;
	int type;
	boolean captured;
	Vector capturedPos;

	Sprite(Game parentGame, int idNumber) {  // Constructor
		super();  // Calling the ImageView constructor
		game = parentGame;
		board = game.board;
		idNum = idNumber;
		pos = new Vector();
		captured = false;
		capturedPos = new Vector(-1, -1);
		
		setPickOnBounds(true); // Mouse actions detected on transparent regions
		
		setOnMouseEntered(event -> {
			if (!captured && !game.ended && !board.midPromotion && !board.navigating) {
				if (isWhite == board.gameState.whiteToMove && !board.aiThinking) {
					setCursor(Cursor.OPEN_HAND);  // Open hand is displayed when hovering over a piece of turning player's colour
				} else {
					setCursor(Cursor.DEFAULT);  // Standard pointer is displayed when hovering over a piece of opposite colour
					
					if (showLegalMoves && board.candidateMoves != null) {
						for (Move move : board.candidateMoves) {
							if (move != null) {
								if (move.endSq.equals(pos)) {
									if (pos.x % 2 == pos.y % 2) board.grid[pos.y * 8 + pos.x].setFill(darkSquareCaptureCol);
									else board.grid[pos.y * 8 + pos.x].setFill(lightSquareCaptureCol);
								}
							} else break;
						}
					}
				}
			}
		});
		
		setOnMouseExited(event -> {
			setCursor(Cursor.DEFAULT);
			
			if (!captured && !game.ended && !board.midPromotion && !board.navigating) {
				if (isWhite != board.gameState.whiteToMove) {
					if (board.candidateMoves != null) {
						for (Move move : board.candidateMoves) {
							if (move != null) {
								if (move.endSq.equals(pos)) {
									board.grid[pos.y * 8 + pos.x].resetColour();
								}
							} else break;
						}
					}
				}
			}
		});
		
		setOnMousePressed(event -> {
			if (!captured && !game.ended && !board.navigating && event.isPrimaryButtonDown() && !board.aiThinking) {
				board.spriteClicked(this);  // Handle two-clicks movement
				
				if (!captured && isWhite == board.gameState.whiteToMove) {
					setCursor(Cursor.CLOSED_HAND);  // Mouse hand 'grips' the piece when dragging it
					setViewOrder(-4);  // Draw it on top of all the other sprites
					
					mouseX = event.getSceneX();
					mouseY = event.getSceneY();
					relocate(mouseX - getFitWidth() / 2, mouseY - getFitHeight() / 2);
				}
			}
		});
		
		setOnMouseDragged(event -> {  // Piece moves with the mouse when dragged
			if (!captured && !game.ended && !board.midPromotion && !board.navigating && event.isPrimaryButtonDown() && !board.aiThinking) {
				if (isWhite == board.gameState.whiteToMove) {
					double dX = event.getSceneX() - mouseX;
					double dY = event.getSceneY() - mouseY;
					relocate(getLayoutX() + dX, getLayoutY() + dY);
					mouseX = event.getSceneX();
					mouseY = event.getSceneY();
					
					int boardX;
					int boardY;
					if (board.flipped) {
						boardX = 7 - (int) Math.floor(8 * (getLayoutX() + squareSize / 2 - boardStartX) / boardSize);
						boardY = (int) Math.floor(8 * (getLayoutY() + squareSize / 2 - boardStartY) / boardSize);
					} else {
						boardX = (int) Math.floor(8 * (getLayoutX() + squareSize / 2 - boardStartX) / boardSize);
						boardY = 7 - (int) Math.floor(8 * (getLayoutY() + squareSize / 2 - boardStartY) / boardSize);
					}
					
					if (board.candidateMoves != null) {
						for (Move move : board.candidateMoves) {
							if (move != null) {
								if (showLegalMoves && move.endSq.equals(boardX, boardY)) {
									if (move.isCapture) {
										if (move.endSq.x % 2 == move.endSq.y % 2) board.grid[move.endSq.y * 8 + move.endSq.x].setFill(darkSquareCaptureCol);
										else board.grid[move.endSq.y * 8 + move.endSq.x].setFill(lightSquareCaptureCol);
									} else {
										if (move.endSq.x % 2 == move.endSq.y % 2) board.grid[move.endSq.y * 8 + move.endSq.x].setFill(darkSquareSelectionCol);
										else board.grid[move.endSq.y * 8 + move.endSq.x].setFill(lightSquareSelectionCol);
									}
								} else board.grid[move.endSq.y * 8 + move.endSq.x].resetColour();
							} else break;
						}
					}
				}
			}
		});
		
		setOnMouseReleased(event -> {
			if (!captured && !game.ended && !board.midPromotion && !board.navigating) {
				if (isWhite == board.gameState.whiteToMove && event.getButton() == MouseButton.PRIMARY) {
					// The coordinates of the square which the piece has been dropped on
					int newX;
					int newY;
					if (board.flipped) {
						newX = 7 - (int) Math.floor(8 * (getLayoutX() + squareSize / 2 - boardStartX) / boardSize);
						newY = (int) Math.floor(8 * (getLayoutY() + squareSize / 2 - boardStartY) / boardSize);
					} else {
						newX = (int) Math.floor(8 * (getLayoutX() + squareSize / 2 - boardStartX) / boardSize);
						newY = 7 - (int) Math.floor(8 * (getLayoutY() + squareSize / 2 - boardStartY) / boardSize);
					}
	
					if (board.candidateMoves != null) {
						int moveIndex = 0;
						for (Move move : board.candidateMoves) {  // Compare every possible move to the one the user tried to make
							if (move != null) {
								if (move.endSq.equals(newX, newY)) {  // Sprite dropped on a legal move square
									if (move.finalType != move.pieceType) {  // Promotion, must choose what to promote to
										board.showOptions(moveIndex);
									} else board.doMove(move, false);
									setCursor(Cursor.DEFAULT);
									break;  // The move is legal, don't need to look through any more
								}
								moveIndex++;
							} else {  // The move is not legal
								setCursor(Cursor.OPEN_HAND);  // 'Let go' of the piece
								if (board.secondClick && newX == pos.x && newY == pos.y) board.deactivate();  // Dropped on original square
								break;
							}
						}
					}
					move();  // Move sprite to the middle of its square
					setViewOrder(0);
				} else if (event.getButton() == MouseButton.SECONDARY) {
					board.deactivate();
					move();
					setViewOrder(0);
				}
			}
		});
	}
	
	void setPos(Vector position) {
		pos.copyOf(position);
	}
	void setPos(int x, int y) {
		pos.setXY(x, y);
	}
	
	void setCapturedPos(int x, int y) {
		capturedPos.setXY(x, y);
	}
	
	void move() {
		if (captured) {  // Moves sprite to its correct position amongst the captured pieces
			if (isWhite && !board.flipped || !isWhite && board.flipped) relocate(capturedStartX + capturedXSpacing * capturedPos.x, topCapturedStartY + capturedPieceSize * capturedPos.y);
			else relocate(capturedStartX + capturedXSpacing * capturedPos.x, bottomCapturedStartY + capturedPieceSize * capturedPos.y);
		} else {  // Moves sprite to the middle of its square
			if (board.flipped) relocate(boardStartX + (7 - pos.x) * squareSize, boardStartY + pos.y * squareSize);
			else relocate(boardStartX + pos.x * squareSize, boardStartY + (7 - pos.y) * squareSize);
		}
	}
	
	void moveAnimated(boolean snap, boolean kingCastling) {  // Pass in true for a faster animation, so the piece quickly moves to the centre of its square
		int duration;
		if (snap || !animateMoves) duration = 0;
		else duration = 300;
		TranslateTransition animation = new TranslateTransition(Duration.millis(duration), this);
		
		if (board.flipped) {
			animation.setByX(boardStartX + (7 - pos.x) * squareSize - getLayoutX());
			animation.setByY(boardStartY + pos.y * squareSize - getLayoutY());
		} else {
			animation.setByX(boardStartX + pos.x * squareSize - getLayoutX());
			animation.setByY(boardStartY + (7 - pos.y) * squareSize - getLayoutY());
		}
		
		TranslateTransition pause = new TranslateTransition(Duration.millis(500), this);
		pause.setOnFinished(event -> game.flip());
		
		animation.setOnFinished(event -> {
			setViewOrder(0);
			setTranslateX(0);
			setTranslateY(0);
			move();
			if (game.setup.singlePlayer && !kingCastling) board.startAIThinking();
			else if (autoFlip && !kingCastling && !game.ended) pause.play();
		});
		
		animation.setInterpolator(Interpolator.EASE_OUT);
		animation.play();
	}
}