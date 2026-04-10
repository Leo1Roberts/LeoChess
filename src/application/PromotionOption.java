package application;

import static application.Constants.*;
import static application.Settings.*;
import static application.Sizes.Game.*;

import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

class PromotionOption extends Button {
	Game game;
	Game.Graphics graphics;
	Game.Board board;
	private int type;  // Queen, rook, knight or bishop
	int moveIndex;  // The index of the move which this option points to in board.candidateMoves
	
	PromotionOption(Game parentGame, int pieceType) {
		super(Color.TRANSPARENT, Color.TRANSPARENT, null);
		game = parentGame;
		board = game.board;
		graphics = game.graphics;
		type = pieceType;
		setViewOrder(1.5);
		imageView.setViewOrder(1.5);
		
		if (!hideBoardTooltips) {
			switch (type) {
			case KNIGHT:
				Utilities.installTooltip(this, "Promote to a knight");
				break;
			case BISHOP:
				Utilities.installTooltip(this, "Promote to a bishop");
				break;
			case ROOK:
				Utilities.installTooltip(this, "Promote to a rook");
				break;
			case QUEEN:
				Utilities.installTooltip(this, "Promote to a queen");
				break;
			}
		}
	}
	
	void update(int baseIndex) {  // The index in board.candidateMoves of the first promotion move of the block of four
		if (board.gameState.whiteToMove) {
			switch (type) {
			case QUEEN:
				imageView.setImage(graphics.queenWpng);
				moveIndex = baseIndex;
				break;
			case ROOK:
				imageView.setImage(graphics.rookWpng);
				moveIndex = baseIndex + 1;
				break;
			case KNIGHT:
				imageView.setImage(graphics.knightWpng);
				moveIndex = baseIndex + 2;
				break;
			case BISHOP:
				imageView.setImage(graphics.bishopWpng);
				moveIndex = baseIndex + 3;
				break;
			default:
				moveIndex = -1;
			}
		} else {
			switch (type) {
			case QUEEN:
				imageView.setImage(graphics.queenBpng);
				moveIndex = baseIndex;
				break;
			case ROOK:
				imageView.setImage(graphics.rookBpng);
				moveIndex = baseIndex + 1;
				break;
			case KNIGHT:
				imageView.setImage(graphics.knightBpng);
				moveIndex = baseIndex + 2;
				break;
			case BISHOP:
				imageView.setImage(graphics.bishopBpng);
				moveIndex = baseIndex + 3;
				break;
			default:
				moveIndex = -1;
			}
		}
		int x = board.candidateMoves[moveIndex].endSq.x;
		int y = board.candidateMoves[moveIndex].endSq.y;
		if (x % 2 == y % 2) {
			if (board.grid[y * 8 + x].getFill().equals(darkSquareHighlightCol)) setOnMouseEntered(event -> setFill(lightSquareHighlightCol));
			else setOnMouseEntered(event -> setFill(lightSquareCol));
		} else {
			if (board.grid[y * 8 + x].getFill().equals(lightSquareHighlightCol)) setOnMouseEntered(event -> setFill(darkSquareHighlightCol));
			else setOnMouseEntered(event -> setFill(darkSquareCol));
		}
		
		setWidth(squareSize / 2);
		setHeight(squareSize / 2);
		imageView.setFitWidth(squareSize / 2);
		imageView.setFitHeight(squareSize / 2);
		
		if (board.flipped) relocate(boardStartX + (7 - board.candidateMoves[baseIndex].endSq.x) * squareSize + (moveIndex % 2) * squareSize / 2, boardStartY + board.candidateMoves[baseIndex].endSq.y * squareSize + (moveIndex - baseIndex) / 2 * squareSize / 2);
		else relocate(boardStartX + board.candidateMoves[baseIndex].endSq.x * squareSize + (moveIndex % 2) * squareSize / 2, boardStartY + (7 - board.candidateMoves[baseIndex].endSq.y) * squareSize + (moveIndex - baseIndex) / 2 * squareSize / 2);
		
		repositionImage();
		
		setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) board.doPromotion(board.candidateMoves[moveIndex]);
			else if (event.getButton() == MouseButton.SECONDARY) board.cancelPromotion();
		});
	}
}