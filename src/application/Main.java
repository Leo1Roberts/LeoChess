package application;

import static application.Settings.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class Main extends Application {
	static Scanner inp;
	static Scene scene;
	static Entry entry;
	static Game game;
	
	@Override
	public void start(Stage stage) {
		try {
			Group entryRoot = new Group();
			Group gameRoot = new Group();
			
			scene = new Scene(entryRoot, 1280, 720);
			
			Settings.loadSettings("assets/settings.txt");
			
			if (Files.notExists(Path.of("assets/pieces/" + piecesFolder))) {
				Alert alert = new Alert(AlertType.ERROR, "Pieces folder \"" + piecesFolder + "\" not found");
				alert.showAndWait();
				System.exit(0);
			} else {
				String missingPiece = "";
				
				if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Pawn W.png"))) missingPiece = "Pawn W";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Pawn B.png"))) missingPiece = "Pawn B";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Knight W.png"))) missingPiece = "Knight W";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Knight B.png"))) missingPiece = "Knight B";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Bishop W.png"))) missingPiece = "Bishop W";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Bishop B.png"))) missingPiece = "Bishop B";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Rook W.png"))) missingPiece = "Rook W";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Rook B.png"))) missingPiece = "Rook B";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Queen W.png"))) missingPiece = "Queen W";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/Queen B.png"))) missingPiece = "Queen B";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/King W.png"))) missingPiece = "King W";
				else if (Files.notExists(Path.of("assets/pieces/" + piecesFolder + "/King B.png"))) missingPiece = "King B";
				
				if (!missingPiece.equals("")) {
					Alert alert = new Alert(AlertType.ERROR, "Piece image file \"" + missingPiece + ".png\" not found");
					alert.showAndWait();
					System.exit(0);
				}
			}
			
			entry = new Entry(scene, entryRoot);
			entry.init();
			entry.root.requestFocus();
			game = new Game(scene, gameRoot, inp);
			
			// Redraw everything after a window resize
			scene.widthProperty().addListener(event -> {
				if (scene.getRoot() == entryRoot) entry.resizeAll();
				else game.resizeAll();
			});
			scene.heightProperty().addListener(event -> {
				if (scene.getRoot() == entryRoot) entry.resizeAll();
				else game.resizeAll();
			});
			
			// For multiple-key shortcuts
			final KeyCombination alt_enter = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN);
			final KeyCombination shift_esc = new KeyCodeCombination(KeyCode.ESCAPE, KeyCombination.SHIFT_DOWN);
			final KeyCombination left = new KeyCodeCombination(KeyCode.LEFT);
			final KeyCombination right = new KeyCodeCombination(KeyCode.RIGHT);
			final KeyCombination ctrl_left = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
			final KeyCombination ctrl_right = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
			
			stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {  // Keyboard shortcuts
				// Program-wide shortcuts
				if (KeyCode.F11.equals(event.getCode())) stage.setFullScreen(!stage.isFullScreen());
				else if (alt_enter.match(event)) stage.setFullScreen(!stage.isFullScreen());
				else if (shift_esc.match(event)) {
					Alert alert = new Alert(AlertType.CONFIRMATION, "Close window? All progress will be lost.");
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						System.exit(0);
					}
				}
				
				if (scene.getRoot() == gameRoot) {  // Game screen shortcuts
					if (KeyCode.F.equals(event.getCode())) game.flip();
					else if (KeyCode.R.equals(event.getCode())) game.reset();
					else if (left.match(event)) game.navBackward();
					else if (right.match(event)) game.navForward();
					else if (ctrl_left.match(event)) game.navToStart();
					else if (ctrl_right.match(event)) game.navToEnd();
					else if (KeyCode.UP.equals(event.getCode())) game.navToStart();
					else if (KeyCode.DOWN.equals(event.getCode())) game.navToEnd();
					else if (KeyCode.BACK_SPACE.equals(event.getCode())) game.undoMove();
				}
			});
			
			stage.setFullScreenExitHint("");  // No need for a message to tell the user how to exit fullscreen mode
			stage.setTitle("LeoChess️");
			stage.getIcons().add(new Image("file:assets/pieces/" + piecesFolder + "/King W.png"));
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	static void startGame(GameSetup setup) {
		game.init(setup);
		scene.setRoot(game.root);
	}
	
	static void loadEntry(GameSetup setup) {
		entry.init(setup);
		scene.setRoot(entry.root);
		entry.root.requestFocus();
	}
	
	public static void main(String[] args) {
		inp = new Scanner(System.in);
		launch(args);
		inp.close();
	}
}