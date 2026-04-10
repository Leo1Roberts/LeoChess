package application;

import static application.Sizes.Entry.*;
import static application.Constants.*;
import static application.Settings.*;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

class Entry {
	Scene scene;
	Group root;
	Graphics graphics;
	Buttons buttons;
	GameSetup setup;
	
	Entry(Scene parentScene, Group entryRoot) {
		scene = parentScene;
		root = entryRoot;
		graphics = new Graphics();
		buttons = new Buttons();
		setup = new GameSetup();
	}
	
	void init() {
		root.getChildren().clear();
		scene.setFill(voidFillerCol);  // The colour 'behind' all nodes
		setup.playerColour = RANDOM;
		setup.aiDepth = 4;
		setup.fenOTB = "";
		setup.fenAI = "";
		graphics.init();
		buttons.init();
		resizeAll();
	}
	
	void init(GameSetup previousSetup) {
		root.getChildren().clear();
		scene.setFill(voidFillerCol);  // The colour 'behind' all nodes
		setup.playerColour = RANDOM;
		setup.aiDepth = previousSetup.aiDepth;
		setup.fenOTB = previousSetup.fenOTB;
		setup.fenAI = previousSetup.fenAI;
		graphics.init();
		buttons.init();
		resizeAll();
	}
	
	void resizeAll() {
		updateSizes(scene.getWidth(), scene.getHeight());
		graphics.resize();
		buttons.resize();
	}
	
	void decreaseDepth() {
		if (buttons.decreaseDepth.active) {
			if (setup.aiDepth == 6) buttons.increaseDepth.activate();
			setup.aiDepth--;
			graphics.aiDepthValue.setText(Integer.toString(setup.aiDepth));
			if (setup.aiDepth == 1) buttons.decreaseDepth.deactivate();
			Utilities.installDepthTooltip(graphics.aiDepthBacking, setup.aiDepth);
		}
	}
	
	void increaseDepth() {
		if (buttons.increaseDepth.active) {
			if (setup.aiDepth == 1) buttons.decreaseDepth.activate();
			setup.aiDepth++;
			graphics.aiDepthValue.setText(Integer.toString(setup.aiDepth));
			if (setup.aiDepth == 6) buttons.increaseDepth.deactivate();
			Utilities.installDepthTooltip(graphics.aiDepthBacking, setup.aiDepth);
		}
	}
	
	void decreaseTimeMins(boolean jump) {
		if (buttons.decreaseTimeMins.active) {
			if (setup.timeSecs / 60 == 59) buttons.increaseTimeMins.activate();
			if (jump) setup.timeSecs -= Math.min(10, setup.timeSecs / 60) * 60;
			else setup.timeSecs -= 60;
			int mins = setup.timeSecs / 60;
			String zero = "";
			if (mins < 10) zero = "0";
			graphics.timeMins.setText(zero + mins);
			if (mins == 0) buttons.decreaseTimeMins.deactivate();
		}
	}
	
	void increaseTimeMins(boolean jump) {
		if (buttons.increaseTimeMins.active) {
			if (setup.timeSecs / 60 == 0) buttons.decreaseTimeMins.activate();
			if (jump) setup.timeSecs += Math.min(10, 59 - setup.timeSecs / 60) * 60;
			else setup.timeSecs += 60;
			int mins = setup.timeSecs / 60;
			String zero = "";
			if (mins < 10) zero = "0";
			graphics.timeMins.setText(zero + mins);
			if (mins == 59) buttons.increaseTimeMins.deactivate();
		}
	}
	
	void decreaseTimeSecs(boolean jump) {
		if (buttons.decreaseTimeSecs.active) {
			if (setup.timeSecs % 60 == 59) buttons.increaseTimeSecs.activate();
			if (jump) setup.timeSecs -= Math.min(10, setup.timeSecs % 60);
			else setup.timeSecs--;
			int secs = setup.timeSecs % 60;
			String zero = "";
			if (secs < 10) zero = "0";
			graphics.timeSecs.setText(zero + secs);
			if (secs == 0) buttons.decreaseTimeSecs.deactivate();
		}
	}
	
	void increaseTimeSecs(boolean jump) {
		if (buttons.increaseTimeSecs.active) {
			if (setup.timeSecs % 60 == 0) buttons.decreaseTimeSecs.activate();
			if (jump) setup.timeSecs += Math.min(10, 59 - setup.timeSecs % 60);
			else setup.timeSecs++;
			int secs = setup.timeSecs % 60;
			String zero = "";
			if (secs < 10) zero = "0";
			graphics.timeSecs.setText(zero + secs);
			if (secs == 59) buttons.increaseTimeSecs.deactivate();
		}
	}
	
	void decreaseIncrementSecs(boolean jump) {
		if (buttons.decreaseIncrementSecs.active) {
			if (setup.incrementSecs == 59) buttons.increaseIncrementSecs.activate();
			if (jump) setup.incrementSecs -= Math.min(10, setup.incrementSecs);
			else setup.incrementSecs--;
			String zero = "";
			if (setup.incrementSecs < 10) zero = "0";
			graphics.incrementSecs.setText(zero + setup.incrementSecs);
			if (setup.incrementSecs == 0) buttons.decreaseIncrementSecs.deactivate();
		}
	}
	
	void increaseIncrementSecs(boolean jump) {
		if (buttons.increaseIncrementSecs.active) {
			if (setup.incrementSecs == 0) buttons.decreaseIncrementSecs.activate();
			if (jump) setup.incrementSecs += Math.min(10, 59 - setup.incrementSecs);
			else setup.incrementSecs++;
			String zero = "";
			if (setup.incrementSecs < 10) zero = "0";
			graphics.incrementSecs.setText(zero + setup.incrementSecs);
			if (setup.incrementSecs == 59) buttons.increaseIncrementSecs.deactivate();
		}
	}
	
	class Graphics {  // For purely graphical elements that have no functionality
		Rectangle backing;
		Rectangle panelOTB;
		Rectangle panelAI;
		Rectangle panelOTBBorder;
		Rectangle panelAIBorder;
		Rectangle titleOTBArea;
		Rectangle titleAIArea;
		Rectangle coverOTB;
		Rectangle coverAI;
		Line titleOTBSeparator;
		Line titleAISeparator;
		Text titleOTB;
		Text titleAI;
		Rectangle customOTBArea;
		Rectangle customAIArea;
		Text customOTBTitle;
		Text customAITitle;
		Text aiDepth;
		Rectangle aiDepthBacking;
		Text aiDepthValue;
		Text playAs;
		Image kingWpng;
		Image kingBpng;
		WritableImage kingWB;
		Text time;
		Text increment;
		Rectangle timeMinsBacking;
		Text timeMins;
		Text colon;
		Text timeSecs;
		Rectangle timeSecsBacking;
		Text incrementSecs;
		Rectangle incrementSecsBacking;
		
		void init() {
			backing = new Rectangle(0, 0, backingCardCol);
			backing.setStroke(borderCol);
			backing.setStrokeType(StrokeType.INSIDE);
			
			panelOTB = new Rectangle(0, 0, panelCol1);
			panelOTB.setStroke(borderCol);
			panelOTBBorder = new Rectangle(0, 0, Color.TRANSPARENT);
			panelOTBBorder.setStroke(borderCol);
			
			panelAI = new Rectangle(0, 0, panelCol1);
			panelAI.setStroke(borderCol);
			panelAIBorder = new Rectangle(0, 0, Color.TRANSPARENT);
			panelAIBorder.setStroke(borderCol);
			
			titleOTBArea = new Rectangle(0, 0, panelCol2);
			titleOTBArea.setStroke(borderCol);
			titleAIArea = new Rectangle(0, 0, panelCol2);
			titleAIArea.setStroke(borderCol);
			
			coverOTB = new Rectangle(0, 0, panelCol1);
			coverAI = new Rectangle(0, 0, panelCol1);
			
			titleOTBSeparator = new Line();
			titleOTBSeparator.setStroke(borderCol);
			titleOTBSeparator.setStrokeLineCap(StrokeLineCap.BUTT);
			titleAISeparator = new Line();
			titleAISeparator.setStroke(borderCol);
			titleAISeparator.setStrokeLineCap(StrokeLineCap.BUTT);
			
			titleOTB = new Text("Two player");
			titleOTB.setTextOrigin(VPos.CENTER);
			titleOTB.setFill(textCol);
			titleAI = new Text("Single player");
			titleAI.setTextOrigin(VPos.CENTER);
			titleAI.setFill(textCol);
			
			customOTBArea = new Rectangle(0, 0, panelCol2);
			customOTBArea.setStroke(borderCol);
			customAIArea = new Rectangle(0, 0, panelCol2);
			customAIArea.setStroke(borderCol);
			
			customOTBTitle = new Text("Custom");
			customOTBTitle.setTextOrigin(VPos.CENTER);
			customOTBTitle.setFill(textCol);
			customAITitle = new Text("Custom");
			customAITitle.setTextOrigin(VPos.CENTER);
			customAITitle.setFill(textCol);
			
			aiDepth = new Text("AI depth");
			aiDepth.setTextOrigin(VPos.CENTER);
			aiDepth.setFill(textCol);
			
			aiDepthBacking = new Rectangle(0, 0, buttonCol);
			aiDepthBacking.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseDepth();
				else if (deltaY < 0) decreaseDepth();
				root.requestFocus();
			});
			aiDepthBacking.setStroke(borderCol);
			
			aiDepthValue = new Text(Integer.toString(setup.aiDepth));
			aiDepthValue.setTextOrigin(VPos.CENTER);
			aiDepthValue.setFill(buttonTextCol);
			aiDepthValue.setMouseTransparent(true);
			
			playAs = new Text("Play as");
			playAs.setTextOrigin(VPos.CENTER);
			playAs.setFill(textCol);
			
			kingWpng = new Image("file:assets/pieces/" + piecesFolder + "/King W.png");
			kingBpng = new Image("file:assets/pieces/" + piecesFolder + "/King B.png");
			int pngSize = (int) kingWpng.getWidth();
			kingWB = new WritableImage(pngSize, pngSize);  // Split-colour king image
			
			// Combining the two different-coloured kings into one
			PixelReader kingWpngReader = kingWpng.getPixelReader();
			PixelReader kingBpngReader = kingBpng.getPixelReader();
			PixelWriter writer = kingWB.getPixelWriter();
			for (int y = 0; y < pngSize; y++) {
				for (int x = 0; x < pngSize / 2; x++) {
					writer.setColor(x, y, kingWpngReader.getColor(x, y));  // Copy the left half of the white king
				}
			}
			for (int y = 0; y < pngSize; y++) {
				for (int x = pngSize / 2; x < pngSize; x++) {
					writer.setColor(x, y, kingBpngReader.getColor(x, y));  // Copy the right half of the black king
				}
			}
			
			time = new Text("Time");
			time.setTextOrigin(VPos.CENTER);
			time.setFill(textCol);
			
			timeMinsBacking = new Rectangle(0, 0, buttonCol);
			timeMinsBacking.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseTimeMins(event.isControlDown());
				else if (deltaY < 0) decreaseTimeMins(event.isControlDown());
				root.requestFocus();
			});
			timeMinsBacking.setStroke(borderCol);
			
			int mins = setup.timeSecs / 60;
			String zero = "";
			if (mins < 10) zero = "0";
			timeMins = new Text(zero + mins);
			timeMins.setTextOrigin(VPos.CENTER);
			timeMins.setFill(buttonTextCol);
			timeMins.setMouseTransparent(true);
			
			colon = new Text(":");
			colon.setTextOrigin(VPos.CENTER);
			colon.setFill(textCol);
			
			timeSecsBacking = new Rectangle(0, 0, buttonCol);
			timeSecsBacking.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseTimeSecs(event.isControlDown());
				else if (deltaY < 0) decreaseTimeSecs(event.isControlDown());
				root.requestFocus();
			});
			timeSecsBacking.setStroke(borderCol);
			
			int secs = setup.timeSecs % 60;
			zero = "";
			if (secs < 10) zero = "0";
			timeSecs = new Text(zero + secs);
			timeSecs.setTextOrigin(VPos.CENTER);
			timeSecs.setFill(buttonTextCol);
			timeSecs.setMouseTransparent(true);
			
			increment = new Text("Increment");
			increment.setTextOrigin(VPos.CENTER);
			increment.setFill(textCol);
			
			incrementSecsBacking = new Rectangle(0, 0, buttonCol);
			incrementSecsBacking.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseIncrementSecs(event.isControlDown());
				else if (deltaY < 0) decreaseIncrementSecs(event.isControlDown());
				root.requestFocus();
			});
			incrementSecsBacking.setStroke(borderCol);
			
			zero = "";
			if (setup.incrementSecs < 10) zero = "0";
			incrementSecs = new Text(zero + setup.incrementSecs);
			incrementSecs.setTextOrigin(VPos.CENTER);
			incrementSecs.setFill(buttonTextCol);
			incrementSecs.setMouseTransparent(true);
			
			root.getChildren().addAll(backing, panelOTB, panelAI, titleOTBArea, titleAIArea, coverOTB, coverAI, titleOTBSeparator, titleAISeparator, panelOTBBorder, panelAIBorder, titleOTB, titleAI, customOTBArea, customAIArea, customOTBTitle, customAITitle, aiDepth, aiDepthBacking, aiDepthValue, playAs, time, timeMinsBacking, timeMins, colon, timeSecsBacking, timeSecs, increment, incrementSecsBacking, incrementSecs);
			
			Utilities.installTooltip(aiDepth, "How far into the future the AI looks - higher values make the AI better but slower");
			Utilities.installDepthTooltip(aiDepthBacking, setup.aiDepth);
			Utilities.installTooltip(playAs, "Which colour pieces to play as");
			Utilities.installTooltip(time, "The amount of time for each player - set to 0 to disable the timer");
			Utilities.installTooltip(timeMinsBacking, "The number of minutes for each player");
			Utilities.installTooltip(timeSecsBacking, "The number of seconds for each player");
			Utilities.installTooltip(increment, "The amount of time a player gains each move");
			Utilities.installTooltip(incrementSecsBacking, "The number of seconds a player gains each move");
		}
		
		void resize() {
			backing.relocate(entryStartX, entryStartY);
			backing.setWidth(entryWidth);
			backing.setHeight(entryHeight);
			backing.setStrokeWidth(strokeWidth);
			
			panelOTB.relocate(panelOTBStartX, panelStartY);
			panelOTB.setWidth(panelWidth);
			panelOTB.setHeight(panelHeight);
			panelOTB.setArcWidth(panelArcSize);
			panelOTB.setArcHeight(panelArcSize);
			panelOTB.setStrokeWidth(strokeWidth);
			panelOTBBorder.relocate(panelOTBStartX, panelStartY);
			panelOTBBorder.setWidth(panelWidth);
			panelOTBBorder.setHeight(panelHeight);
			panelOTBBorder.setArcWidth(panelArcSize);
			panelOTBBorder.setArcHeight(panelArcSize);
			panelOTBBorder.setStrokeWidth(strokeWidth);
			
			panelAI.relocate(panelAIStartX, panelStartY);
			panelAI.setWidth(panelWidth);
			panelAI.setHeight(panelHeight);
			panelAI.setArcWidth(panelArcSize);
			panelAI.setArcHeight(panelArcSize);
			panelAI.setStrokeWidth(strokeWidth);
			panelAIBorder.relocate(panelAIStartX, panelStartY);
			panelAIBorder.setWidth(panelWidth);
			panelAIBorder.setHeight(panelHeight);
			panelAIBorder.setArcWidth(panelArcSize);
			panelAIBorder.setArcHeight(panelArcSize);
			panelAIBorder.setStrokeWidth(strokeWidth);
			
			titleOTBSeparator.setStartX(panelOTBStartX);
			titleOTBSeparator.setStartY(panelStartY + titleAreaHeight);
			titleOTBSeparator.setEndX(panelOTBStartX + panelWidth);
			titleOTBSeparator.setEndY(panelStartY + titleAreaHeight);
			titleOTBSeparator.setStrokeWidth(strokeWidth);
			
			titleAISeparator.setStartX(panelAIStartX);
			titleAISeparator.setStartY(panelStartY + titleAreaHeight);
			titleAISeparator.setEndX(panelAIStartX + panelWidth);
			titleAISeparator.setEndY(panelStartY + titleAreaHeight);
			titleAISeparator.setStrokeWidth(strokeWidth);
			
			titleOTBArea.relocate(panelOTBStartX, panelStartY);
			titleOTBArea.setWidth(panelWidth);
			titleOTBArea.setHeight(titleAreaHeight * 2);
			titleOTBArea.setArcWidth(panelArcSize);
			titleOTBArea.setArcHeight(panelArcSize);
			titleOTBArea.setStrokeWidth(strokeWidth);
			
			titleAIArea.relocate(panelAIStartX, panelStartY);
			titleAIArea.setWidth(panelWidth);
			titleAIArea.setHeight(titleAreaHeight * 2);
			titleAIArea.setArcWidth(panelArcSize);
			titleAIArea.setArcHeight(panelArcSize);
			titleAIArea.setStrokeWidth(strokeWidth);
			
			coverOTB.relocate(panelOTBStartX + strokeWidth / 2, panelStartY + titleAreaHeight);
			coverOTB.setWidth(panelWidth - strokeWidth / 2);
			coverOTB.setHeight(titleAreaHeight * 2);
			
			coverAI.relocate(panelAIStartX + strokeWidth / 2, panelStartY + titleAreaHeight);
			coverAI.setWidth(panelWidth - strokeWidth / 2);
			coverAI.setHeight(titleAreaHeight * 2);
			
			titleOTB.setFont(Font.font("Arial Rounded MT Bold", titleAreaHeight * 0.7));
			titleOTB.setX(panelOTBStartX + panelWidth / 2 - titleOTB.getLayoutBounds().getWidth() / 2);
			titleOTB.setY(panelStartY + titleAreaHeight / 2 - panelMargin / 4);
			
			titleAI.setFont(Font.font("Arial Rounded MT Bold", titleAreaHeight * 0.7));
			titleAI.setX(panelAIStartX + panelWidth / 2 - titleAI.getLayoutBounds().getWidth() / 2);
			titleAI.setY(panelStartY + titleAreaHeight / 2 - panelMargin / 4);
			
			customOTBArea.relocate(customAreaOTBStartX, customAreaStartY);
			customOTBArea.setWidth(customAreaWidth);
			customOTBArea.setHeight(customAreaHeight);
			customOTBArea.setArcWidth(customAreaHeight / 6);
			customOTBArea.setArcHeight(customAreaHeight / 6);
			customOTBArea.setStrokeWidth(strokeWidth);
			
			customAIArea.relocate(customAreaAIStartX, customAreaStartY);
			customAIArea.setWidth(customAreaWidth);
			customAIArea.setHeight(customAreaHeight);
			customAIArea.setArcWidth(customAreaHeight / 6);
			customAIArea.setArcHeight(customAreaHeight / 6);
			customAIArea.setStrokeWidth(strokeWidth);
			
			customOTBTitle.setFont(Font.font("Arial Rounded MT Bold", titleAreaHeight / 2));
			customOTBTitle.setX(customAreaOTBStartX + panelMargin);
			customOTBTitle.setY(customAreaStartY + panelMargin * 2 / 3 + customOTBTitle.getLayoutBounds().getHeight() / 2);
			
			customAITitle.setFont(Font.font("Arial Rounded MT Bold", titleAreaHeight / 2));
			customAITitle.setX(customAIColumn1StartX);
			customAITitle.setY(customAreaStartY + panelMargin * 2 / 3 + customAITitle.getLayoutBounds().getHeight() / 2);
			
			aiDepth.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			aiDepthValue.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			aiDepth.setX(customAIColumn1StartX);
			aiDepth.setY(customFirstLineY);
			aiDepthValue.setX(customAIColumn1StartX + aiDepth.getLayoutBounds().getWidth() + smallGap + depthButtonWidth);
			aiDepthValue.setY(customFirstLineY);
			
			aiDepthBacking.relocate(customAIColumn1StartX + aiDepth.getLayoutBounds().getWidth() + smallGap, customFirstLineY - depthButtonHeight / 2);
			aiDepthBacking.setWidth(depthButtonWidth * 2 + aiDepthValue.getLayoutBounds().getWidth());
			aiDepthBacking.setHeight(depthButtonHeight);
			aiDepthBacking.setArcWidth(depthButtonHeight / 2);
			aiDepthBacking.setArcHeight(depthButtonHeight / 2);
			aiDepthBacking.setStrokeWidth(strokeWidth);
			
			playAs.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			playAs.setX(customAIColumn2StartX);
			playAs.setY(customFirstLineY);
			
			time.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			time.setX(customOTBColumn1StartX);
			time.setY(customFirstLineY);
			
			timeMins.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			timeMins.setX(customOTBColumn1StartX + time.getLayoutBounds().getWidth() + smallGap + (timeButtonWidth - timeMins.getLayoutBounds().getWidth()) / 2 + strokeWidth / 2);
			timeMins.setY(customFirstLineY);
			
			timeMinsBacking.setWidth(timeButtonWidth);
			timeMinsBacking.setHeight(timeButtonHeight * 2 + normalTextSize);
			timeMinsBacking.relocate(customOTBColumn1StartX + time.getLayoutBounds().getWidth() + smallGap, customFirstLineY - timeButtonHeight - normalTextSize / 2);
			timeMinsBacking.setArcWidth(timeButtonWidth / 2);
			timeMinsBacking.setArcHeight(timeButtonWidth / 2);
			timeMinsBacking.setStrokeWidth(strokeWidth);
			
			colon.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			colon.setX(customOTBColumn1StartX + time.getLayoutBounds().getWidth() + smallGap + timeButtonWidth + strokeWidth + strokeWidth / 4);
			colon.setY(customFirstLineY);
			
			timeSecs.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			timeSecs.setX(customOTBColumn1StartX + time.getLayoutBounds().getWidth() + smallGap + timeButtonWidth + strokeWidth * 2 + colon.getLayoutBounds().getWidth() + (timeButtonWidth - timeSecs.getLayoutBounds().getWidth()) / 2 + strokeWidth / 2);
			timeSecs.setY(customFirstLineY);
			
			timeSecsBacking.setWidth(timeButtonWidth);
			timeSecsBacking.setHeight(timeButtonHeight * 2 + normalTextSize);
			timeSecsBacking.relocate(customOTBColumn1StartX + time.getLayoutBounds().getWidth() + smallGap + timeButtonWidth + strokeWidth * 2 + colon.getLayoutBounds().getWidth(), customFirstLineY - timeButtonHeight - normalTextSize / 2);
			timeSecsBacking.setArcWidth(timeButtonWidth / 2);
			timeSecsBacking.setArcHeight(timeButtonWidth / 2);
			timeSecsBacking.setStrokeWidth(strokeWidth);
			
			increment.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			increment.setX(customOTBColumn2StartX);
			increment.setY(customFirstLineY);
			
			incrementSecs.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			incrementSecs.setX(customOTBColumn2StartX + increment.getLayoutBounds().getWidth() + smallGap + (timeButtonWidth - incrementSecs.getLayoutBounds().getWidth()) / 2 + strokeWidth / 2);
			incrementSecs.setY(customFirstLineY);
			
			incrementSecsBacking.setWidth(timeButtonWidth);
			incrementSecsBacking.setHeight(timeButtonHeight * 2 + normalTextSize);
			incrementSecsBacking.relocate(customOTBColumn2StartX + increment.getLayoutBounds().getWidth() + smallGap, customFirstLineY - timeButtonHeight - normalTextSize / 2);
			incrementSecsBacking.setArcWidth(timeButtonWidth / 2);
			incrementSecsBacking.setArcHeight(timeButtonWidth / 2);
			incrementSecsBacking.setStrokeWidth(strokeWidth);
		}
	}
	
	class Buttons {  // For functional elements
		Button playOTB;
		Button playAI;
		Button decreaseDepth;
		Button increaseDepth;
		Button playAsButton;
		Button quickOTB1;
		Button quickOTB2;
		Button quickOTB3;
		Button quickOTB4;
		Button quickOTB5;
		Button quickOTB6;
		Button decreaseTimeMins;
		Button increaseTimeMins;
		Button decreaseTimeSecs;
		Button increaseTimeSecs;
		Button decreaseIncrementSecs;
		Button increaseIncrementSecs;
		Button quickAI1;
		Button quickAI2;
		Button quickAI3;
		Button quickAI4;
		TextField fenOTB;
		TextField fenAI;
		
		void init() {
			playOTB = new Button(playButtonCol, playButtonHoverCol, "PLAY", playButtonTextCol);
			playOTB.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					if (setup.timeSecs == 0) setup.incrementSecs = 0;
					setup.fenOTB = fenOTB.getText().strip();
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			playOTB.setStroke(borderCol);
			
			playAI = new Button(playButtonCol, playButtonHoverCol, "PLAY", playButtonTextCol);
			playAI.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = true;
					setup.timeSecs = 0;
					setup.fenAI = fenAI.getText().strip();
					setup.fenOTB = fenOTB.getText();
					Main.startGame(setup);
				}
			});
			playAI.setStroke(borderCol);
			
			decreaseDepth = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "◂", buttonTextCol, inactiveButtonTextCol);
			decreaseDepth.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					decreaseDepth();
					root.requestFocus();
				}
			});
			decreaseDepth.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseDepth();
				else if (deltaY < 0) decreaseDepth();
				root.requestFocus();
			});
			
			increaseDepth = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▸", buttonTextCol, inactiveButtonTextCol);
			increaseDepth.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					increaseDepth();
					root.requestFocus();
				}
			});
			increaseDepth.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseDepth();
				else if (deltaY < 0) decreaseDepth();
				root.requestFocus();
			});
			
			if (setup.aiDepth == 1) decreaseDepth.deactivate();
			else if (setup.aiDepth == 6) increaseDepth.deactivate();
			
			playAsButton = new Button(darkSquareCol, lightSquareCol, graphics.kingWB);
			playAsButton.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					switch (setup.playerColour) {
					case RANDOM:
						setup.playerColour = WHITE;
						playAsButton.imageView.setImage(graphics.kingWpng);
						Utilities.installTooltip(playAsButton, "White");
						break;
					case WHITE:
						setup.playerColour = BLACK;
						playAsButton.imageView.setImage(graphics.kingBpng);
						Utilities.installTooltip(playAsButton, "Black");
						break;
					case BLACK:
						setup.playerColour = RANDOM;
						playAsButton.imageView.setImage(graphics.kingWB);
						Utilities.installTooltip(playAsButton, "Random, then alternating");
						break;
					}
					root.requestFocus();
				}
			});
			playAsButton.setStroke(borderCol);
			
			quickOTB1 = new Button(buttonCol, buttonHoverCol, "Unlimited", buttonTextCol);
			quickOTB1.setStroke(borderCol);
			quickOTB1.text.setTextAlignment(TextAlignment.CENTER);
			quickOTB1.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					setup.timeSecs = 0;
					setup.incrementSecs = 0;
					setup.fenOTB = "";
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			quickOTB2 = new Button(buttonCol, buttonHoverCol, "3 + 2\n🗲Blitz", buttonTextCol);
			quickOTB2.setStroke(borderCol);
			quickOTB2.text.setTextAlignment(TextAlignment.CENTER);
			quickOTB2.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					setup.timeSecs = 3 * 60;
					setup.incrementSecs = 2;
					setup.fenOTB = "";
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			quickOTB3 = new Button(buttonCol, buttonHoverCol, "5 + 0\n🗲Blitz", buttonTextCol);
			quickOTB3.setStroke(borderCol);
			quickOTB3.text.setTextAlignment(TextAlignment.CENTER);
			quickOTB3.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					setup.timeSecs = 5 * 60;
					setup.incrementSecs = 0;
					setup.fenOTB = "";
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			quickOTB4 = new Button(buttonCol, buttonHoverCol, "10 + 0\n🕑 Rapid", buttonTextCol);
			quickOTB4.setStroke(borderCol);
			quickOTB4.text.setTextAlignment(TextAlignment.CENTER);
			quickOTB4.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					setup.timeSecs = 10 * 60;
					setup.incrementSecs = 0;
					setup.fenOTB = "";
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			quickOTB5 = new Button(buttonCol, buttonHoverCol, "15 + 10\n🕑 Rapid", buttonTextCol);
			quickOTB5.setStroke(borderCol);
			quickOTB5.text.setTextAlignment(TextAlignment.CENTER);
			quickOTB5.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					setup.timeSecs = 15 * 60;
					setup.incrementSecs = 10;
					setup.fenOTB = "";
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			quickOTB6 = new Button(buttonCol, buttonHoverCol, "30 + 20\nClassical", buttonTextCol);
			quickOTB6.setStroke(borderCol);
			quickOTB6.text.setTextAlignment(TextAlignment.CENTER);
			quickOTB6.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = false;
					setup.timeSecs = 30 * 60;
					setup.incrementSecs = 20;
					setup.fenOTB = "";
					setup.fenAI = fenAI.getText();
					Main.startGame(setup);
				}
			});
			
			decreaseTimeMins = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▾", buttonTextCol, inactiveButtonTextCol);
			decreaseTimeMins.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					decreaseTimeMins(event.isControlDown());
					root.requestFocus();
				}
			});
			decreaseTimeMins.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseTimeMins(event.isControlDown());
				else if (deltaY < 0) decreaseTimeMins(event.isControlDown());
				root.requestFocus();
			});
			
			increaseTimeMins = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▴", buttonTextCol, inactiveButtonTextCol);
			increaseTimeMins.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					increaseTimeMins(event.isControlDown());
					root.requestFocus();
				}
			});
			increaseTimeMins.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseTimeMins(event.isControlDown());
				else if (deltaY < 0) decreaseTimeMins(event.isControlDown());
				root.requestFocus();
			});
			
			if (setup.timeSecs / 60 == 0) decreaseTimeMins.deactivate();
			else if (setup.timeSecs / 60 == 59) increaseTimeMins.deactivate();
			
			decreaseTimeSecs = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▾", buttonTextCol, inactiveButtonTextCol);
			decreaseTimeSecs.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					decreaseTimeSecs(event.isControlDown());
					root.requestFocus();
				}
			});
			decreaseTimeSecs.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseTimeSecs(event.isControlDown());
				else if (deltaY < 0) decreaseTimeSecs(event.isControlDown());
				root.requestFocus();
			});
			
			increaseTimeSecs = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▴", buttonTextCol, inactiveButtonTextCol);
			increaseTimeSecs.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					increaseTimeSecs(event.isControlDown());
					root.requestFocus();
				}
			});
			increaseTimeSecs.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseTimeSecs(event.isControlDown());
				else if (deltaY < 0) decreaseTimeSecs(event.isControlDown());
				root.requestFocus();
			});
			
			if (setup.timeSecs % 60 == 0) decreaseTimeSecs.deactivate();
			else if (setup.timeSecs % 60 == 59) increaseTimeSecs.deactivate();
			
			decreaseIncrementSecs = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▾", buttonTextCol, inactiveButtonTextCol);
			decreaseIncrementSecs.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					decreaseIncrementSecs(event.isControlDown());
					root.requestFocus();
				}
			});
			decreaseIncrementSecs.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseIncrementSecs(event.isControlDown());
				else if (deltaY < 0) decreaseIncrementSecs(event.isControlDown());
				root.requestFocus();
			});
			
			increaseIncrementSecs = new Button(Color.TRANSPARENT, Color.TRANSPARENT, "▴", buttonTextCol, inactiveButtonTextCol);
			increaseIncrementSecs.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					increaseIncrementSecs(event.isControlDown());
					root.requestFocus();
				}
			});
			increaseIncrementSecs.setOnScroll(event -> {
				double deltaY = event.getDeltaY();
				if (deltaY > 0) increaseIncrementSecs(event.isControlDown());
				else if (deltaY < 0) decreaseIncrementSecs(event.isControlDown());
				root.requestFocus();
			});
			
			if (setup.incrementSecs == 0) decreaseIncrementSecs.deactivate();
			else if (setup.incrementSecs == 59) increaseIncrementSecs.deactivate();
			
			quickAI1 = new Button(buttonCol, buttonHoverCol, "Depth 1\nBanzai", buttonTextCol);
			quickAI1.setStroke(borderCol);
			quickAI1.text.setTextAlignment(TextAlignment.CENTER);
			quickAI1.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = true;
					setup.aiDepth = 1;
					setup.timeSecs = 0;
					setup.incrementSecs = 0;
					setup.fenAI = "";
					setup.fenOTB = fenOTB.getText();
					Main.startGame(setup);
				}
			});
			quickAI2 = new Button(buttonCol, buttonHoverCol, "Depth 2\nBeginner", buttonTextCol);
			quickAI2.setStroke(borderCol);
			quickAI2.text.setTextAlignment(TextAlignment.CENTER);
			quickAI2.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = true;
					setup.aiDepth = 2;
					setup.timeSecs = 0;
					setup.incrementSecs = 0;
					setup.fenAI = "";
					setup.fenOTB = fenOTB.getText();
					Main.startGame(setup);
				}
			});
			quickAI3 = new Button(buttonCol, buttonHoverCol, "Depth 4\nLearner", buttonTextCol);
			quickAI3.setStroke(borderCol);
			quickAI3.text.setTextAlignment(TextAlignment.CENTER);
			quickAI3.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = true;
					setup.aiDepth = 4;
					setup.timeSecs = 0;
					setup.incrementSecs = 0;
					setup.fenAI = "";
					setup.fenOTB = fenOTB.getText();
					Main.startGame(setup);
				}
			});
			quickAI4 = new Button(buttonCol, buttonHoverCol, "Depth 5\nIntermediate", buttonTextCol);
			quickAI4.setStroke(borderCol);
			quickAI4.text.setTextAlignment(TextAlignment.CENTER);
			quickAI4.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					setup.singlePlayer = true;
					setup.aiDepth = 5;
					setup.timeSecs = 0;
					setup.incrementSecs = 0;
					setup.fenAI = "";
					setup.fenOTB = fenOTB.getText();
					Main.startGame(setup);
				}
			});
			
			fenOTB = new TextField();
			fenOTB.setPromptText("Starting position FEN");
			fenOTB.setText(setup.fenOTB);
			fenOTB.setStyle("-fx-text-fill: " + Utilities.colToHex(buttonTextCol) + "; -fx-prompt-text-fill: " + Utilities.colToHex(inactiveButtonTextCol));
			Utilities.installTooltip(fenOTB, "Valid FEN");
			if (setup.fenOTB.equals("")) Utilities.installTooltip(fenOTB, "If no valid FEN is entered, the standard starting position will be used");
			else if (Utilities.fenToBoard(setup.fenOTB) == null) {
				fenOTB.setStyle(fenOTB.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(warningTextCol));
				Utilities.installTooltip(fenOTB, "Invalid FEN");
			}
			fenOTB.setOnKeyTyped(event -> {
				if (Utilities.fenToBoard(fenOTB.getText().strip()) == null) {
					if (fenOTB.getText().strip().equals("")) {
						fenOTB.setStyle(fenOTB.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(buttonTextCol));
						Utilities.installTooltip(fenOTB, "If no valid FEN is entered, the standard starting position will be used");
					} else {
						fenOTB.setStyle(fenOTB.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(warningTextCol));
						Utilities.installTooltip(fenOTB, "Invalid FEN");
					}
				} else {
					fenOTB.setStyle(fenOTB.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(buttonTextCol));
					Utilities.installTooltip(fenOTB, "Valid FEN");
				}
			});
			fenOTB.focusedProperty().addListener(event -> {  // Highlight all text when first clicked onto
				if (fenOTB.isFocused()) {
					fenOTB.selectAll();
					fenOTB.setOnMouseReleased(event2 -> {
						fenOTB.selectAll();
						fenOTB.setOnMouseReleased(e -> {});
					});
				}
			});
			
			fenAI = new TextField();
			fenAI.setPromptText("Starting position FEN");
			fenAI.setText(setup.fenAI);
			fenAI.setStyle("-fx-text-fill: " + Utilities.colToHex(buttonTextCol) + "; -fx-prompt-text-fill: " + Utilities.colToHex(inactiveButtonTextCol));
			Utilities.installTooltip(fenAI, "Valid FEN");
			if (setup.fenAI.equals("")) Utilities.installTooltip(fenAI, "If no valid FEN is entered, the standard starting position will be used");
			else if (Utilities.fenToBoard(setup.fenAI) == null) {
				fenAI.setStyle(fenAI.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(warningTextCol));
				Utilities.installTooltip(fenAI, "Invalid FEN");
			}
			fenAI.setOnKeyTyped(event -> {
				if (Utilities.fenToBoard(fenAI.getText().strip()) == null) {
					if (fenAI.getText().strip().equals("")) {
						fenAI.setStyle(fenAI.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(buttonTextCol));
						Utilities.installTooltip(fenAI, "If no valid FEN is entered, the standard starting position will be used");
					} else {
						fenAI.setStyle(fenAI.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(warningTextCol));
						Utilities.installTooltip(fenAI, "Invalid FEN");
					}
				} else {
					fenAI.setStyle(fenAI.getStyle() + "; -fx-text-fill: " + Utilities.colToHex(buttonTextCol));
					Utilities.installTooltip(fenAI, "Valid FEN");
				}
			});
			fenAI.focusedProperty().addListener(event -> {  // Highlight all text when first clicked onto
				if (fenAI.isFocused()) {
					fenAI.selectAll();
					fenAI.setOnMouseReleased(event2 -> {
						fenAI.selectAll();
						fenAI.setOnMouseReleased(e -> {});
					});
				}
			});
			
			root.getChildren().addAll(playOTB, playOTB.text, playAI, playAI.text, decreaseDepth, decreaseDepth.text, increaseDepth, increaseDepth.text, playAsButton, playAsButton.imageView, quickOTB1, quickOTB1.text, quickOTB2, quickOTB2.text, quickOTB3, quickOTB3.text, quickOTB4, quickOTB4.text, quickOTB5, quickOTB5.text, quickOTB6, quickOTB6.text, decreaseTimeMins, decreaseTimeMins.text, increaseTimeMins, increaseTimeMins.text, decreaseTimeSecs, decreaseTimeSecs.text, increaseTimeSecs, increaseTimeSecs.text, decreaseIncrementSecs, decreaseIncrementSecs.text, increaseIncrementSecs, increaseIncrementSecs.text, quickAI1, quickAI1.text, quickAI2, quickAI2.text, quickAI3, quickAI3.text, quickAI4, quickAI4.text, fenOTB, fenAI);
			
			Utilities.installTooltip(playOTB, "Play against a friend");
			Utilities.installTooltip(playAI, "Play against the computer");
			Utilities.installTooltip(decreaseDepth, "Decrease the AI depth (minimum 1)");
			Utilities.installTooltip(increaseDepth, "Increase the AI depth (maximum 6)");
			Utilities.installTooltip(playAsButton, "Random, then alternating");
			Utilities.installTooltip(decreaseTimeMins, "Decrease the minutes for each player (hold Ctrl to decrease by 10)");
			Utilities.installTooltip(increaseTimeMins, "Increase the minutes for each player (hold Ctrl to increase by 10)");
			Utilities.installTooltip(decreaseTimeSecs, "Decrease the seconds for each player (hold Ctrl to decrease by 10)");
			Utilities.installTooltip(increaseTimeSecs, "Increase the seconds for each player (hold Ctrl to increase by 10)");
			Utilities.installTooltip(decreaseIncrementSecs, "Decrease the seconds of increment for each player (hold Ctrl to decrease by 10)");
			Utilities.installTooltip(increaseIncrementSecs, "Increase the seconds of increment for each player (hold Ctrl to increase by 10)");
		}
		
		void resize() {
			playOTB.setWidth(playWidth);
			playOTB.setHeight(playHeight);
			playOTB.relocate(playOTBStartX, playStartY);
			playOTB.text.setFont(Font.font("Arial Rounded MT Bold", playHeight / 2));
			playOTB.repositionText();
			playOTB.setArcWidth(playHeight / 2);
			playOTB.setArcHeight(playHeight / 2);
			playOTB.setStrokeWidth(strokeWidth);
			
			playAI.setWidth(playWidth);
			playAI.setHeight(playHeight);
			playAI.relocate(playAIStartX, playStartY);
			playAI.text.setFont(Font.font("Arial Rounded MT Bold", playHeight / 2));
			playAI.repositionText();
			playAI.setArcWidth(playHeight / 2);
			playAI.setArcHeight(playHeight / 2);
			playAI.setStrokeWidth(strokeWidth);
			
			decreaseDepth.setWidth(depthButtonWidth);
			decreaseDepth.setHeight(depthButtonHeight);
			decreaseDepth.relocate(customAIColumn1StartX + graphics.aiDepth.getLayoutBounds().getWidth() + smallGap, customFirstLineY - depthButtonHeight / 2);
			decreaseDepth.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			decreaseDepth.repositionText();
			
			increaseDepth.setWidth(depthButtonWidth);
			increaseDepth.setHeight(depthButtonHeight);
			increaseDepth.relocate(customAIColumn1StartX + graphics.aiDepth.getLayoutBounds().getWidth() + smallGap + depthButtonWidth + graphics.aiDepthValue.getLayoutBounds().getWidth(), customFirstLineY - depthButtonHeight / 2);
			increaseDepth.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			increaseDepth.repositionText();
			
			playAsButton.setWidth(playAsIconSize);
			playAsButton.setHeight(playAsIconSize);
			playAsButton.relocate(customAIColumn2StartX + graphics.playAs.getLayoutBounds().getWidth() + smallGap, customFirstLineY - playAsIconSize / 2);
			playAsButton.imageView.setFitWidth(playAsIconSize);
			playAsButton.imageView.setFitHeight(playAsIconSize);
			playAsButton.repositionImage();
			playAsButton.setArcWidth(normalTextSize);
			playAsButton.setArcHeight(normalTextSize);
			playAsButton.setStrokeWidth(strokeWidth);
			
			quickOTB1.setWidth(quickOTBWidth);
			quickOTB1.setHeight(quickOTBHeight);
			quickOTB1.relocate(panelOTBStartX + panelMargin, panelStartY + titleAreaHeight + panelMargin);
			quickOTB1.text.setFont(Font.font("Arial Rounded MT Bold", quickOTBHeight / 4));
			quickOTB1.repositionText();
			quickOTB1.setArcWidth(quickOTBHeight / 2);
			quickOTB1.setArcHeight(quickOTBHeight / 2);
			quickOTB1.setStrokeWidth(strokeWidth);
			
			quickOTB2.setWidth(quickOTBWidth);
			quickOTB2.setHeight(quickOTBHeight);
			quickOTB2.relocate(panelOTBStartX + panelMargin * 2 + quickOTBWidth, panelStartY + titleAreaHeight + panelMargin);
			quickOTB2.text.setFont(Font.font("Arial Rounded MT Bold", quickOTBHeight / 4));
			quickOTB2.repositionText();
			quickOTB2.setArcWidth(quickOTBHeight / 2);
			quickOTB2.setArcHeight(quickOTBHeight / 2);
			quickOTB2.setStrokeWidth(strokeWidth);
			
			quickOTB3.setWidth(quickOTBWidth);
			quickOTB3.setHeight(quickOTBHeight);
			quickOTB3.relocate(panelOTBStartX + panelMargin * 3 + quickOTBWidth * 2, panelStartY + titleAreaHeight + panelMargin);
			quickOTB3.text.setFont(Font.font("Arial Rounded MT Bold", quickOTBHeight / 4));
			quickOTB3.repositionText();
			quickOTB3.setArcWidth(quickOTBHeight / 2);
			quickOTB3.setArcHeight(quickOTBHeight / 2);
			quickOTB3.setStrokeWidth(strokeWidth);
			
			quickOTB4.setWidth(quickOTBWidth);
			quickOTB4.setHeight(quickOTBHeight);
			quickOTB4.relocate(panelOTBStartX + panelMargin, panelStartY + titleAreaHeight + panelMargin * 2 + quickOTBHeight);
			quickOTB4.text.setFont(Font.font("Arial Rounded MT Bold", quickOTBHeight / 4));
			quickOTB4.repositionText();
			quickOTB4.setArcWidth(quickOTBHeight / 2);
			quickOTB4.setArcHeight(quickOTBHeight / 2);
			quickOTB4.setStrokeWidth(strokeWidth);
			
			quickOTB5.setWidth(quickOTBWidth);
			quickOTB5.setHeight(quickOTBHeight);
			quickOTB5.relocate(panelOTBStartX + panelMargin * 2 + quickOTBWidth, panelStartY + titleAreaHeight + panelMargin * 2 + quickOTBHeight);
			quickOTB5.text.setFont(Font.font("Arial Rounded MT Bold", quickOTBHeight / 4));
			quickOTB5.repositionText();
			quickOTB5.setArcWidth(quickOTBHeight / 2);
			quickOTB5.setArcHeight(quickOTBHeight / 2);
			quickOTB5.setStrokeWidth(strokeWidth);
			
			quickOTB6.setWidth(quickOTBWidth);
			quickOTB6.setHeight(quickOTBHeight);
			quickOTB6.relocate(panelOTBStartX + panelMargin * 3 + quickOTBWidth * 2, panelStartY + titleAreaHeight + panelMargin * 2 + quickOTBHeight);
			quickOTB6.text.setFont(Font.font("Arial Rounded MT Bold", quickOTBHeight / 4));
			quickOTB6.repositionText();
			quickOTB6.setArcWidth(quickOTBHeight / 2);
			quickOTB6.setArcHeight(quickOTBHeight / 2);
			quickOTB6.setStrokeWidth(strokeWidth);
			
			decreaseTimeMins.setWidth(timeButtonWidth);
			decreaseTimeMins.setHeight(timeButtonHeight);
			decreaseTimeMins.relocate(customOTBColumn1StartX + graphics.time.getLayoutBounds().getWidth() + smallGap, customFirstLineY + normalTextSize / 2);
			decreaseTimeMins.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			decreaseTimeMins.repositionText();
			
			increaseTimeMins.setWidth(timeButtonWidth);
			increaseTimeMins.setHeight(timeButtonHeight);
			increaseTimeMins.relocate(customOTBColumn1StartX + graphics.time.getLayoutBounds().getWidth() + smallGap, customFirstLineY - normalTextSize / 2 - timeButtonHeight);
			increaseTimeMins.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			increaseTimeMins.repositionText();
			
			decreaseTimeSecs.setWidth(timeButtonWidth);
			decreaseTimeSecs.setHeight(timeButtonHeight);
			decreaseTimeSecs.relocate(customOTBColumn1StartX + graphics.time.getLayoutBounds().getWidth() + smallGap + timeButtonWidth + strokeWidth * 2 + graphics.colon.getLayoutBounds().getWidth(), customFirstLineY + normalTextSize / 2);
			decreaseTimeSecs.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			decreaseTimeSecs.repositionText();
			
			increaseTimeSecs.setWidth(timeButtonWidth);
			increaseTimeSecs.setHeight(timeButtonHeight);
			increaseTimeSecs.relocate(customOTBColumn1StartX + graphics.time.getLayoutBounds().getWidth() + smallGap + timeButtonWidth + strokeWidth * 2 + graphics.colon.getLayoutBounds().getWidth(), customFirstLineY - normalTextSize / 2 - timeButtonHeight);
			increaseTimeSecs.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			increaseTimeSecs.repositionText();
			
			decreaseIncrementSecs.setWidth(timeButtonWidth);
			decreaseIncrementSecs.setHeight(timeButtonHeight);
			decreaseIncrementSecs.relocate(customOTBColumn2StartX + graphics.increment.getLayoutBounds().getWidth() + smallGap, customFirstLineY + normalTextSize / 2);
			decreaseIncrementSecs.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			decreaseIncrementSecs.repositionText();
			
			increaseIncrementSecs.setWidth(timeButtonWidth);
			increaseIncrementSecs.setHeight(timeButtonHeight);
			increaseIncrementSecs.relocate(customOTBColumn2StartX + graphics.increment.getLayoutBounds().getWidth() + smallGap, customFirstLineY - normalTextSize / 2 - timeButtonHeight);
			increaseIncrementSecs.text.setFont(Font.font("Arial Rounded MT Bold", normalTextSize));
			increaseIncrementSecs.repositionText();
			
			quickAI1.setWidth(quickAIWidth);
			quickAI1.setHeight(quickAIHeight);
			quickAI1.relocate(panelAIStartX + panelMargin, panelStartY + titleAreaHeight + panelMargin);
			quickAI1.text.setFont(Font.font("Arial Rounded MT Bold", quickAIHeight / 4));
			quickAI1.repositionText();
			quickAI1.setArcWidth(quickAIHeight / 2);
			quickAI1.setArcHeight(quickAIHeight / 2);
			quickAI1.setStrokeWidth(strokeWidth);
			
			quickAI2.setWidth(quickAIWidth);
			quickAI2.setHeight(quickAIHeight);
			quickAI2.relocate(panelAIStartX + panelMargin * 2 + quickAIWidth, panelStartY + titleAreaHeight + panelMargin);
			quickAI2.text.setFont(Font.font("Arial Rounded MT Bold", quickAIHeight / 4));
			quickAI2.repositionText();
			quickAI2.setArcWidth(quickAIHeight / 2);
			quickAI2.setArcHeight(quickAIHeight / 2);
			quickAI2.setStrokeWidth(strokeWidth);
			
			quickAI3.setWidth(quickAIWidth);
			quickAI3.setHeight(quickAIHeight);
			quickAI3.relocate(panelAIStartX + panelMargin, panelStartY + titleAreaHeight + panelMargin * 2 + quickAIHeight);
			quickAI3.text.setFont(Font.font("Arial Rounded MT Bold", quickAIHeight / 4));
			quickAI3.repositionText();
			quickAI3.setArcWidth(quickAIHeight / 2);
			quickAI3.setArcHeight(quickAIHeight / 2);
			quickAI3.setStrokeWidth(strokeWidth);
			
			quickAI4.setWidth(quickAIWidth);
			quickAI4.setHeight(quickAIHeight);
			quickAI4.relocate(panelAIStartX + panelMargin * 2 + quickAIWidth, panelStartY + titleAreaHeight + panelMargin * 2 + quickAIHeight);
			quickAI4.text.setFont(Font.font("Arial Rounded MT Bold", quickAIHeight / 4));
			quickAI4.repositionText();
			quickAI4.setArcWidth(quickAIHeight / 2);
			quickAI4.setArcHeight(quickAIHeight / 2);
			quickAI4.setStrokeWidth(strokeWidth);
			
			fenOTB.setPrefWidth(customAreaWidth - panelMargin * 2);
			fenOTB.setFont(Font.font("Arial Rounded MT Bold", normalTextSize * 0.55));
			fenOTB.relocate(customOTBColumn1StartX, customFirstLineY + customAreaHeight / 5);
			fenOTB.setBackground(new Background(new BackgroundFill(buttonCol, new CornerRadii(normalTextSize / 3.8), null)));
			fenOTB.setBorder(new Border(new BorderStroke(borderCol, BorderStrokeStyle.SOLID, new CornerRadii(normalTextSize / 4), new BorderWidths(strokeWidth))));
			
			fenAI.setPrefWidth(customAreaWidth - panelMargin * 2);
			fenAI.setFont(Font.font("Arial Rounded MT Bold", normalTextSize * 0.55));
			fenAI.relocate(customAIColumn1StartX, customFirstLineY + customAreaHeight / 5);
			fenAI.setBackground(new Background(new BackgroundFill(buttonCol, new CornerRadii(normalTextSize / 3.8), null)));
			fenAI.setBorder(new Border(new BorderStroke(borderCol, BorderStrokeStyle.SOLID, new CornerRadii(normalTextSize / 4), new BorderWidths(strokeWidth))));
		}
	}
}
