package application;

import static application.Settings.*;
import static application.Constants.*;

import javafx.scene.shape.Rectangle;

class BoardSquare extends Rectangle {
	Game.Board board;
	int idNum;
	Vector pos;
	
	BoardSquare(Game parentGame, int idNumber) {
		super();
		board = parentGame.board;
		idNum = idNumber;
		pos = new Vector(idNum % 8, idNum / 8);
		
		if (!hideBoardTooltips) {
			String text = "";
			switch (pos.x) {
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
			text += (pos.y + 1);
			Utilities.installTooltip(this, text);
		}
		
		setOnMousePressed(event -> {
			if (event.isPrimaryButtonDown()) board.squareClicked(this);  // Handle two-clicks movement
			else if (event.isSecondaryButtonDown()) board.deactivate();
		});
		
		setOnMouseEntered(event -> {
			if (showLegalMoves && board.candidateMoves != null && !board.midPromotion) {
				for (Move move : board.candidateMoves) {
					if (move != null) {
						if (move.endSq.equals(pos)) {
							if (move.isEnPassant) {
								if (pos.x % 2 == pos.y % 2) board.grid[pos.y * 8 + pos.x].setFill(darkSquareCaptureCol);
								else board.grid[pos.y * 8 + pos.x].setFill(lightSquareCaptureCol);
							} else {
								if (pos.x % 2 == pos.y % 2) setFill(darkSquareSelectionCol);
								else setFill(lightSquareSelectionCol);
							}
						}
					} else break;
				}
			}
		});
		
		setOnMouseExited(event -> {
			if (board.hintStage == HINTOFF || !(board.hintStage == HINTFULL && pos.equals(board.hint.endSq))) resetColour();
		});
	}
	
	void resetColour() {
		if (pos.x % 2 == pos.y % 2) setFill(darkSquareCol);
		else setFill(lightSquareCol);
		if (board.gameState.inCheck(true) && pos.equals(board.gameState.wKingPos) || board.gameState.inCheck(false) && pos.equals(board.gameState.bKingPos)) {
			if (pos.x % 2 == pos.y % 2) setFill(darkSquareCheckCol);
			else setFill(lightSquareCheckCol);
		}
		if (highlightPreviousMove && board.gameState.halfMoves != 0) {
			Move previousMove = board.moveHistory[board.gameState.halfMoves - 1];
			if (previousMove != null && (pos.equals(previousMove.startSq) || pos.equals(previousMove.endSq))) {
				if (pos.x % 2 == pos.y % 2) setFill(darkSquareHighlightCol);
				else setFill(lightSquareHighlightCol);
			}
		}
	}
}