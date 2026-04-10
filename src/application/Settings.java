package application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.paint.Color;

class Settings {
	static String filePath;
	
	static boolean autoFlip = true;
	static boolean highlightPreviousMove = true;
	static boolean showLegalMoves = true;
	static boolean animateMoves = true;
	static boolean coordinatesOutsideBoard = true;
	static boolean showTooltips = true;
	static boolean hideBoardTooltips = true;
	static boolean allowTakingBackMoves = true;
	static boolean allowMoveSuggestions = true;
	
	static String piecesFolder = "modern";
	
	static Color darkSquareCol;
	static Color lightSquareCol;
	static Color darkSquareHighlightCol;
	static Color lightSquareHighlightCol;
	static Color darkSquareSelectionCol;
	static Color lightSquareSelectionCol;
	static Color darkSquareCaptureCol;
	static Color lightSquareCaptureCol;
	static Color darkSquareCheckCol;
	static Color lightSquareCheckCol;
	static Color voidFillerCol;
	static Color backingCardCol;
	static Color panelCol1;
	static Color panelCol2;
	static Color textCol;
	static Color buttonCol;
	static Color buttonHoverCol;
	static Color buttonTextCol;
	static Color inactiveButtonTextCol;
	static Color warningTextCol;
	static Color moveListHighlightCol;
	static Color moveListHoverCol;
	static Color playButtonCol;
	static Color playButtonHoverCol;
	static Color playButtonTextCol;
	static Color boardMarginTextCol;
	static Color borderCol;
	
	static void loadSettings(String pathName) {
		filePath = pathName;
		loadSettings();
	}
	
	static void loadSettings() {
		try {
			String fileStr = Files.readString(Path.of(filePath));
			fileStr = fileStr.toLowerCase().replaceAll(" ", "") +'\n';
			
			int i = 0;
			String name = "";
			String setting = "";
			boolean onName = true;
			while (i < fileStr.length()) {
				char c = fileStr.charAt(i);
				if (c == '\n' || c == '\r') {
					if (name != "" && setting != "") {
						switch (name) {
						case "autoflipboard":
							autoFlip = toBoolean(setting);
							break;
						case "highlightpreviousmove":
							highlightPreviousMove = toBoolean(setting);
							break;
						case "showlegalmoves":
							showLegalMoves = toBoolean(setting);
							break;
						case "animatemoves":
							animateMoves = toBoolean(setting);
							break;
						case "coordinatesoutsideboard":
							coordinatesOutsideBoard = toBoolean(setting);
							break;
						case "showtooltips":
							showTooltips = toBoolean(setting);
							break;
						case "hideboardtooltips":
							hideBoardTooltips = toBoolean(setting);
							break;
						case "allowtakingbackmoves":
							allowTakingBackMoves = toBoolean(setting);
							break;
						case "allowmovesuggestions":
							allowMoveSuggestions = toBoolean(setting);
							break;
						case "piecesfolder":
							piecesFolder = setting;
							break;
						case "darksquarecolour":
							darkSquareCol = toColour(setting);
							break;
						case "lightsquarecolour":
							lightSquareCol = toColour(setting);
							break;
						case "darksquarehighlightcolour":
							darkSquareHighlightCol = toColour(setting);
							break;
						case "lightsquarehighlightcolour":
							lightSquareHighlightCol = toColour(setting);
							break;
						case "darksquareselectioncolour":
							darkSquareSelectionCol = toColour(setting);
							break;
						case "lightsquareselectioncolour":
							lightSquareSelectionCol = toColour(setting);
							break;
						case "darksquarecapturecolour":
							darkSquareCaptureCol = toColour(setting);
							break;
						case "lightsquarecapturecolour":
							lightSquareCaptureCol = toColour(setting);
							break;
						case "darksquarecheckcolour":
							darkSquareCheckCol = toColour(setting);
							break;
						case "lightsquarecheckcolour":
							lightSquareCheckCol = toColour(setting);
							break;
						case "voidfillercolour":
							voidFillerCol = toColour(setting);
							break;
						case "backingcardcolour":
							backingCardCol = toColour(setting);
							break;
						case "panelcolour1":
							panelCol1 = toColour(setting);
							break;
						case "panelcolour2":
							panelCol2 = toColour(setting);
							break;
						case "textcolour":
							textCol = toColour(setting);
							break;
						case "buttoncolour":
							buttonCol = toColour(setting);
							break;
						case "buttonhovercolour":
							buttonHoverCol = toColour(setting);
							break;
						case "buttontextcolour":
							buttonTextCol = toColour(setting);
							break;
						case "inactivebuttontextcolour":
							inactiveButtonTextCol = toColour(setting);
							break;
						case "warningtextcolour":
							warningTextCol = toColour(setting);
							break;
						case "movelisthighlightcolour":
							moveListHighlightCol = toColour(setting);
							break;
						case "movelisthovercolour":
							moveListHoverCol = toColour(setting);
							break;
						case "playbuttoncolour":
							playButtonCol = toColour(setting);
							break;
						case "playbuttonhovercolour":
							playButtonHoverCol = toColour(setting);
							break;
						case "playbuttontextcolour":
							playButtonTextCol = toColour(setting);
							break;
						case "boardmargintextcolour":
							boardMarginTextCol = toColour(setting);
							break;
						case "bordercolour":
							borderCol = toColour(setting);
							break;
						}
					}
					name = "";
					setting = "";
					onName = true;
				} else if (c == ':') {
					onName = false;
				} else {
					if (onName) name += c;
					else setting += c;
				}
				i++;
			}
		} catch (IOException e) {
			System.out.println("Settings file not found - default settings apply");
		}
		
		if (darkSquareCol == null) darkSquareCol = Color.web("b58863");
		if (lightSquareCol == null) lightSquareCol = Color.web("f0d9b5");
		if (darkSquareHighlightCol == null) darkSquareHighlightCol = Color.web("aaa23a");
		if (lightSquareHighlightCol == null) lightSquareHighlightCol = Color.web("cdd26a");
		if (darkSquareSelectionCol == null) darkSquareSelectionCol = Color.web("4682b4");
		if (lightSquareSelectionCol == null) lightSquareSelectionCol = Color.web("84accd");
		if (darkSquareCaptureCol == null) darkSquareCaptureCol = Color.web("ff4500");
		if (lightSquareCaptureCol == null) lightSquareCaptureCol = Color.web("ff8300");
		if (darkSquareCheckCol == null) darkSquareCheckCol = Color.web("dc143c");
		if (lightSquareCheckCol == null) lightSquareCheckCol = Color.web("dc143c");
		if (voidFillerCol == null) voidFillerCol = Color.web("808080");
		if (backingCardCol == null) backingCardCol = Color.web("d3d3d3");
		if (panelCol1 == null) panelCol1 = Color.web("a9a9a9");
		if (panelCol2 == null) panelCol2 = Color.web("bebebe");
		if (textCol == null) textCol = Color.web("000000");
		if (buttonCol == null) buttonCol = Color.web("808080");
		if (buttonHoverCol == null) buttonHoverCol = Color.web("666666");
		if (buttonTextCol == null) buttonTextCol = Color.web("ffffff");
		if (inactiveButtonTextCol == null) inactiveButtonTextCol = Color.web("bbbbbb");
		if (warningTextCol == null) warningTextCol = Color.web("ffa0a0");
		if (moveListHighlightCol == null) moveListHighlightCol = Color.web("929292");
		if (moveListHoverCol == null) moveListHoverCol = Color.web("808080");
		if (playButtonCol == null) playButtonCol = Color.web("00b000");
		if (playButtonHoverCol == null) playButtonHoverCol = Color.web("00e000");
		if (playButtonTextCol == null) playButtonTextCol = Color.web("000000");
		if (borderCol == null) borderCol = Color.web("000000");
		if (boardMarginTextCol == null) boardMarginTextCol = Color.web("666666");
	}
	
	static Color toColour(String inp) {
		if (inp.charAt(0) == '#') inp = inp.substring(1, inp.length());
		try {
			return Color.web(inp);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	static boolean toBoolean(String inp) {
		if (inp.equals("no") || inp.equals("n") || inp.equals("false")) return false;
		return true;  // All on/off settings on by default
	}
}
