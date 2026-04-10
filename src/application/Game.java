package application;

import static application.Constants.*;
import static application.Sizes.Game.*;
import static application.Settings.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

class Game {
	Scene scene;
	Group root;
	Graphics graphics;
	Board board;
	Buttons buttons;
	Scanner inp;
	GameSetup setup;
	boolean ended;
	boolean playerIsWhite;
	int numberOfLines;
	int topLineIndex;
	int firstStateIndex;
	
	Game(Scene parentScene, Group gameRoot, Scanner systemInp) {
		scene = parentScene;
		root = gameRoot;
		graphics = new Graphics();
		board = new Board();
		buttons = new Buttons();
		inp = systemInp;
	}
	
	void init(GameSetup gameSetup) {
		root.getChildren().clear();
		setup = gameSetup;
		switch (setup.playerColour) {
		case WHITE:
			playerIsWhite = true;
			break;
		case BLACK:
			playerIsWhite = false;
			break;
		case WHITETOGGLE:
			playerIsWhite = true;
			break;
		case BLACKTOGGLE:
			playerIsWhite = false;
			break;
		case RANDOM:
			playerIsWhite = new Random().nextBoolean();
		}
		
		scene.setFill(voidFillerCol);  // The colour 'behind' all nodes
		graphics.init();
		board.init();
		buttons.init();
		initMoves();
		hidePopup();
		ended = false;
		numberOfLines = 0;
		topLineIndex = firstStateIndex / 2;
		resizeAll();

		if (setup.singlePlayer && !playerIsWhite || !board.gameState.whiteToMove && !setup.singlePlayer) flip();
		board.startAIThinking();
	}
	
	void resizeAll() {
		updateSizes(scene.getWidth(), scene.getHeight());
		graphics.resize();
		board.resize();
		buttons.resize();
		resizeMoves();
	}
	
	void exit() {
		board.ai.cancel = true;
		if (setup.timeSecs > 0) {
			if (board.gameState.whiteToMove) board.whiteTimeline.pause();
			else board.blackTimeline.pause();
		}
		
		Alert alert = new Alert(AlertType.CONFIRMATION, "Abandon game? All progress will be lost.");
		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.isPresent()) {
			if (result.get() == ButtonType.OK) {
				if (setup.timeSecs > 0) {
					board.whiteTimeline.stop();
					board.blackTimeline.stop();
				}
				Main.loadEntry(setup);
			} else {
				board.ai.cancel = false;
				board.startAIThinking();
				if (setup.timeSecs > 0 && !ended && board.gameState.halfMoves > 1) {
					if (board.gameState.whiteToMove) board.whiteTimeline.play();
					else board.blackTimeline.play();
				}
			}
		}
	}

	void reset() {
		board.ai.cancel = true;
		if (setup.timeSecs > 0) {
			if (board.gameState.whiteToMove) board.whiteTimeline.pause();
			else board.blackTimeline.pause();
		}
		
		Alert alert = new Alert(AlertType.CONFIRMATION, "Restart game? All progress will be lost.");
		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.isPresent()) {
			if (result.get() == ButtonType.OK) {
				if (setup.singlePlayer) {
					if (setup.playerColour == RANDOM) {
						if (playerIsWhite) setup.playerColour = WHITETOGGLE;
						else setup.playerColour = BLACKTOGGLE;
					}
				} else if (setup.timeSecs > 0) {
					board.whiteTimeline.stop();
					board.blackTimeline.stop();
				}
				init(setup);
			} else {
				board.ai.cancel = false;
				board.startAIThinking();
				if (setup.timeSecs > 0 && !ended && board.gameState.halfMoves > 1) {
					if (board.gameState.whiteToMove) board.whiteTimeline.play();
					else board.blackTimeline.play();
				}
			}
		}
	}
	
	void flip() {
		board.flipped = !board.flipped;
		resizeAll();
	}
	
	void navBackward() {
		if (setup.timeSecs == 0 || ended) {
			if (board.endAnimation) {
				board.navBackwardCued = true;
				board.flash.jumpTo(board.flash.getTotalDuration().subtract(Duration.millis(1)));
			} else {
				if (ended && board.history[board.gameState.halfMoves + 1] == null && graphics.popupArea.isVisible()) {  // Popup is up
					hidePopup();
					if (board.stateType == WHITERESIGNED || board.stateType == BLACKRESIGNED || board.stateType == DRAWAGREED) {
						if (board.midPromotion) board.cancelPromotion();
						board.navigating = true;
						buttons.undoMove.deactivate();
						if (board.gameState.halfMoves == 0 || board.history[board.gameState.halfMoves - 1] == null) buttons.navBackward.deactivate();
					}
					buttons.navForward.activate();
				} else if (board.gameState.halfMoves > 0 && board.history[board.gameState.halfMoves - 1] != null && !board.midPromotion && !board.aiThinking) {  // Popup is down and there is a previous move
					board.navigating = true;
					buttons.wResign.deactivate();
					buttons.bResign.deactivate();
					buttons.draw.deactivate();
					board.deactivate();
					
					int lineIndex = (board.gameState.halfMoves - 1) / 2;
					if (board.gameState.whiteToMove) {
						buttons.moves[lineIndex][1].setFill(Color.TRANSPARENT);
						buttons.moves[lineIndex][1].setOnMouseExited(event -> buttons.moves[lineIndex][1].setFill(Color.TRANSPARENT));
					} else {
						buttons.moves[lineIndex][0].setFill(Color.TRANSPARENT);
						buttons.moves[lineIndex][0].setOnMouseExited(event -> buttons.moves[lineIndex][0].setFill(Color.TRANSPARENT));
					}
					
					board.gameState.copyOf(board.history[board.gameState.halfMoves - 1]);
					board.initSprites();
					graphics.updateCapturedAreas();
					board.updateGridFill();
					
					buttons.navForward.activate();
					buttons.undoMove.deactivate();
					buttons.hint.deactivate();
					if (board.gameState.halfMoves == 0 || board.history[board.gameState.halfMoves - 1] == null) {
						buttons.navBackward.deactivate();
					} else {
						int lineIndexNew = (board.gameState.halfMoves - 1) / 2;
						if (board.gameState.whiteToMove) {
							buttons.moves[lineIndexNew][1].setFill(moveListHighlightCol);
							buttons.moves[lineIndexNew][1].setOnMouseExited(event -> buttons.moves[lineIndexNew][1].setFill(moveListHighlightCol));
						} else {
							buttons.moves[lineIndexNew][0].setFill(moveListHighlightCol);
							buttons.moves[lineIndexNew][0].setOnMouseExited(event -> buttons.moves[lineIndexNew][0].setFill(moveListHighlightCol));
						}
						
						if (lineIndexNew < topLineIndex) scrollMoves(1);  // Make sure move is on screen
					}
					Utilities.installTooltip(buttons.navForward, "Next move (right arrow, hold Ctrl to jump to end)");
				}
			}
		}
	}
	
	void navForward() {
		if (setup.timeSecs == 0 || ended) {
			if (board.endAnimation) {
				board.flash.jumpTo(board.flash.getTotalDuration().subtract(Duration.millis(1)));
			} else {
				if (ended && board.history[board.gameState.halfMoves + 1] == null && !graphics.popupArea.isVisible()) {  // Show popup
					showPopup();
					board.navigating = false;
					buttons.navForward.deactivate();
					buttons.navBackward.activate();
					if (setup.timeSecs == 0 && allowTakingBackMoves) buttons.undoMove.activate();
				} else if (board.history[board.gameState.halfMoves + 1] != null && !board.aiThinking) {  // Popup is down and there is a next move
					if (board.midPromotion) board.cancelPromotion();
					
					int lineIndex = (board.gameState.halfMoves - 1) / 2;
					if (board.gameState.whiteToMove) {
						buttons.moves[lineIndex][1].setFill(Color.TRANSPARENT);
						buttons.moves[lineIndex][1].setOnMouseExited(event -> buttons.moves[lineIndex][1].setFill(Color.TRANSPARENT));
					} else {
						buttons.moves[lineIndex][0].setFill(Color.TRANSPARENT);
						buttons.moves[lineIndex][0].setOnMouseExited(event -> buttons.moves[lineIndex][0].setFill(Color.TRANSPARENT));
					}
					
					board.gameState.copyOf(board.history[board.gameState.halfMoves + 1]);
					board.initSprites();
					graphics.updateCapturedAreas();
					board.updateGridFill();
					
					int lineIndexNew = (board.gameState.halfMoves - 1) / 2;
					if (board.gameState.whiteToMove) {
						buttons.moves[lineIndexNew][1].setFill(moveListHighlightCol);
						buttons.moves[lineIndexNew][1].setOnMouseExited(event -> buttons.moves[lineIndexNew][1].setFill(moveListHighlightCol));
					} else {
						buttons.moves[lineIndexNew][0].setFill(moveListHighlightCol);
						buttons.moves[lineIndexNew][0].setOnMouseExited(event -> buttons.moves[lineIndexNew][0].setFill(moveListHighlightCol));
					}
					
					if (lineIndexNew == topLineIndex + maxVisibleLines) scrollMoves(-1);  // Make sure move is on screen
					
					if (board.history[board.gameState.halfMoves + 1] == null) {
						board.navigating = false;
						if (ended) {
							Utilities.installTooltip(buttons.navForward, "Show result card (right arrow)");
							if (board.stateType != WHITERESIGNED && board.stateType != BLACKRESIGNED && board.stateType != DRAWAGREED && setup.timeSecs == 0 && allowTakingBackMoves) buttons.undoMove.activate();
						} else {
							buttons.wResign.activate();
							buttons.bResign.activate();
							buttons.draw.activate();
							buttons.navForward.deactivate();
							if (setup.timeSecs == 0 && allowTakingBackMoves) buttons.undoMove.activate();
							if (allowMoveSuggestions) buttons.hint.activate();
						}
					}
					buttons.navBackward.activate();
				}
			}
		}
	}
	
	void navToStart() {
		if (setup.timeSecs == 0 || ended) {
			if (board.endAnimation) {
				board.navToStartCued = true;
				board.flash.jumpTo(board.flash.getTotalDuration().subtract(Duration.millis(1)));
			} else {
				while (board.gameState.halfMoves != firstStateIndex) navBackward();
				navBackward();  // In case of resignation/draw before first move
			}
		}
	}
	
	void navToEnd() {
		if (setup.timeSecs == 0 || ended) {
			if (board.endAnimation) board.flash.jumpTo(board.flash.getTotalDuration().subtract(Duration.millis(1)));
			else for (int i = 0; i < 500; i++) navForward();
		}
	}
	
	void undoMove() {
		if (setup.timeSecs == 0 && allowTakingBackMoves) {
			if (board.endAnimation) {
				board.undoMoveCued = true;
				board.flash.jumpTo(board.flash.getTotalDuration().subtract(Duration.millis(1)));
			} else {
				if (ended && board.history[board.gameState.halfMoves + 1] == null && graphics.popupArea.isVisible() && (board.stateType == WHITERESIGNED || board.stateType == BLACKRESIGNED || board.stateType == DRAWAGREED)) {
					ended = false;
					buttons.wResign.activate();
					buttons.bResign.activate();
					buttons.draw.activate();
					board.stateType = board.gameState.updateState(board.history);
					hidePopup();
					buttons.navForward.deactivate();
					if (board.gameState.halfMoves == 0 || board.history[board.gameState.halfMoves - 1] == null) {
						buttons.navBackward.deactivate();
						buttons.undoMove.deactivate();
					}
					if (!setup.singlePlayer || playerIsWhite == board.gameState.whiteToMove) board.generateHint();
					Utilities.installTooltip(buttons.undoMove, "Take back last move (backspace)");
					board.ai.cancel = false;
					board.startAIThinking();
				} else if (board.gameState.halfMoves > 0 && board.history[board.gameState.halfMoves - 1] != null && board.stateType != WHITERESIGNED && board.stateType != BLACKRESIGNED && board.stateType != DRAWAGREED && !board.midPromotion && !board.navigating && !board.aiThinking) {
					board.deactivate();
					board.history[board.gameState.halfMoves] = null;
					board.captureHistory[board.gameState.halfMoves] = null;
					
					int lineIndex = (board.gameState.halfMoves - 1) / 2;
					if (board.gameState.whiteToMove) {
						buttons.moves[lineIndex][1].setVisible(false);
						buttons.moves[lineIndex][1].text.setText("");
						buttons.moves[lineIndex][1].text.setVisible(false);
					} else {
						numberOfLines--;
						graphics.moveNumbers[lineIndex].setVisible(false);
						buttons.moves[lineIndex][0].setVisible(false);
						buttons.moves[lineIndex][0].text.setText("");
						buttons.moves[lineIndex][0].text.setVisible(false);
						if (lineIndex >= maxVisibleLines - 1) scrollMoves(1);
					}
					
					board.gameState.copyOf(board.history[board.gameState.halfMoves - 1]);
					board.initSprites();
					graphics.updateCapturedAreas();
					board.updateGridFill();
					if (!setup.singlePlayer && autoFlip && !ended) flip();
					if (ended) {
						hidePopup();
						ended = false;
						buttons.wResign.activate();
						buttons.bResign.activate();
						buttons.draw.activate();
					}
					buttons.navForward.deactivate();
					buttons.hint.deactivate();
					if (!setup.singlePlayer || playerIsWhite == board.gameState.whiteToMove) board.generateHint();
					
					if (board.gameState.halfMoves == 0 || board.history[board.gameState.halfMoves - 1] == null) {
						buttons.navBackward.deactivate();
						buttons.undoMove.deactivate();
					} else {
						int lineIndexNew = (board.gameState.halfMoves - 1) / 2;
						if (board.gameState.whiteToMove) {
							buttons.moves[lineIndexNew][1].setFill(moveListHighlightCol);
							buttons.moves[lineIndexNew][1].setOnMouseExited(event -> buttons.moves[lineIndexNew][1].setFill(moveListHighlightCol));
						} else {
							buttons.moves[lineIndexNew][0].setFill(moveListHighlightCol);
							buttons.moves[lineIndexNew][0].setOnMouseExited(event -> buttons.moves[lineIndexNew][0].setFill(moveListHighlightCol));
						}
					}
				}
				Utilities.installTooltip(buttons.navForward, "Next move (right arrow, hold Ctrl to jump to end)");
			}
		}
	}
	
	void showPopup() {
		graphics.resultText1.setFill(Color.BLACK);
		graphics.resultText2.setFill(Color.GREY);
		buttons.copyPgn.text.setFill(Color.GREY);
		buttons.hidePopup.text.setFill(Color.GREY);
		
		switch (board.stateType) {
		case CHECKMATE:
			if (board.gameState.whiteToMove) {
				graphics.popupArea.setFill(Color.BLACK);
				graphics.resultText1.setText("Black wins");
				graphics.resultText1.setFill(Color.WHITE);
			} else {
				graphics.popupArea.setFill(Color.WHITE);
				graphics.resultText1.setText("White wins");
			}
			graphics.resultText2.setText("checkmate");
			break;
		case STALEMATE:
			graphics.popupArea.setFill(Color.DARKGREY);
			graphics.resultText1.setText("Draw");
			graphics.resultText2.setText("stalemate");
			graphics.resultText2.setFill(Color.web("666666"));
			buttons.copyPgn.text.setFill(Color.web("666666"));
			buttons.hidePopup.text.setFill(Color.web("666666"));
			break;
		case REPETITION:
			graphics.popupArea.setFill(Color.DARKGREY);
			graphics.resultText1.setText("Draw");
			graphics.resultText2.setText("threefold repetition");
			graphics.resultText2.setFill(Color.web("666666"));
			buttons.copyPgn.text.setFill(Color.web("666666"));
			buttons.hidePopup.text.setFill(Color.web("666666"));
			break;
		case FIFTYMOVE:
			graphics.popupArea.setFill(Color.DARKGREY);
			graphics.resultText1.setText("Draw");
			graphics.resultText2.setText("fifty-move rule");
			graphics.resultText2.setFill(Color.web("666666"));
			buttons.copyPgn.text.setFill(Color.web("666666"));
			buttons.hidePopup.text.setFill(Color.web("666666"));
			break;
		case INSUF:
			graphics.popupArea.setFill(Color.DARKGREY);
			graphics.resultText1.setText("Draw");
			graphics.resultText2.setText("insufficient material");
			graphics.resultText2.setFill(Color.web("666666"));
			buttons.copyPgn.text.setFill(Color.web("666666"));
			buttons.hidePopup.text.setFill(Color.web("666666"));
			break;
		case WHITETIMEOUT:
			graphics.popupArea.setFill(Color.BLACK);
			graphics.resultText1.setText("Black wins");
			graphics.resultText2.setText("on time");
			graphics.resultText1.setFill(Color.WHITE);
			break;
		case BLACKTIMEOUT:
			graphics.popupArea.setFill(Color.WHITE);
			graphics.resultText1.setText("White wins");
			graphics.resultText2.setText("on time");
			break;
		case WHITERESIGNED:
			graphics.popupArea.setFill(Color.BLACK);
			graphics.resultText1.setText("Black wins");
			graphics.resultText2.setText("resignation");
			graphics.resultText1.setFill(Color.WHITE);
			break;
		case BLACKRESIGNED:
			graphics.popupArea.setFill(Color.WHITE);
			graphics.resultText1.setText("White wins");
			graphics.resultText2.setText("resignation");
			break;
		case DRAWAGREED:
			graphics.popupArea.setFill(Color.DARKGREY);
			graphics.resultText1.setText("Draw");
			graphics.resultText2.setText("agreement");
			graphics.resultText2.setFill(Color.web("666666"));
			buttons.copyPgn.text.setFill(Color.web("666666"));
			buttons.hidePopup.text.setFill(Color.web("666666"));
		}
		
		graphics.resizePopupText();
		
		graphics.popupArea.setVisible(true);
		graphics.resultText1.setVisible(true);
		graphics.resultText2.setVisible(true);
		buttons.rematch.setVisible(true);
		buttons.rematch.text.setVisible(true);
		buttons.newGame.setVisible(true);
		buttons.newGame.text.setVisible(true);
		//if (firstStateIndex == 0) {  // PGN must start with move 1
			buttons.copyPgn.setVisible(true);
			buttons.copyPgn.text.setVisible(true);
		//}
		buttons.hidePopup.setVisible(true);
		buttons.hidePopup.text.setVisible(true);
		
		Utilities.installTooltip(buttons.navForward, "Show result card (right arrow)");
	}
	
	void hidePopup() {
		graphics.popupArea.setVisible(false);
		graphics.resultText1.setVisible(false);
		graphics.resultText2.setVisible(false);
		buttons.rematch.setVisible(false);
		buttons.rematch.text.setVisible(false);
		buttons.newGame.setVisible(false);
		buttons.newGame.text.setVisible(false);
		buttons.copyPgn.setVisible(false);
		buttons.copyPgn.text.setVisible(false);
		buttons.hidePopup.setVisible(false);
		buttons.hidePopup.text.setVisible(false);
	}
	
	void timeout() {
		board.whiteTimeline.stop();
		board.blackTimeline.stop();
		ended = true;
		board.deactivate();
		buttons.wResign.deactivate();
		buttons.bResign.deactivate();
		buttons.draw.deactivate();
		buttons.giveWhiteTime.deactivate();
		buttons.giveBlackTime.deactivate();
		if (setup.timeSecs > 0) activateMoves();
		showPopup();
		if (board.midPromotion) board.cancelPromotion();
		if (board.activeSpriteId >= 0) {
			board.pieces[board.activeSpriteId].move();
			board.deactivate();
		}
		buttons.draw.text.setText("DRAW");
		buttons.draw.repositionText();
		buttons.navBackward.activate();
		buttons.hint.deactivate();
	}
	
	void initMoves() {
		for (int i = 0; i < 250; i++) {
			graphics.moveNumbers[i] = new Text(Integer.toString(i + 1) + ".");
			graphics.moveNumbers[i].setTextOrigin(VPos.CENTER);
			graphics.moveNumbers[i].setViewOrder(-1.5);
			graphics.moveNumbers[i].setFill(boardMarginTextCol);
			graphics.moveNumbers[i].setVisible(false);
			graphics.moveNumbers[i].setMouseTransparent(true);
			root.getChildren().add(graphics.moveNumbers[i]);
			
			for (int j = 0; j < 2; j++) {
				buttons.moves[i][j] = new Button(Color.TRANSPARENT, moveListHoverCol, "", textCol, textCol);
				root.getChildren().addAll(buttons.moves[i][j], buttons.moves[i][j].text);
				buttons.moves[i][j].setViewOrder(-1.5);
				buttons.moves[i][j].text.setViewOrder(-1.5);
				buttons.moves[i][j].setVisible(false);
				buttons.moves[i][j].text.setVisible(false);
				buttons.moves[i][j].setOnScroll(event -> {
					scrollMoves((int) Math.round(event.getTextDeltaY()));
				});
				if (setup.timeSecs > 0) buttons.moves[i][j].deactivate();
				
				final int iCopy = i;
				final int jCopy = j;
				buttons.moves[i][j].setOnMouseClicked(event -> navToMove(iCopy * 2 + jCopy));
			}
			
		}
	}
	
	void resizeMoves() {
		for (int i = 0; i < 250; i++) {
			graphics.moveNumbers[i].setFont(Font.font("Arial Rounded MT Bold", moveTextSize));
			graphics.moveNumbers[i].setX(moveAreaStartX + strokeWidth * 5);
			graphics.moveNumbers[i].setY(moveAreaStartY + moveButtonHeight + strokeWidth + (i - topLineIndex + 0.5) * moveHeight);
			
			for (int j = 0; j < 2; j++) {
				buttons.moves[i][j].setWidth(moveWidth);
				buttons.moves[i][j].setHeight(moveHeight);
				buttons.moves[i][j].text.setFont(Font.font("Arial Rounded MT Bold", moveTextSize));
				buttons.moves[i][j].text.setY(moveAreaStartY + moveButtonHeight + strokeWidth + (i - topLineIndex) * moveHeight + moveHeight / 2);
				
				if (j == 0) {
					buttons.moves[i][j].text.setX(whiteMoveStartX + strokeWidth * 5);
					buttons.moves[i][j].relocate(whiteMoveStartX, moveAreaStartY + moveButtonHeight + strokeWidth + (i - topLineIndex) * moveHeight);
				} else {
					buttons.moves[i][j].text.setX(blackMoveStartX + strokeWidth * 5);
					buttons.moves[i][j].relocate(blackMoveStartX, moveAreaStartY + moveButtonHeight + strokeWidth + (i - topLineIndex) * moveHeight);
				}
			}
		}
	}
	
	void displayMove() {
		resizeMoves();
		
		int lineIndex = (board.gameState.halfMoves - 1) / 2;
		
		numberOfLines = lineIndex + 1;
		
		graphics.moveNumbers[lineIndex].setVisible(true);
		
		String moveText = Utilities.moveToSAN(board.history, board.moveHistory[board.gameState.halfMoves - 1], board.gameState.halfMoves - 1);
		
		if (board.gameState.whiteToMove) {
			buttons.moves[lineIndex][1].text.setText(moveText);
			buttons.moves[lineIndex][1].setVisible(true);
			buttons.moves[lineIndex][1].text.setVisible(true);
			buttons.moves[lineIndex][0].setFill(Color.TRANSPARENT);
			buttons.moves[lineIndex][0].setOnMouseExited(event -> buttons.moves[lineIndex][0].setFill(Color.TRANSPARENT));
			buttons.moves[lineIndex][1].setFill(moveListHighlightCol);
			buttons.moves[lineIndex][1].setOnMouseExited(event -> buttons.moves[lineIndex][1].setFill(moveListHighlightCol));
		} else {
			buttons.moves[lineIndex][0].text.setText(moveText);
			buttons.moves[lineIndex][0].setVisible(true);
			buttons.moves[lineIndex][0].text.setVisible(true);
			if (lineIndex > 0) {
				buttons.moves[lineIndex - 1][1].setFill(Color.TRANSPARENT);
				buttons.moves[lineIndex - 1][1].setOnMouseExited(event -> buttons.moves[lineIndex - 1][1].setFill(Color.TRANSPARENT));
			}
			buttons.moves[lineIndex][0].setFill(moveListHighlightCol);
			buttons.moves[lineIndex][0].setOnMouseExited(event -> buttons.moves[lineIndex][0].setFill(moveListHighlightCol));
		}
		
		if (lineIndex >= topLineIndex + maxVisibleLines) {
			scrollMoves(maxVisibleLines + topLineIndex - lineIndex - 1);
		}
	}
	
	void navToMove(int targetIndex) {
		if (setup.timeSecs == 0 || ended) {
			int currentIndex = board.gameState.halfMoves - 1;  // IMPORTANT since board.gameState.halfMoves changes with function calls
			
			if (currentIndex < targetIndex) {  // Clicked ahead to a future move
				for (int i = 0; i < targetIndex - currentIndex; i++) navForward();
			} else if (currentIndex > targetIndex) {  // Clicked back to a previous move
				if (graphics.popupArea.isVisible()) navBackward();
				for (int i = 0; i < currentIndex - targetIndex; i++) {
					navBackward();
				}
			} else if (graphics.popupArea.isVisible()) navBackward();  // Popup is visible but user clicked on last move, hide the popup
		}
	}
	
	void scrollMoves(int lines) {
		if (lines > 0 && topLineIndex > firstStateIndex / 2) {  // Scrolled up, move the list down
			int actualLines = Math.min(lines, topLineIndex - firstStateIndex / 2);
			for (int i = 0; i < actualLines; i++) {
				graphics.moveNumbers[topLineIndex + maxVisibleLines - 1 - i].setVisible(false);
				graphics.moveNumbers[topLineIndex - 1 - i].setVisible(true);
				for (int j = 0; j < 2; j++) {
					buttons.moves[topLineIndex + maxVisibleLines - 1 - i][j].setVisible(false);
					buttons.moves[topLineIndex + maxVisibleLines - 1 - i][j].text.setVisible(false);
					if (!buttons.moves[topLineIndex - 1 - i][j].text.getText().equals("")) {
						buttons.moves[topLineIndex - 1 - i][j].setVisible(true);
						buttons.moves[topLineIndex - 1 - i][j].text.setVisible(true);
					}
				}
			}
			topLineIndex -= actualLines;
			resizeMoves();
		} else if (lines < 0 && numberOfLines > topLineIndex + maxVisibleLines) {  // Scrolled down, move the list up
			int actualLines = Math.min(-lines, numberOfLines - topLineIndex - maxVisibleLines);
			for (int i = 0; i < actualLines; i++) {
				graphics.moveNumbers[topLineIndex + i].setVisible(false);
				graphics.moveNumbers[topLineIndex + maxVisibleLines + i].setVisible(true);
				for (int j = 0; j < 2; j++) {
					buttons.moves[topLineIndex + i][j].setVisible(false);
					buttons.moves[topLineIndex + i][j].text.setVisible(false);
					if (!buttons.moves[topLineIndex + maxVisibleLines + i][j].text.getText().equals("")) {
						buttons.moves[topLineIndex + maxVisibleLines + i][j].setVisible(true);
						buttons.moves[topLineIndex + maxVisibleLines + i][j].text.setVisible(true);
					}
				}
			}
			topLineIndex += actualLines;
			resizeMoves();
		}
	}
	
	void activateMoves() {
		for (int i = 0; i < 250; i++) {
			for (int j = 0; j < 2; j++) {
				buttons.moves[i][j].activate();
			}
		}
	}
	
	String gameToPgn() {
		String pgn = "[Event \"";
		
		if (setup.singlePlayer) pgn += "Single player";
		else {
			pgn += "Two player - ";
			if (setup.timeSecs == 0) pgn += "untimed";
			else {
				int mins = setup.timeSecs / 60;
				int secs = setup.timeSecs % 60;
				pgn += mins + ":";
				if (secs < 10) pgn += 0;
				pgn += secs + " + " + setup.incrementSecs;
			}
		}
		
		pgn += "\"]\n[Site \"LeoChess\"]\n[Date \"" + LocalDate.now().toString().replace('-', '.') + "\"]\n[Round \"?\"]\n[White \"";
		
		if (setup.singlePlayer) {
			String aiName = "LeoChess AI, Depth " + setup.aiDepth;
			if (playerIsWhite) pgn += "?\"]\n[Black \"" + aiName;
			else pgn += aiName + "\"]\n[Black \"?";
		} else pgn += "?\"]\n[Black \"?";
		
		pgn += "\"]\n[Result \"";
		
		String result = "";
		
		if (ended) {
			if (board.stateType == DRAWAGREED || board.stateType == STALEMATE || board.stateType == REPETITION || board.stateType == FIFTYMOVE || board.stateType == INSUF) result = "1/2-1/2";
			else if (board.stateType == WHITERESIGNED || board.stateType == WHITETIMEOUT || board.gameState.whiteToMove) result = "0-1";
			else if (board.stateType == BLACKRESIGNED || board.stateType == BLACKTIMEOUT || !board.gameState.whiteToMove) result = "1-0";
		} else pgn += "*";
		
		pgn += result + "\"]\n";
		
		String fen = Utilities.boardToFen(board.history[firstStateIndex]);
		if (!fen.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")) {
			pgn += "[SetUp \"1\"]\n[FEN \"" + fen + "\"]\n";
		}
		
		pgn += "\n";
		
		String[] moves = Utilities.gameToSAN(board.history, board.moveHistory);
		
		int firstIndex = 0;
		for (int i = 0; i < moves.length; i++) {
			if (moves[i] != null) {
				firstIndex = i;
				break;
			}
		}
		
		if (firstIndex % 2 == 1) pgn += (firstIndex / 2 + 1) + "... ";  // Special case for when black has the first move
		for (int i = firstIndex; i < moves.length; i++) {
			if (i % 2 == 0) pgn += (i / 2 + 1) + ". ";
			pgn += moves[i] + " ";
		}
		
		pgn += result;
		
		return pgn;
	}
	
	class Graphics {  // For purely graphical elements that have no functionality
		Image pawnWpng;
		Image pawnBpng;
		Image knightWpng;
		Image knightBpng;
		Image bishopWpng;
		Image bishopBpng;
		Image rookWpng;
		Image rookBpng;
		Image queenWpng;
		Image queenBpng;
		Image kingWpng;
		Image kingBpng;
		Rectangle panelL;
		Rectangle topCapturedArea;
		Rectangle bottomCapturedArea;
		Text materialAdvantage;
		Rectangle panelR;
		Rectangle playArea;
		Rectangle boardBorder;
		Text[] topCoordinates;
		Text[] bottomCoordinates;
		Text[] leftCoordinates;
		Text[] rightCoordinates;
		Rectangle popupArea;
		DropShadow popupAreaShadow;
		Text resultText1;
		Text resultText2;
		Rectangle moveArea;
		Rectangle gameBorder;
		Shape moveButtonsCover;
		Rectangle moveAreaBorder;
		Rectangle clock;
		Rectangle clockTopBox;
		Rectangle clockBottomBox;
		Shape clockTop;
		Shape clockBottom;
		Text whiteTime;
		Text blackTime;
		Rectangle moveTitleArea;
		Text movesTitle;
		Text[] moveNumbers;
		
		void init() {
			String pathStart = "file:assets/pieces/" + piecesFolder + "/";
			pawnWpng = new Image(pathStart + "Pawn W.png");
			pawnBpng = new Image(pathStart + "Pawn B.png");
			knightWpng = new Image(pathStart + "Knight W.png");
			knightBpng = new Image(pathStart + "Knight B.png");
			bishopWpng = new Image(pathStart + "Bishop W.png");
			bishopBpng = new Image(pathStart + "Bishop B.png");
			rookWpng = new Image(pathStart + "Rook W.png");
			rookBpng = new Image(pathStart + "Rook B.png");
			queenWpng = new Image(pathStart + "Queen W.png");
			queenBpng = new Image(pathStart + "Queen B.png");
			kingWpng = new Image(pathStart + "King W.png");
			kingBpng = new Image(pathStart + "King B.png");
			
			panelL = new Rectangle(0, 0, backingCardCol);
			panelL.setViewOrder(4);
			topCapturedArea = new Rectangle(0, 0, panelCol1);
			topCapturedArea.setStroke(borderCol);
			bottomCapturedArea = new Rectangle(0, 0, panelCol1);
			bottomCapturedArea.setStroke(borderCol);
			materialAdvantage = new Text();
			materialAdvantage.setTextOrigin(VPos.CENTER);
			materialAdvantage.setFill(textCol);
			
			panelR = new Rectangle(0, 0, backingCardCol);
			panelR.setViewOrder(4);
			moveArea = new Rectangle(0, 0, panelCol1);
			moveArea.setViewOrder(-1);
			moveArea.setStroke(borderCol);
			moveArea.setOnScroll(event -> {
				scrollMoves((int) Math.round(event.getTextDeltaY()));
			});
			moveAreaBorder = new Rectangle(0, 0, Color.TRANSPARENT);
			moveAreaBorder.setViewOrder(-3);
			moveAreaBorder.setStroke(borderCol);
			moveAreaBorder.setMouseTransparent(true);
			
			playArea = new Rectangle(0, 0, panelCol1);
			playArea.setViewOrder(3);
			playArea.setStrokeType(StrokeType.INSIDE);
			playArea.setStroke(borderCol);
			boardBorder = new Rectangle(0, 0, Color.TRANSPARENT);
			boardBorder.setStroke(borderCol);
			boardBorder.setMouseTransparent(true);
			boardBorder.setViewOrder(1);
			
			popupArea = new Rectangle(0, 0, Color.GREY);
			popupArea.setViewOrder(-5);
			popupArea.setStroke(borderCol);
			popupAreaShadow = new DropShadow();
			popupArea.setEffect(popupAreaShadow);
			resultText1 = new Text();
			resultText1.setTextOrigin(VPos.CENTER);
			resultText1.setViewOrder(-6);
			resultText2 = new Text();
			resultText2.setTextOrigin(VPos.CENTER);
			resultText2.setViewOrder(-6);
			
			gameBorder = new Rectangle(0, 0, Color.TRANSPARENT);
			gameBorder.setStroke(borderCol);
			gameBorder.setStrokeType(StrokeType.INSIDE);
			gameBorder.setMouseTransparent(true);
			gameBorder.setViewOrder(-7);
			
			if (setup.timeSecs > 0) {
				clock = new Rectangle();
				clockTopBox = new Rectangle();
				clockBottomBox = new Rectangle();
				
				whiteTime = new Text();
				whiteTime.setTextOrigin(VPos.CENTER);
				whiteTime.setFill(Color.GREY);
				whiteTime.setViewOrder(-1);
				blackTime = new Text();
				blackTime.setTextOrigin(VPos.CENTER);
				blackTime.setFill(Color.GREY);
				blackTime.setViewOrder(-1);
				root.getChildren().addAll(whiteTime, blackTime);
			}
			
			moveTitleArea = new Rectangle(0, 0, panelCol2);
			moveTitleArea.setStroke(borderCol);
			moveTitleArea.setViewOrder(-2);
			
			movesTitle = new Text("Moves");
			movesTitle.setFill(textCol);
			movesTitle.setTextOrigin(VPos.CENTER);
			movesTitle.setViewOrder(-3);
			
			moveNumbers = new Text[250];
			
			root.getChildren().addAll(panelL, topCapturedArea, bottomCapturedArea, materialAdvantage, panelR, playArea, boardBorder, popupArea, resultText1, resultText2, moveArea, moveAreaBorder, gameBorder, moveTitleArea, movesTitle);
			
			bottomCoordinates = new Text[8];
			createCoordinates(bottomCoordinates, true);
			leftCoordinates = new Text[8];
			createCoordinates(leftCoordinates, false);
			if (coordinatesOutsideBoard) {
				topCoordinates = new Text[8];
				createCoordinates(topCoordinates, true);
				rightCoordinates = new Text[8];
				createCoordinates(rightCoordinates, false);
			}
		}
		
		void resize() {
			panelL.relocate(gameStartX, gameStartY);
			panelL.setWidth(panelWidth);
			panelL.setHeight(gameHeight);
			
			topCapturedArea.relocate(gameStartX + (panelWidth - capturedAreaWidth) / 2, topCapturedStartY - capturedAreaPadding);
			topCapturedArea.setWidth(capturedAreaWidth);
			topCapturedArea.setHeight(capturedAreaHeight);
			topCapturedArea.setArcWidth(capturedAreaHeight / 3);
			topCapturedArea.setArcHeight(capturedAreaHeight / 3);
			topCapturedArea.setStrokeWidth(strokeWidth);
			
			bottomCapturedArea.relocate(gameStartX + (panelWidth - capturedAreaWidth) / 2, bottomCapturedStartY - capturedAreaPadding);
			bottomCapturedArea.setWidth(capturedAreaWidth);
			bottomCapturedArea.setHeight(capturedAreaHeight);
			bottomCapturedArea.setArcWidth(capturedAreaHeight / 3);
			bottomCapturedArea.setArcHeight(capturedAreaHeight / 3);
			bottomCapturedArea.setStrokeWidth(strokeWidth);
			
			updateCapturedAreas();
			
			if (setup.timeSecs > 0) {
				clock.setWidth(clockWidth);
				clock.setHeight(clockHeight);
				clock.relocate(clockStartX, clockStartY);
				clock.setArcWidth(clockHeight / 4);
				clock.setArcHeight(clockHeight / 4);
				
				clockTopBox.setWidth(clockWidth + strokeWidth * 4);
				clockTopBox.setHeight(clockHeight + strokeWidth * 2);
				clockTopBox.relocate(clockStartX - strokeWidth * 2, clockStartY - strokeWidth * 2);
				clockBottomBox.setWidth(clockWidth + strokeWidth * 4);
				clockBottomBox.setHeight(clockHeight + strokeWidth * 2);
				clockBottomBox.relocate(clockStartX - strokeWidth * 2, clockStartY + clockHeight / 2);
				
				if (root.getChildren().contains(clockTop)) root.getChildren().remove(clockTop);
				clockTop = Shape.intersect(clock, clockTopBox);
				clockTop.setStroke(borderCol);
				clockTop.setStrokeWidth(strokeWidth);
				root.getChildren().add(clockTop);
				if (root.getChildren().contains(clockBottom)) root.getChildren().remove(clockBottom);
				clockBottom = Shape.intersect(clock, clockBottomBox);
				clockBottom.setStroke(borderCol);
				clockBottom.setStrokeWidth(strokeWidth);
				root.getChildren().add(clockBottom);
				if (board.flipped) {
					clockTop.setFill(Color.WHITE);
					clockBottom.setFill(Color.BLACK);
				} else {
					clockTop.setFill(Color.BLACK);
					clockBottom.setFill(Color.WHITE);
				}
				
				whiteTime.setFont(Font.font("Arial Rounded MT Bold", clockHeight / 4));
				whiteTime.setX(clockStartX + (clockWidth - whiteTime.getLayoutBounds().getWidth()) / 2);
				blackTime.setFont(Font.font("Arial Rounded MT Bold", clockHeight / 4));
				blackTime.setX(clockStartX + (clockWidth - blackTime.getLayoutBounds().getWidth()) / 2);
				if (board.flipped) {
					whiteTime.setY(clockStartY + clockHeight / 4);
					blackTime.setY(clockStartY + clockHeight * 0.75);
				} else {
					whiteTime.setY(clockStartY + clockHeight * 0.75);
					blackTime.setY(clockStartY + clockHeight / 4);
				}
			}
			
			panelR.relocate(panelRStartX, gameStartY);
			panelR.setWidth(panelWidth);
			panelR.setHeight(gameHeight);
			
			playArea.relocate(playAreaStartX, gameStartY);
			playArea.setWidth(gameHeight);
			playArea.setHeight(gameHeight);
			playArea.setStrokeWidth(strokeWidth);
			boardBorder.relocate(boardStartX, boardStartY);
			boardBorder.setWidth(boardSize);
			boardBorder.setHeight(boardSize);
			boardBorder.setStrokeWidth(strokeWidth);

			resizeCoordinates(bottomCoordinates);
			repositionCoordinates(bottomCoordinates, true);
			resizeCoordinates(leftCoordinates);
			repositionCoordinates(leftCoordinates, false);
			if (coordinatesOutsideBoard) {
				resizeCoordinates(topCoordinates);
				repositionCoordinates(topCoordinates, true);
				resizeCoordinates(rightCoordinates);
				repositionCoordinates(rightCoordinates, false);
				for (Text coordinate : bottomCoordinates) coordinate.setY(gameStartY + gameHeight - boardMarginSize / 2);
				for (Text coordinate : leftCoordinates) coordinate.setX(gameStartX + panelWidth + boardMarginSize / 2 - coordinate.getLayoutBounds().getWidth() / 2);
				for (Text coordinate : topCoordinates) coordinate.setY(gameStartY + boardMarginSize / 2);
				for (Text coordinate : rightCoordinates) coordinate.setX(gameStartX + gameWidth - panelWidth - boardMarginSize / 2 - coordinate.getLayoutBounds().getWidth() / 2);
			} else {
				for (Text coordinate : bottomCoordinates) coordinate.setY(gameStartY + gameHeight - boardMarginSize * 1.35);
				for (Text coordinate : leftCoordinates) coordinate.setX(gameStartX + panelWidth + boardMarginSize + leftCoordinates[1].getLayoutBounds().getWidth() / 2);
			}
			
			popupArea.relocate(popupStartX, popupStartY);
			popupArea.setWidth(popupWidth);
			popupArea.setHeight(popupHeight);
			popupArea.setArcWidth(popupHeight / 5);
			popupArea.setArcHeight(popupHeight / 5);
			popupArea.setStrokeWidth(strokeWidth);
			popupAreaShadow.setOffsetX(gameHeight / 216);
			popupAreaShadow.setOffsetY(gameHeight / 216);
			popupAreaShadow.setRadius(gameHeight / 54);
			resizePopupText();
			
			gameBorder.relocate(gameStartX, gameStartY);
			gameBorder.setWidth(gameWidth);
			gameBorder.setHeight(gameHeight);
			gameBorder.setStrokeWidth(strokeWidth);
			
			moveArea.setWidth(moveAreaWidth);
			moveArea.setHeight(moveAreaHeight);
			moveArea.relocate(moveAreaStartX, moveAreaStartY);
			moveArea.setArcWidth(moveAreaWidth / 5);
			moveArea.setArcHeight(moveAreaWidth / 5);
			moveArea.setStrokeWidth(strokeWidth);
			
			moveAreaBorder.setWidth(moveAreaWidth);
			moveAreaBorder.setHeight(moveAreaHeight);
			moveAreaBorder.relocate(moveAreaStartX, moveAreaStartY);
			moveAreaBorder.setArcWidth(moveAreaWidth / 5);
			moveAreaBorder.setArcHeight(moveAreaWidth / 5);
			moveAreaBorder.setStrokeWidth(strokeWidth);
			
			moveTitleArea.setWidth(moveAreaWidth);
			moveTitleArea.setHeight(moveButtonHeight);
			moveTitleArea.relocate(moveAreaStartX, moveAreaStartY);
			moveTitleArea.setStrokeWidth(strokeWidth);
			
			movesTitle.setFont(Font.font("Arial Rounded MT Bold", moveButtonHeight * 0.7));
			movesTitle.setX(moveAreaStartX + (moveAreaWidth - movesTitle.getLayoutBounds().getWidth()) / 2);
			movesTitle.setY(moveAreaStartY + moveButtonHeight / 2);
		}
		
		void createCoordinates(Text[] coordinates, boolean horizontal) {
			for (int i = 0; i < 8; i++) {
				if (horizontal) coordinates[i] = new Text(Character.toString('a' + i));
				else coordinates[i] = new Text(Integer.toString(i + 1));
				coordinates[i].setTextOrigin(VPos.CENTER);
				if (coordinatesOutsideBoard) coordinates[i].setFill(boardMarginTextCol);
				else {
					if (i % 2 == 0) coordinates[i].setFill(lightSquareCol);
					else coordinates[i].setFill(darkSquareCol);
				}
				coordinates[i].setViewOrder(1);
				root.getChildren().add(coordinates[i]);
			}
		}
		
		void resizeCoordinates(Text[] coordinates) {
			for (Text coordinate : coordinates) coordinate.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize / 2));
		}
		
		void repositionCoordinates(Text[] coordinates, boolean horizontal)  {
			if (horizontal) {
				for (int x = 0; x < 8; x++) {
					int number;
					if (board.flipped) {
						if (!coordinatesOutsideBoard) {
							if (x % 2 == 0) coordinates[x].setFill(darkSquareCol);
							else coordinates[x].setFill(lightSquareCol);
						}
						number = 7 - x;
					} else {
						if (!coordinatesOutsideBoard) {
							if (x % 2 == 0) coordinates[x].setFill(lightSquareCol);
							else coordinates[x].setFill(darkSquareCol);
						}
						number = x;
					}
					if (coordinatesOutsideBoard) coordinates[x].setX(boardStartX + squareSize * number + squareSize / 2 - coordinates[x].getLayoutBounds().getWidth() / 2);
					else coordinates[x].setX(boardStartX + squareSize * (number + 1) - coordinates[1].getLayoutBounds().getWidth() * 1.5);
				}
			} else {
				for (int y = 0; y < 8; y++) {
					int number;
					if (board.flipped) {
						if (!coordinatesOutsideBoard) {
							if (y % 2 == 0) coordinates[y].setFill(darkSquareCol);
							else coordinates[y].setFill(lightSquareCol);
						}
						number = y;
					} else {
						if (!coordinatesOutsideBoard) {
							if (y % 2 == 0) coordinates[y].setFill(lightSquareCol);
							else coordinates[y].setFill(darkSquareCol);
						}
						number = 7 - y;
					}
					if (coordinatesOutsideBoard) coordinates[y].setY(boardStartY + squareSize * number + squareSize / 2);
					else coordinates[y].setY(boardStartY + squareSize * number + boardMarginSize * 0.35);
				}
			}
		}
		
		void resizePopupText() {
			resultText1.setFont(Font.font("Arial Rounded MT Bold", popupHeight / 4));
			resultText1.setX(popupStartX + popupWidth / 2 - resultText1.getLayoutBounds().getWidth() / 2);
			resultText1.setY(popupStartY + popupHeight / 2 - squareSize * 0.6);
			resultText2.setFont(Font.font("Arial Rounded MT Bold", popupHeight / 8));
			resultText2.setX(popupStartX + popupWidth / 2 - resultText2.getLayoutBounds().getWidth() / 2);
			resultText2.setY(popupStartY + popupHeight / 2 - squareSize / 8 + squareSize * 0.2);
		}
		
		void updateCapturedAreas() {
			int materialSum = 0;
			for (int piece : board.gameState.board) {
				if (board.gameState.type(piece) != NONE) {
					if (board.gameState.isWhite(piece)) {
						switch (board.gameState.type(piece)) {
						case PAWN:
							materialSum++;
							break;
						case KNIGHT:
							materialSum += 3;
							break;
						case BISHOP:
							materialSum += 3;
							break;
						case ROOK:
							materialSum += 5;
							break;
						case QUEEN:
							materialSum += 9;
						}
					} else {
						switch (board.gameState.type(piece)) {
						case PAWN:
							materialSum--;
							break;
						case KNIGHT:
							materialSum -= 3;
							break;
						case BISHOP:
							materialSum -= 3;
							break;
						case ROOK:
							materialSum -= 5;
							break;
						case QUEEN:
							materialSum -= 9;
						}
					}
				}
			}
			if (materialSum == 0) {
				materialAdvantage.setVisible(false);
			} else {
				materialAdvantage.setVisible(true);
				materialAdvantage.setText("+" + Math.abs(materialSum));
			}
			
			if (Math.abs(materialSum) > 9) {
				materialAdvantage.setFont(Font.font("Arial Rounded MT Bold", capturedPieceSize / 3));
			} else {
				materialAdvantage.setFont(Font.font("Arial Rounded MT Bold", capturedPieceSize / 2));
			}
			materialAdvantage.setX(capturedStartX + capturedBoxWidth - materialAdvantage.getLayoutBounds().getWidth() - capturedPieceSize * 0.2);
			if (board.flipped && materialSum > 0 || !board.flipped && materialSum < 0) {
				materialAdvantage.setY(topCapturedStartY + (capturedRows - 0.5) * capturedBoxHeight / capturedRows);
			} else {
				materialAdvantage.setY(bottomCapturedStartY + (capturedRows - 0.5) * capturedBoxHeight / capturedRows);
			}
		}
		
		void updateTimeText(boolean whiteTime) {
			String text = "";
			int minutes;
			int seconds;
			if (whiteTime) {
				if (setup.timeSecs >= 60) {
					minutes = board.whiteTimeMillis / 60000;
					if (setup.timeSecs >= 600 && minutes < 10) text += "0";
					text += minutes + ":";
				}
				seconds = board.whiteTimeMillis % 60000 / 1000;
				if (seconds < 10) text += "0";
				text += seconds + "." + board.whiteTimeMillis % 1000 / 100;
				graphics.whiteTime.setText(text);
			} else {
				if (setup.timeSecs >= 60) {
					minutes = board.blackTimeMillis / 60000;
					if (setup.timeSecs >= 600 && minutes < 10) text += "0";
					text += minutes + ":";
				}
				seconds = board.blackTimeMillis % 60000 / 1000;
				if (seconds < 10) text += "0";
				text += seconds + "." + board.blackTimeMillis % 1000 / 100;
				graphics.blackTime.setText(text);
			}
		}
	}
	
	class Board {  // For functional elements directly related to pieces on the board
		GameState gameState;
		int stateType;  // E.g. NORMAL, CHECK, CHECKMATE...
		GameState[] history;
		Move[] moveHistory;
		BoardSquare[] grid;
		Sprite[] pieces;
		Sprite[] captureHistory;
		int activeSpriteId;
		Move[] candidateMoves;
		boolean secondClick;
		PromotionOption[] options;
		boolean midPromotion;
		boolean flipped;
		boolean navigating;
		AI ai;
		Task<Move> aiTask;
		Thread aiThread;
		boolean aiThinking;
		boolean endAnimation;
		FillTransition flash;
		boolean navBackwardCued;
		boolean navToStartCued;
		boolean undoMoveCued;
		Timeline whiteTimeline;
		int whiteTimeMillis;
		Timeline blackTimeline;
		int blackTimeMillis;
		AI hintAi;
		Task<Move> hintTask;
		Thread hintThread;
		Move hint;
		int hintStage;
		
		void init() {
			if (setup.singlePlayer) {
				gameState = Utilities.fenToBoard(setup.fenAI);
				if (gameState == null) {
					setup.fenAI = "";
					gameState = new GameState();
					gameState.init();
				}
			} else {
				gameState = Utilities.fenToBoard(setup.fenOTB);
				if (gameState == null) {
					setup.fenOTB = "";
					gameState = new GameState();
					gameState.init();
				}
			}
			firstStateIndex = gameState.halfMoves;
			
			int wCapturablePieces = 0;
			int bCapturablePieces = 0;
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					int type = gameState.type(x, y);
					if (type != NONE && type != KING) {
						if (gameState.isWhite(x, y)) wCapturablePieces++;
						else bCapturablePieces++;
					}
				}
			}
			int maxCapturedPieces = Math.max(wCapturablePieces, bCapturablePieces);
			if (maxCapturedPieces < 16) {
				capturedRows = 2;
				capturedColumns = 8;
			} else {
				capturedRows = 3;
				capturedColumns = 11;
			}
			
			history = new GameState[500];
			history[firstStateIndex] = new GameState(gameState);
			
			moveHistory = new Move[500];
			
			grid = new BoardSquare[64];
			for (int i = 0; i < 64; i++) {
				grid[i] = new BoardSquare(Game.this, i);
				grid[i].resetColour();
				grid[i].setViewOrder(2);
				root.getChildren().add(grid[i]);
			}
			
			pieces = new Sprite[64];
			for (int i = 0; i < 64; i++) pieces[i] = new Sprite(Game.this, i);
			captureHistory = new Sprite[500];
			initSprites();
			activeSpriteId = -1;  // Anything negative means there is not an active sprite
			candidateMoves = null;
			secondClick = false;  // Related to toggling sprite activity by clicking twice
			
			options = new PromotionOption[4];
			options[0] = new PromotionOption(Game.this, QUEEN);
			options[1] = new PromotionOption(Game.this, ROOK);
			options[2] = new PromotionOption(Game.this, KNIGHT);
			options[3] = new PromotionOption(Game.this, BISHOP);
			for(PromotionOption option : options) {
				option.setVisible(false);
				option.imageView.setVisible(false);
				root.getChildren().addAll(option, option.imageView);
			}
			midPromotion = false;  // Interactivity of other pieces is frozen whilst the user is selecting what to promote to (midPromotion = true)
			flipped = false;
			navigating = false;
			
			ai = new AI(setup.aiDepth, !playerIsWhite, history);
			
			aiThinking = false;
			endAnimation = false;
			navBackwardCued = false;
			navToStartCued = false;
			undoMoveCued = false;
			
			if (setup.timeSecs > 0) {
				whiteTimeMillis = setup.timeSecs * 1000;
				graphics.updateTimeText(true);
				whiteTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
					whiteTimeMillis -= 100;
					graphics.updateTimeText(true);
					if (whiteTimeMillis == 0) {
						stateType = WHITETIMEOUT;
						timeout();
					}
				}));
				whiteTimeline.setCycleCount(Animation.INDEFINITE);
				
				blackTimeMillis = setup.timeSecs * 1000;
				graphics.updateTimeText(false);
				blackTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
					blackTimeMillis -= 100;
					graphics.updateTimeText(false);
					if (blackTimeMillis == 0) {
						stateType = BLACKTIMEOUT;
						timeout();
					}
				}));
				blackTimeline.setCycleCount(Animation.INDEFINITE);
			}
			
			hintAi = new AI(4, playerIsWhite, history);
			hintStage = HINTOFF;
		}
		
		void resize() {
			resizeGrid();
			resizeSprites();
			if (midPromotion) updateOptions();
			for (BoardSquare square : grid) square.setStrokeWidth(squareBorderWidth);
		}
		
		void resizeGrid() {
			for (int i = 0; i < 64; i++) {  // Loop through all the graphical board squares
				int x;
				int y;
				if (flipped) {
					x = 7 - (i % 8);
					y = i / 8;
				} else {
					x = i % 8;
					y = 7 - (i / 8);
				}
				// Relocate and scale them
				grid[i].relocate(x * squareSize + boardStartX, y * squareSize + boardStartY);
				grid[i].setWidth(squareSize);
				grid[i].setHeight(squareSize);
			}
		}
		
		void initSprites() {
			for (Sprite sprite : pieces) root.getChildren().remove(sprite);
			
			for (int i = 0; i < 64; i++) {
				int piece = gameState.board[i];
				int type = gameState.type(piece);
				if (type != NONE) {
					Sprite sprite = pieces[gameState.getSprite(piece)];
					sprite.type = type;
					if (gameState.isWhite(piece)) {
						sprite.isWhite = true;
						
						switch(type) {
						case PAWN:
							sprite.setImage(graphics.pawnWpng);
							break;
						case KNIGHT:
							sprite.setImage(graphics.knightWpng);
							break;
						case BISHOP:
							sprite.setImage(graphics.bishopWpng);
							break;
						case ROOK:
							sprite.setImage(graphics.rookWpng);
							break;
						case QUEEN:
							sprite.setImage(graphics.queenWpng);
							break;
						case KING:
							sprite.setImage(graphics.kingWpng);
							break;
						}
					} else {
						sprite.isWhite = false;
						
						switch(type) {
						case PAWN:
							sprite.setImage(graphics.pawnBpng);
							break;
						case KNIGHT:
							sprite.setImage(graphics.knightBpng);
							break;
						case BISHOP:
							sprite.setImage(graphics.bishopBpng);
							break;
						case ROOK:
							sprite.setImage(graphics.rookBpng);
							break;
						case QUEEN:
							sprite.setImage(graphics.queenBpng);
							break;
						case KING:
							sprite.setImage(graphics.kingBpng);
							break;
						}
					}
					
					sprite.captured = false;
					
					sprite.setPos(i % 8, i / 8);
					sprite.setVisible(true);
					sprite.setViewOrder(0);
					root.getChildren().add(sprite);
					sprite.move();
					
					Utilities.installSpriteTooltip(sprite);
				}
			}
			
			for (int i = 0; i <= gameState.halfMoves; i++) {
				Sprite capturedSprite = captureHistory[i];
				if (capturedSprite != null) {
					capturedSprite.captured = true;
					capturedSprite.setViewOrder(- (float) i / 500);
					root.getChildren().add(capturedSprite);
					capturedSprite.move();
					
					Utilities.installSpriteTooltip(capturedSprite);
				}
			}
			
			resizeSprites();
		}
		
		void resizeSprites() {
			for (Sprite sprite : pieces) {  // Loop though every sprite
				if (sprite.captured) {
					sprite.setFitWidth(capturedPieceSize);
					sprite.setFitHeight(capturedPieceSize);
				} else {
					sprite.setFitWidth(squareSize);
					sprite.setFitHeight(squareSize);
				}
				sprite.move();
			}
		}
		
		void updateOptions() {
			int baseIndex = options[0].moveIndex;
			for (PromotionOption option : options) option.update(baseIndex);
		}
		
		void activate(Sprite sprite) {
			deactivate();
			
			activeSpriteId = sprite.idNum;
			candidateMoves = gameState.genMoves(sprite.pos);
			
			if (sprite.pos.x % 2 == sprite.pos.y % 2) grid[sprite.pos.y * 8 + sprite.pos.x].setFill(darkSquareSelectionCol);
			else grid[sprite.pos.y * 8 + sprite.pos.x].setFill(lightSquareSelectionCol);
			
			if (showLegalMoves) {
				for (Move move : candidateMoves) {
					if (move != null) {  // Highlight all potential target squares
						if (move.isCapture) {
							if (move.endSq.x % 2 == move.endSq.y % 2) grid[move.endSq.y * 8 + move.endSq.x].setStroke(darkSquareCaptureCol);
							else grid[move.endSq.y * 8 + move.endSq.x].setStroke(lightSquareCaptureCol);
						} else {
							if (move.endSq.x % 2 == move.endSq.y % 2) grid[move.endSq.y * 8 + move.endSq.x].setStroke(darkSquareSelectionCol);
							else grid[move.endSq.y * 8 + move.endSq.x].setStroke(lightSquareSelectionCol);
						}
						grid[move.endSq.y * 8 + move.endSq.x].setStrokeWidth(squareBorderWidth);
						grid[move.endSq.y * 8 + move.endSq.x].setStrokeType(StrokeType.INSIDE);
					} else break;
				}
			}
		}
		
		void deactivate() {
			activeSpriteId = -1;
			candidateMoves = null;
			for (BoardSquare square : grid) {  // Resetting the colour of the squares, candidate moves no longer displayed
				square.setStroke(null);
			}
			updateGridFill();
			hintStage = HINTOFF;
			Utilities.installTooltip(buttons.hint, "Show move hint");
		}
		
		void updateGridFill() {  // Updates the colour of all board squares
			for (BoardSquare square : grid) square.resetColour();
		}
		
		void doMove(Move move, boolean animate) {
			if (setup.timeSecs > 0 && gameState.halfMoves > 1) {
				if (gameState.whiteToMove) {
					whiteTimeline.pause();
					graphics.whiteTime.setFill(Color.GREY);
				} else {
					blackTimeline.pause();
					graphics.blackTime.setFill(Color.GREY);
				}
			}
			
			buttons.hint.deactivate();
			
			if (move.isCapture && move.finalType == move.pieceType) {  // Sprite of captured piece is removed
				captureSprite(move);
			} else if (move.isCastling) {  // The rook moves automatically in castling
				if (move.endSq.x == 6) {
					Sprite rook = pieces[gameState.getSprite(7, move.endSq.y)];
					rook.setPos(5, move.startSq.y);  // Kingside castling
					rook.moveAnimated(false, false);
					Utilities.installSpriteTooltip(rook);
				} else {
					Sprite rook = pieces[gameState.getSprite(0, move.endSq.y)];
					rook.setPos(3, move.startSq.y);  // Queenside castling
					rook.moveAnimated(false, false);
					Utilities.installSpriteTooltip(rook);
				}
			}
			
			Sprite sprite = pieces[gameState.getSprite(move.startSq)];
			sprite.setPos(move.endSq);  // Update the sprite's internally stored board position
			
			Utilities.installSpriteTooltip(sprite);
			
			if (move.finalType != move.pieceType) {  // Promotion move
				if (gameState.whiteToMove) {
					switch (move.finalType) {
					case QUEEN:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.queenWpng);
						break;
					case ROOK:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.rookWpng);
						break;
					case KNIGHT:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.knightWpng);
						break;
					case BISHOP:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.bishopWpng);
						break;
					}
				} else {
					switch (move.finalType) {
					case QUEEN:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.queenBpng);
						break;
					case ROOK:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.rookBpng);
						break;
					case KNIGHT:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.knightBpng);
						break;
					case BISHOP:
						pieces[gameState.getSprite(move.startSq)].setImage(graphics.bishopBpng);
						break;
					}
				}
			}
			
			secondClick = false;
			moveHistory[gameState.halfMoves] = move;
			gameState.makeMove(move);
			deactivate();
			graphics.updateCapturedAreas();
			buttons.draw.text.setText("DRAW");
			buttons.draw.repositionText();
			
			history[gameState.halfMoves] = new GameState(gameState);
			
			displayMove();

			stateType = gameState.updateState(history);
			
			if (stateType != NORMAL && stateType != CHECK) {
				ended = true;
				buttons.wResign.deactivate();
				buttons.bResign.deactivate();
				buttons.draw.deactivate();
				
				if (setup.timeSecs > 0) {
					buttons.giveWhiteTime.deactivate();
					buttons.giveBlackTime.deactivate();
					whiteTimeline.stop();
					blackTimeline.stop();
					if (gameState.whiteToMove) graphics.blackTime.setFill(Color.WHITE);
					else graphics.whiteTime.setFill(Color.BLACK);
				}
				if (stateType == CHECKMATE) {
					Vector pos;
					if (gameState.whiteToMove) pos = gameState.wKingPos;
					else pos = gameState.bKingPos;
					
					Color baseColour;
					Color flashColour;
					if (pos.x % 2 == pos.y % 2) {
						baseColour = darkSquareCol;
						flashColour = darkSquareCheckCol;
					} else {
						baseColour = lightSquareCol;
						flashColour = lightSquareCheckCol;
					}
					
					flash = new FillTransition(Duration.seconds(0.3), grid[pos.y * 8 + pos.x], baseColour, flashColour);
					flash.setCycleCount(13);
					flash.setAutoReverse(true);
					flash.setOnFinished(event -> {
						showPopup();
						endAnimation = false;
						buttons.navBackward.activate();
						if (setup.timeSecs > 0) activateMoves();
						if (navBackwardCued) {
							navBackward();  // Hide the popup
							navBackward();  // Go to previous move
							navBackwardCued = false;
						} else if (navToStartCued) {
							navToStart();
							navToStartCued = false;
						} else if (undoMoveCued) {
							undoMove();
							undoMoveCued = false;
						}
					});
					flash.play();
					endAnimation = true;
				} else {
					showPopup();
					if (setup.timeSecs > 0) activateMoves();
				}
			} else generateHint();
			
			sprite.setViewOrder(-4);  // Draw above the other pieces 
			if (animate) sprite.moveAnimated(false, move.isCastling);
			else sprite.moveAnimated(true, move.isCastling);
			
			if (setup.timeSecs > 0 && gameState.halfMoves > 1 && !ended) {
				if (gameState.whiteToMove) {
					if (gameState.halfMoves != 2) {
						blackTimeMillis += setup.incrementSecs * 1000;
						graphics.updateTimeText(false);
					}
					whiteTimeline.play();
					graphics.whiteTime.setFill(Color.BLACK);
				} else {
					whiteTimeMillis += setup.incrementSecs * 1000;
					graphics.updateTimeText(true);
					blackTimeline.play();
					graphics.blackTime.setFill(Color.WHITE);
				}
			}
			
			if ((setup.timeSecs == 0 || ended) && !endAnimation) buttons.navBackward.activate();
			if (setup.timeSecs == 0 && allowTakingBackMoves) buttons.undoMove.activate();
		}
		
		void showOptions(int index) {
			midPromotion = true;
			pieces[activeSpriteId].setVisible(false);  // Hide the active pawn
			for (BoardSquare square : grid) square.setStroke(null);  // Candidate moves no longer displayed
			updateGridFill();
			// Remove the sprite of a captured piece
			if (candidateMoves[index].isCapture) captureSprite(candidateMoves[index]);
			for(PromotionOption option : options) {  // Display the four options
				option.update(index);
				option.setVisible(true);
				option.imageView.setVisible(true);
			}
		}
		
		void cancelPromotion() {
			deactivate();
			board.midPromotion = false;
			for(PromotionOption option : options) {  // Hide the four options
				option.setVisible(false);
				option.imageView.setVisible(false);
			}
			graphics.updateCapturedAreas();
			captureHistory[gameState.halfMoves + 1] = null;
			board.initSprites();
		}
		
		void doPromotion(Move move) {
			for(PromotionOption option : options) {  // Hide the four options
				option.setVisible(false);
				option.imageView.setVisible(false);
			}
			pieces[activeSpriteId].setVisible(true);  // Show the piece, now promoted
			doMove(move, false);
			midPromotion = false;
		}
		
		void startAIThinking() {
			if (setup.singlePlayer && gameState.whiteToMove == ai.playingAsWhite && !ended) {
				aiTask = new Task<Move> () {
					@Override
					protected Move call() throws Exception {
						return ai.getBestMove(gameState.halfMoves);
					}
				};
				
				aiTask.setOnSucceeded(event -> {
					aiThinking = false;
					if (ai.cancel) {
						ai.cancel = false;
					} else {
						Move move = aiTask.getValue();
						if (move.isCapture && move.finalType != move.pieceType) captureSprite(move);
						doMove(move, true);
					}
				});
				
				aiThread = new Thread(aiTask);
				aiThread.setDaemon(true);
				
				aiThinking = true;
				aiThread.start();
			}
		}
		
		void generateHint() {
			if (allowMoveSuggestions && (!setup.singlePlayer || setup.singlePlayer && gameState.whiteToMove == playerIsWhite) && !ended) {
				hintTask = new Task<Move> () {
					@Override
					protected Move call() throws Exception {
						hintAi.playingAsWhite = gameState.whiteToMove;
						return hintAi.getBestMove(gameState.halfMoves);
					}
				};
				
				hintTask.setOnSucceeded(event -> {
					if (!ended) {
						hint = hintTask.getValue();
						if (!navigating) buttons.hint.activate();
					}
				});
				
				hintThread = new Thread(hintTask);
				hintThread.setDaemon(true);
				
				hintThread.start();
			}
		}
		
		void showHint() {
			if (allowMoveSuggestions) {
				final int hintStageCopy = hintStage;
	
				board.deactivate();
				
				if (hintStageCopy == HINTOFF || hintStageCopy == HINTPARTIAL) {
					if (hint.startSq.x % 2 == hint.startSq.y % 2) grid[hint.startSq.y * 8 + hint.startSq.x].setFill(darkSquareSelectionCol);
					else grid[hint.startSq.y * 8 + hint.startSq.x].setFill(lightSquareSelectionCol);
					hintStage = HINTPARTIAL;
					Utilities.installTooltip(buttons.hint, "Show move suggestion");
				}
				if (hintStageCopy == HINTPARTIAL) {
					if (hint.endSq.x % 2 == hint.endSq.y % 2) {
						if (gameState.occupied(hint.endSq)) grid[hint.endSq.y * 8 + hint.endSq.x].setFill(darkSquareCaptureCol);
						else grid[hint.endSq.y * 8 + hint.endSq.x].setFill(darkSquareSelectionCol);
					} else {
						if (gameState.occupied(hint.endSq)) grid[hint.endSq.y * 8 + hint.endSq.x].setFill(lightSquareCaptureCol);
						else grid[hint.endSq.y * 8 + hint.endSq.x].setFill(lightSquareSelectionCol);
					}
					hintStage = HINTFULL;
					Utilities.installTooltip(buttons.hint, "Hide move suggestion");
				}
			}
		}
		
		void captureSprite(Move move) {
			Sprite capturedSprite;
			if (move.isEnPassant) {
				capturedSprite = pieces[gameState.getSprite(move.endSq.x, move.startSq.y)];
			} else {
				capturedSprite = pieces[gameState.getSprite(move.endSq)];
			}
			
			capturedSprite.captured = true;
			captureHistory[gameState.halfMoves + 1] = capturedSprite;
			Utilities.installSpriteTooltip(capturedSprite);
			
			if (capturedSprite.isWhite) {
				int nextCapturedSlot = 0;
				for (int i = 0; i < gameState.halfMoves; i++) {
					if (captureHistory[i] != null && captureHistory[i].isWhite) nextCapturedSlot++;
					capturedSprite.setViewOrder(- (float) i / 500);
				}
				capturedSprite.setCapturedPos(nextCapturedSlot % capturedColumns, nextCapturedSlot / capturedColumns);
			} else {
				int nextCapturedSlot = 0;
				for (int i = 0; i < gameState.halfMoves; i++) {
					if (captureHistory[i] != null && !captureHistory[i].isWhite) nextCapturedSlot++;
					capturedSprite.setViewOrder(- (float) i / 500);
				}
				capturedSprite.setCapturedPos(nextCapturedSlot % capturedColumns, nextCapturedSlot / capturedColumns);
			}
			capturedSprite.setFitWidth(capturedPieceSize);
			capturedSprite.setFitHeight(capturedPieceSize);
			capturedSprite.move();
		}
		
		void spriteClicked(Sprite sprite) {
			if (midPromotion) cancelPromotion();
			else {
				if (sprite.idNum == activeSpriteId) secondClick = true;
				else if (activeSpriteId < 0) {
					if (sprite.isWhite == gameState.whiteToMove) {
						secondClick = false;
						activate(sprite);
					}
				} else {
					if (sprite.isWhite == gameState.whiteToMove) {
						// Clicked another piece of turning player's colour, change the active sprite
						deactivate();
						secondClick = false;
						activate(sprite);
					} else {
						// Clicked piece of opposite colour, maybe a capture move
						int moveIndex = 0;
						for (Move move : candidateMoves) {
							if (move != null) {
								if (move.endSq.equals(sprite.pos)) {
									if (move.finalType != move.pieceType) {  // Promotion, must choose what to promote to
										showOptions(moveIndex);
									} else doMove(move, true);
									break;  // The move is legal, don't need to look through any more
								}
								moveIndex++;
							} else deactivate();  // Didn't try to make a legal move, deactivate the sprite
						}
					}
				}
			}
		}
		
		void squareClicked(BoardSquare square) {
			if (midPromotion) cancelPromotion();
			else {
				if (activeSpriteId >= 0) {
					int moveIndex = 0;
					for (Move move : candidateMoves) {
						if (move != null) {
							if (move.endSq.equals(square.pos)) {
								if (move.finalType != move.pieceType) {  // Promotion, must choose what to promote to
									showOptions(moveIndex);
								} else doMove(move, true);
								break;  // The move is legal, don't need to look through any more
							}
							moveIndex++;
						} else deactivate();  // Didn't try to make a legal move, deactivate the sprite
					}
				}
			}
		}
	}
	
	class Buttons {  // For functional elements off the board
		Button wResign;
		Button bResign;
		Button draw;
		Button rematch;
		Button newGame;
		Button reset;
		Button exit;
		Button flip;
		Button copyFen;
		Button hint;
		Button navBackward;
		Button navForward;
		Button undoMove;
		Button giveWhiteTime;
		Button giveBlackTime;
		Button[][] moves;
		Button copyPgn;
		Button hidePopup;
		
		void init() {
			wResign = new Button(Color.WHITE, Color.web("cccccc"), "RESIGN", Color.BLACK, Color.GREY);
			wResign.setOnMouseClicked(event -> {
				if (!ended && !board.navigating && event.getButton() == MouseButton.PRIMARY) {
					ended = true;
					wResign.deactivate();
					bResign.deactivate();
					draw.deactivate();
					if (setup.timeSecs > 0) activateMoves();
					board.ai.cancel = true;
					board.stateType = WHITERESIGNED;
					showPopup();
					if (board.midPromotion) board.cancelPromotion();
					board.deactivate();
					draw.text.setText("DRAW");
					draw.repositionText();
					navBackward.activate();
					hint.deactivate();
					Utilities.installTooltip(undoMove, "Take back resignation (backspace)");
					
					if (setup.timeSecs > 0) {
						board.whiteTimeline.stop();
						board.blackTimeline.stop();
						buttons.giveWhiteTime.deactivate();
						buttons.giveBlackTime.deactivate();
					} else if (allowTakingBackMoves) undoMove.activate();
				}
			});
			wResign.setStroke(borderCol);
			
			bResign = new Button(Color.BLACK, Color.web("333333"), "RESIGN", Color.WHITE, Color.GREY);
			bResign.setOnMouseClicked(event -> {
				if (!ended && !board.navigating && event.getButton() == MouseButton.PRIMARY) {
					ended = true;
					wResign.deactivate();
					bResign.deactivate();
					draw.deactivate();
					if (setup.timeSecs > 0) activateMoves();
					board.ai.cancel = true;
					board.stateType = BLACKRESIGNED;
					showPopup();
					if (board.midPromotion) board.cancelPromotion();
					board.deactivate();
					draw.text.setText("DRAW");
					draw.repositionText();
					navBackward.activate();
					hint.deactivate();
					Utilities.installTooltip(undoMove, "Take back resignation (backspace)");
					
					if (setup.timeSecs > 0) {
						board.whiteTimeline.stop();
						board.blackTimeline.stop();
						buttons.giveWhiteTime.deactivate();
						buttons.giveBlackTime.deactivate();
					} else if (allowTakingBackMoves) undoMove.activate();
				}
			});
			bResign.setStroke(borderCol);
			
			draw = new Button(Color.GREY, Color.web("666666"), "DRAW", Color.WHITE, Color.LIGHTGREY);
			draw.setOnMouseClicked(event -> {
				if (!ended && !board.navigating) {
					if (event.getButton() == MouseButton.PRIMARY) {
						if (draw.text.getText() == "DRAW") {
							draw.text.setText("CONFIRM DRAW");
							draw.repositionText();
						} else {
							draw.text.setText("DRAW");
							draw.repositionText();
							ended = true;
							wResign.deactivate();
							bResign.deactivate();
							draw.deactivate();
							board.deactivate();
							if (setup.timeSecs > 0) activateMoves();
							board.stateType = DRAWAGREED;
							showPopup();
							if (board.midPromotion) board.cancelPromotion();
							if (setup.timeSecs > 0) {
								board.whiteTimeline.stop();
								board.blackTimeline.stop();
								buttons.giveWhiteTime.deactivate();
								buttons.giveBlackTime.deactivate();
							} else if (allowTakingBackMoves) undoMove.activate();
							navBackward.activate();
							hint.deactivate();
							Utilities.installTooltip(undoMove, "Take back agreed draw (backspace)");
						}
					} else if (event.getButton() == MouseButton.SECONDARY) {
						draw.text.setText("DRAW");
						draw.repositionText();
					}
				}
			});
			draw.setStroke(borderCol);
			
			rematch = new Button(buttonCol, buttonHoverCol, "Rematch", buttonTextCol);
			rematch.setViewOrder(-6);
			rematch.text.setViewOrder(-6);
			rematch.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (setup.singlePlayer) {
						switch (setup.playerColour) {
						case WHITETOGGLE:
							setup.playerColour = BLACKTOGGLE;
							break;
						case BLACKTOGGLE:
							setup.playerColour = WHITETOGGLE;
							break;
						case RANDOM:
							if (playerIsWhite) setup.playerColour = BLACKTOGGLE;
							else setup.playerColour = WHITETOGGLE;
						}
					}
					Game.this.init(setup);
				}
			});
			rematch.setStroke(borderCol);
			
			newGame = new Button(buttonCol, buttonHoverCol, "New game", buttonTextCol);
			newGame.setViewOrder(-6);
			newGame.text.setViewOrder(-6);
			newGame.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) Main.loadEntry(setup);
			});
			newGame.setStroke(borderCol);
			
			reset = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "⭮", boardMarginTextCol);  // Alternative symbol: ⟳
			reset.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) reset();
			});
			
			exit = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "❌", boardMarginTextCol);  // Alternative symbol: ⟳
			exit.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) exit();
			});
			
			flip = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "⮁", boardMarginTextCol);  // Alternative symbol: 🗘
			flip.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) flip();
			});
			
			copyFen = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "⧉", boardMarginTextCol);
			copyFen.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					Clipboard clipboard = Clipboard.getSystemClipboard();
					ClipboardContent content = new ClipboardContent();
					content.putString(Utilities.boardToFen(board.gameState));
					clipboard.setContent(content);
				}
			});
			
			hint = new Button(buttonCol, buttonHoverCol, "💡", buttonTextCol, inactiveButtonTextCol);
			hint.setViewOrder(-2);
			hint.text.setViewOrder(-2);
			hint.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY && hint.active) board.showHint();
			});
			hint.deactivate();
			if (!setup.singlePlayer || playerIsWhite == board.gameState.whiteToMove) board.generateHint();
			hint.setStroke(borderCol);
			
			navBackward = new Button(buttonCol, buttonHoverCol, "◀", buttonTextCol, inactiveButtonTextCol);
			navBackward.setViewOrder(-2);
			navBackward.text.setViewOrder(-2);
			navBackward.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (event.isControlDown()) navToStart();
					else navBackward();
				}
			});
			navBackward.deactivate();
			navBackward.setStroke(borderCol);
			
			navForward = new Button(buttonCol, buttonHoverCol, "▶", buttonTextCol, inactiveButtonTextCol);
			navForward.setViewOrder(-2);
			navForward.text.setViewOrder(-2);
			navForward.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (event.isControlDown()) navToEnd();
					else navForward();
				}
			});
			navForward.deactivate();
			navForward.setStroke(borderCol);
			
			undoMove = new Button(buttonCol, buttonHoverCol, "⮌", buttonTextCol, inactiveButtonTextCol);
			undoMove.setViewOrder(-2);
			undoMove.text.setViewOrder(-2);
			undoMove.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) undoMove();
			});
			undoMove.deactivate();
			undoMove.setStroke(borderCol);
			
			copyPgn = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "⧉", Color.GREY);
			copyPgn.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					Clipboard clipboard = Clipboard.getSystemClipboard();
					ClipboardContent content = new ClipboardContent();
					content.putString(gameToPgn());
					clipboard.setContent(content);
				}
			});
			copyPgn.setViewOrder(-6);
			copyPgn.text.setViewOrder(-6);
			
			hidePopup = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "❌", Color.GREY);
			hidePopup.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) navBackward();
			});
			hidePopup.setViewOrder(-6);
			hidePopup.text.setViewOrder(-6);
			
			if (setup.timeSecs > 0) {
				giveWhiteTime = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "+", Color.GREY, Color.GREY);
				giveWhiteTime.setOnMouseClicked(event -> {
					if (!ended) {
						board.whiteTimeMillis += 15000;
						graphics.updateTimeText(true);
					}
				});
				giveWhiteTime.setViewOrder(-1);
				giveWhiteTime.text.setViewOrder(-1);
				
				giveBlackTime = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "+", Color.GREY, Color.GREY);
				giveBlackTime.setOnMouseClicked(event -> {
					if (!ended) {
						board.blackTimeMillis += 15000;
						graphics.updateTimeText(false);
					}
				});
				giveBlackTime.setViewOrder(-1);
				giveBlackTime.text.setViewOrder(-1);
				
				root.getChildren().addAll(giveWhiteTime, giveWhiteTime.text, giveBlackTime, giveBlackTime.text);
				Utilities.installTooltip(giveWhiteTime, "Give 15 extra seconds to White");
				Utilities.installTooltip(giveBlackTime, "Give 15 extra seconds to Black");
			}
			
			moves = new Button[250][2];
			
			root.getChildren().addAll(wResign, wResign.text, bResign, bResign.text, draw, draw.text, rematch, rematch.text, newGame, newGame.text, reset, reset.text, exit, exit.text, flip, flip.text, copyFen, copyFen.text, navBackward, navBackward.text, navForward, navForward.text, undoMove, undoMove.text, hint, hint.text, copyPgn, copyPgn.text, hidePopup, hidePopup.text);
			
			Utilities.installTooltip(wResign, "Resign (white)");
			Utilities.installTooltip(bResign, "Resign (black)");
			Utilities.installTooltip(draw, "Agree to draw");
			Utilities.installTooltip(rematch, "Start a new game with the same settings");
			Utilities.installTooltip(newGame, "Create a new game with different settings");
			Utilities.installTooltip(reset, "Restart game (R)");
			Utilities.installTooltip(exit, "Abandon game");
			Utilities.installTooltip(flip, "Flip board (F)");
			Utilities.installTooltip(copyFen, "Copy FEN to clipboard");
			Utilities.installTooltip(hint, "Show move hint");
			Utilities.installTooltip(navBackward, "Previous move (left arrow, hold Ctrl to jump to start)");
			Utilities.installTooltip(navForward, "Next move (right arrow, hold Ctrl to jump to end)");
			Utilities.installTooltip(undoMove, "Take back last move (backspace)");
			Utilities.installTooltip(copyPgn, "Copy PGN to clipboard");
			Utilities.installTooltip(hidePopup, "Hide result card");
			
			if (setup.singlePlayer) {
				root.getChildren().removeAll(draw, draw.text);
				if (playerIsWhite) root.getChildren().removeAll(bResign, bResign.text);
				else root.getChildren().removeAll(wResign, wResign.text);
			}
		}
		
		void resize() {
			wResign.setWidth(resignWidth);
			wResign.setHeight(resignHeight);
			bResign.setWidth(resignWidth);
			bResign.setHeight(resignHeight);
			if (board.flipped) {
				wResign.relocate(resignStartX, topResignStartY);
				bResign.relocate(resignStartX, bottomResignStartY);
			} else {
				wResign.relocate(resignStartX, bottomResignStartY);
				bResign.relocate(resignStartX, topResignStartY);
			}
			wResign.text.setFont(Font.font("Arial Rounded MT Bold", resignHeight / 2));
			bResign.text.setFont(Font.font("Arial Rounded MT Bold", resignHeight / 2));
			wResign.repositionText();
			bResign.repositionText();
			wResign.setArcWidth(resignHeight / 2);
			wResign.setArcHeight(resignHeight / 2);
			wResign.setStrokeWidth(strokeWidth);
			bResign.setArcWidth(resignHeight / 2);
			bResign.setArcHeight(resignHeight / 2);
			bResign.setStrokeWidth(strokeWidth);
			
			draw.setWidth(drawWidth);
			draw.setHeight(drawHeight);
			draw.relocate(drawStartX, drawStartY);
			draw.text.setFont(Font.font("Arial Rounded MT Bold", drawHeight / 2));
			draw.repositionText();
			draw.setArcWidth(drawHeight / 2);
			draw.setArcHeight(drawHeight / 2);
			draw.setStrokeWidth(strokeWidth);
			
			rematch.setWidth(popupButtonWidth);
			rematch.setHeight(popupButtonHeight);
			rematch.relocate(popupStartX + popupButtonGap, popupButtonStartY);
			rematch.text.setFont(Font.font("Arial Rounded MT Bold", popupButtonHeight * 0.45));
			rematch.repositionText();
			rematch.setArcWidth(popupButtonHeight / 2);
			rematch.setArcHeight(popupButtonHeight / 2);
			rematch.setStrokeWidth(strokeWidth);
			
			newGame.setWidth(popupButtonWidth);
			newGame.setHeight(popupButtonHeight);
			newGame.relocate(popupStartX + popupButtonGap * 2 + popupButtonWidth, popupButtonStartY);
			newGame.text.setFont(Font.font("Arial Rounded MT Bold", popupButtonHeight * 0.45));
			newGame.repositionText();
			newGame.setArcWidth(popupButtonHeight / 2);
			newGame.setArcHeight(popupButtonHeight / 2);
			newGame.setStrokeWidth(strokeWidth);
			
			copyPgn.setWidth(popupSmallButtonSize);
			copyPgn.setHeight(popupSmallButtonSize);
			copyPgn.relocate(popupStartX + popupButtonGap / 2, popupStartY + popupButtonGap / 2);
			copyPgn.text.setFont(Font.font("Arial Rounded MT Bold", popupSmallButtonSize * 0.7));
			copyPgn.repositionText();
			
			hidePopup.setWidth(popupSmallButtonSize);
			hidePopup.setHeight(popupSmallButtonSize);
			hidePopup.relocate(popupStartX + popupWidth - popupButtonGap / 2 - popupSmallButtonSize, popupStartY + popupButtonGap / 2);
			hidePopup.text.setFont(Font.font("Arial Rounded MT Bold", popupSmallButtonSize * 0.6));
			hidePopup.repositionText();
			
			exit.setWidth(boardMarginSize);
			exit.setHeight(boardMarginSize);
			exit.relocate(playAreaStartX + boardMarginSize + boardSize - strokeWidth, gameStartY + strokeWidth);
			exit.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.6));
			exit.repositionText();
			
			reset.setWidth(boardMarginSize);
			reset.setHeight(boardMarginSize);
			reset.relocate(playAreaStartX + strokeWidth, gameStartY + strokeWidth);
			reset.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.8));
			reset.repositionText();
			
			flip.setWidth(boardMarginSize);
			flip.setHeight(boardMarginSize);
			flip.relocate(playAreaStartX + strokeWidth, gameStartY + gameHeight - boardMarginSize - strokeWidth);
			flip.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.8));
			flip.repositionText();
			
			copyFen.setWidth(boardMarginSize);
			copyFen.setHeight(boardMarginSize);
			copyFen.relocate(playAreaStartX + boardMarginSize + boardSize - strokeWidth, gameStartY + gameHeight - boardMarginSize - strokeWidth);
			copyFen.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.8));
			copyFen.repositionText();
			
			hint.setWidth(moveButtonWidth);
			hint.setHeight(moveButtonHeight);
			hint.relocate(moveAreaStartX, moveButtonStartY);
			hint.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.6));
			hint.repositionText();
			hint.setStrokeWidth(strokeWidth);
			
			navBackward.setWidth(moveButtonWidth);
			navBackward.setHeight(moveButtonHeight);
			navBackward.relocate(moveAreaStartX + moveButtonWidth, moveButtonStartY);
			navBackward.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.8));
			navBackward.repositionText();
			navBackward.setStrokeWidth(strokeWidth);
			
			navForward.setWidth(moveButtonWidth);
			navForward.setHeight(moveButtonHeight);
			navForward.relocate(moveAreaStartX + moveButtonWidth * 2, moveButtonStartY);
			navForward.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.8));
			navForward.repositionText();
			navForward.setStrokeWidth(strokeWidth);
			
			undoMove.setWidth(moveButtonWidth);
			undoMove.setHeight(moveButtonHeight);
			undoMove.relocate(moveAreaStartX + moveButtonWidth * 3, moveButtonStartY);
			undoMove.text.setFont(Font.font("Arial Rounded MT Bold", boardMarginSize * 0.8));
			undoMove.repositionText();
			undoMove.setStrokeWidth(strokeWidth);

			if (root.getChildren().contains(graphics.moveButtonsCover)) root.getChildren().remove(graphics.moveButtonsCover);
			graphics.moveButtonsCover = Shape.subtract(Shape.subtract(graphics.panelR, graphics.moveArea), draw);
			graphics.moveButtonsCover.setFill(backingCardCol);
			graphics.moveButtonsCover.setViewOrder(-3);
			root.getChildren().add(graphics.moveButtonsCover);
			
			if (setup.timeSecs > 0) {
				giveWhiteTime.setWidth(clockHeight / 6);
				giveWhiteTime.setHeight(clockHeight / 6);
				giveWhiteTime.text.setFont(Font.font("Arial Rounded MT Bold", clockHeight / 6));
				giveBlackTime.setWidth(clockHeight / 6);
				giveBlackTime.setHeight(clockHeight / 6);
				giveBlackTime.text.setFont(Font.font("Arial Rounded MT Bold", clockHeight / 6));
				
				if (board.flipped) { 
					giveWhiteTime.relocate(clockStartX + strokeWidth / 2, clockStartY + clockHeight / 2 - clockHeight / 6 - strokeWidth / 2);
					giveBlackTime.relocate(clockStartX + strokeWidth / 2, clockStartY + clockHeight / 2 + strokeWidth / 2);
				} else {
					giveWhiteTime.relocate(clockStartX + strokeWidth / 2, clockStartY + clockHeight / 2 + strokeWidth / 2);
					giveBlackTime.relocate(clockStartX + strokeWidth / 2, clockStartY + clockHeight / 2 - clockHeight / 6 - strokeWidth / 2);
				}

				giveWhiteTime.repositionText();
				giveBlackTime.repositionText();
			}
		}
	}
}
