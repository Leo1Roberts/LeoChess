package application;

class Sizes {
	class Entry {
		static double entryWidth;
		static double entryHeight;
		static double entryStartX;
		static double entryStartY;
		
		static double strokeWidth;
		
		static double panelMargin;
		static double panelHeight;
		static double panelStartY;
		static double panelWidth;
		static double panelOTBStartX;
		static double panelAIStartX;
		static double titleAreaHeight;
		static double panelArcSize;
		
		static double customAreaWidth;
		static double customAreaHeight;
		static double customAreaStartY;
		static double customAreaOTBStartX;
		static double customAreaAIStartX;
		
		static double playWidth;
		static double playHeight;
		static double playStartY;
		static double playOTBStartX;
		static double playAIStartX;
		
		static double normalTextSize;
		
		static double customOTBColumn1StartX;
		static double customOTBColumn2StartX;
		static double customAIColumn1StartX;
		static double customAIColumn2StartX;
		static double customFirstLineY;
		
		static double depthButtonWidth;
		static double depthButtonHeight;
		static double depthY;
		
		static double playAsIconSize;
		static double smallGap;
		static double playAsY;
		
		static double quickOTBWidth;
		static double quickOTBHeight;
		
		static double timeButtonWidth;
		static double timeButtonHeight;
		
		static double quickAIWidth;
		static double quickAIHeight;
		
		static void updateSizes(double sceneWidth, double sceneHeight) {
			if (sceneWidth / 16 < sceneHeight / 9) {
				entryWidth = sceneWidth;
				entryHeight = sceneWidth / 16 * 9;
			} else {
				entryHeight = sceneHeight;
				entryWidth = sceneHeight / 9 * 16;
			}
			entryStartX = (sceneWidth - entryWidth) / 2;
			entryStartY = (sceneHeight - entryHeight) / 2;
			
			strokeWidth = entryHeight / 540;
			
			panelMargin = entryWidth * 0.015;
			panelHeight = entryHeight - panelMargin * 2;
			panelWidth = (entryWidth - panelMargin * 3) / 2;
			//panelWidth = (entryWidth - panelMargin * 4) * 0.4;
			panelOTBStartX = entryStartX + panelMargin;
			panelAIStartX = entryStartX + panelWidth + panelMargin * 2;
			panelStartY = entryStartY + panelMargin;
			titleAreaHeight = entryHeight / 10;
			panelArcSize = panelWidth / 8;
			
			customAreaWidth = panelWidth - 2 * panelMargin;
			customAreaHeight = panelHeight / 2;
			customAreaStartY = panelStartY + panelHeight - panelMargin - customAreaHeight;
			customAreaOTBStartX = panelOTBStartX + panelMargin;
			customAreaAIStartX = panelAIStartX + panelMargin;
			
			playWidth = panelWidth / 4;
			playHeight = playWidth / 2;
			playStartY = panelStartY + panelHeight - panelMargin * 2 - playHeight;
			playOTBStartX = panelOTBStartX + panelWidth / 2 - playWidth / 2;
			
			playAIStartX = panelAIStartX + panelWidth / 2 - playWidth / 2;
			
			normalTextSize = panelHeight / 24;
			
			customOTBColumn1StartX = customAreaOTBStartX + panelMargin;
			customOTBColumn2StartX = customAreaOTBStartX + customAreaWidth / 2 + panelMargin;
			customAIColumn1StartX = customAreaAIStartX + panelMargin;
			customAIColumn2StartX = customAreaAIStartX + customAreaWidth / 2 + panelMargin;
			customFirstLineY = customAreaStartY + customAreaHeight / 3;
			
			depthButtonHeight = normalTextSize * 1.5;
			depthButtonWidth = depthButtonHeight / 2;
			depthY = panelStartY + panelHeight / 2;
			
			playAsIconSize = normalTextSize * 2;
			smallGap = playAsIconSize / 5;
			playAsY = panelStartY + panelHeight / 3 * 2;
			
			quickOTBWidth = (panelWidth - panelMargin * 4) / 3;
			quickOTBHeight = (panelHeight - titleAreaHeight - customAreaHeight - panelMargin * 4) / 2;
			
			timeButtonWidth = normalTextSize * 1.5;
			timeButtonHeight = timeButtonWidth / 2;
			
			quickAIWidth = (panelWidth - panelMargin * 3) / 2;
			quickAIHeight = (panelHeight - titleAreaHeight - customAreaHeight - panelMargin * 4) / 2;
		}
	}
	
	class Game {
		static double gameWidth;
		static double gameHeight;
		static double gameStartX;
		static double gameStartY;
		
		static double strokeWidth;
		
		static double playAreaStartX;
		
		static double boardSize;
		static double boardStartX;
		static double boardStartY;
		
		static double boardMarginSize;
		
		static double squareSize;
		static double squareBorderWidth;
		
		static double panelWidth;
		static double panelRStartX;
		
		static double popupWidth;
		static double popupHeight;
		static double popupStartX;
		static double popupStartY;
		static double popupButtonWidth;
		static double popupButtonHeight;
		static double popupButtonGap;
		static double popupButtonStartY;
		static double popupSmallButtonSize;
		
		static double capturedPieceSize;
		static int capturedRows;
		static int capturedColumns;
		static double capturedXSpacing;
		static double capturedBoxWidth;
		static double capturedBoxHeight;
		static double capturedStartX;
		static double topCapturedStartY;
		static double bottomCapturedStartY;
		static double capturedAreaWidth;
		static double capturedAreaHeight;
		static double capturedAreaPadding;
		
		static double clockWidth;
		static double clockHeight;
		static double clockStartX;
		static double clockStartY;
		
		static double resignWidth;
		static double resignHeight;
		static double resignStartX;
		static double topResignStartY;
		static double bottomResignStartY;
		
		static double drawWidth;
		static double drawHeight;
		static double drawStartX;
		static double drawStartY;
		
		static double moveAreaWidth;
		static double moveAreaHeight;
		static double moveAreaStartX;
		static double moveAreaStartY;
		static double moveButtonWidth;
		static double moveButtonHeight;
		static double moveButtonStartY;
		
		static int maxVisibleLines;
		static double moveWidth;
		static double moveHeight;
		static double moveTextSize;
		static double whiteMoveStartX;
		static double blackMoveStartX;
		
		static void updateSizes(double sceneWidth, double sceneHeight) {
			if (sceneWidth / 16 < sceneHeight / 9) {
				gameWidth = sceneWidth;
				gameHeight = sceneWidth / 16 * 9;
			} else {
				gameHeight = sceneHeight;
				gameWidth = sceneHeight / 9 * 16;
			}
			gameStartX = (sceneWidth - gameWidth) / 2;
			gameStartY = (sceneHeight - gameHeight) / 2;
			
			strokeWidth = gameHeight / 540;
			
			playAreaStartX = (sceneWidth - gameHeight) / 2;
			
			boardSize = gameHeight * 0.9;
			boardStartX = (sceneWidth - boardSize) / 2;
			boardStartY = (sceneHeight - boardSize) / 2;
			
			boardMarginSize = (gameHeight - boardSize) / 2;
			
			squareSize = boardSize / 8;
			squareBorderWidth = squareSize / 10;
			
			panelWidth = (gameWidth - gameHeight) / 2;
			panelRStartX = gameStartX + gameWidth - panelWidth;
			
			popupWidth = gameHeight / 2;
			popupHeight = squareSize * 3;
			popupStartX = playAreaStartX + (gameHeight - popupWidth) / 2;
			popupStartY = gameStartY + (gameHeight - popupHeight) / 2;
			popupButtonWidth = popupWidth * 0.45;
			popupButtonHeight = popupHeight / 4;
			popupButtonGap = (popupWidth - popupButtonWidth * 2) / 3;
			popupButtonStartY = popupStartY + popupHeight - popupButtonHeight - popupButtonGap;
			popupSmallButtonSize = popupHeight / 7;
			
			capturedBoxHeight = squareSize;
			capturedPieceSize = capturedBoxHeight / capturedRows;
			capturedXSpacing = capturedPieceSize * 0.7;
			capturedBoxWidth = capturedXSpacing * (capturedColumns - 1) + capturedPieceSize;
			capturedStartX = gameStartX + (panelWidth - capturedBoxWidth) / 2;
			topCapturedStartY = boardStartY;
			bottomCapturedStartY = gameStartY + gameHeight - boardMarginSize - capturedBoxHeight;
			capturedAreaPadding = capturedPieceSize / 4;
			capturedAreaWidth = panelWidth - boardMarginSize;
			capturedAreaHeight = capturedBoxHeight + capturedAreaPadding * 2;
			
			clockHeight = squareSize * 2;
			clockWidth = clockHeight * 1.2;
			clockStartX = gameStartX + (panelWidth - clockWidth) / 2;
			clockStartY = gameStartY + (gameHeight - clockHeight) / 2;
			
			resignHeight = squareSize / 2;
			resignWidth = resignHeight * 2.5;
			resignStartX = gameStartX + (panelWidth - resignWidth) / 2;
			topResignStartY = boardStartY + squareSize + boardMarginSize;
			bottomResignStartY = gameStartY + gameHeight - squareSize - boardMarginSize * 2 - resignHeight;
			
			drawHeight = resignHeight;
			drawWidth = capturedAreaWidth * 0.75;
			drawStartX = gameStartX + gameWidth - (panelWidth + drawWidth) / 2;
			drawStartY = gameStartY + gameHeight - boardMarginSize - drawHeight;
			
			moveAreaWidth = capturedAreaWidth;
			moveAreaHeight = squareSize * 7 + capturedAreaPadding * 2;
			moveAreaStartX = panelRStartX + (panelWidth - moveAreaWidth) / 2;
			moveAreaStartY = gameStartY + boardMarginSize - capturedAreaPadding;
			moveButtonWidth = moveAreaWidth / 4;
			moveButtonHeight = squareSize / 2;
			moveButtonStartY = moveAreaStartY + moveAreaHeight - moveButtonHeight;
			
			maxVisibleLines = 16;
			moveWidth = moveAreaWidth * 0.395;
			moveHeight = (moveAreaHeight - moveButtonHeight * 2 - strokeWidth) / (double) maxVisibleLines;
			moveTextSize = moveHeight * 0.6;
			whiteMoveStartX = moveAreaStartX + moveAreaWidth - moveWidth * 2;
			blackMoveStartX = moveAreaStartX + moveAreaWidth - moveWidth;
		}
	}
}
