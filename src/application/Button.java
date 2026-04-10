package application;

import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

class Button extends Rectangle {
	Text text;
	ImageView imageView;
	Color baseCol;
	Color textCol;
	Color inactiveTextCol;
	boolean active;
	
	Button(Color baseColour, Color hoverColour, String buttonText, Color textColour) {
		super(0, 0, baseColour);
		text = new Text(buttonText);
		text.setMouseTransparent(true);  // Cannot click on text, only rectangle beneath
		text.setFill(textColour);
		text.setTextOrigin(VPos.CENTER);
		active = true;
		
		setCursor(Cursor.HAND);
		
		setOnMouseEntered(event -> {
			if (active) setFill(hoverColour);
		});
		
		setOnMouseExited(event -> setFill(baseColour));
	}
	
	Button(Color baseColour, Color hoverColour, String buttonText, Color textColour, Color inactiveTextColour) {
		super(0, 0, baseColour);
		baseCol = baseColour;
		textCol = textColour;
		inactiveTextCol = inactiveTextColour;
		text = new Text(buttonText);
		text.setMouseTransparent(true);  // Cannot click on text, only rectangle beneath
		text.setFill(textColour);
		text.setTextOrigin(VPos.CENTER);
		active = true;
		
		setCursor(Cursor.HAND);
		
		setOnMouseEntered(event -> {
			if (active) setFill(hoverColour);
		});
		
		setOnMouseExited(event -> setFill(baseColour));
	}
	
	Button(Color baseColour, Color hoverColour, Image image) {
		super(0, 0, baseColour);
		imageView = new ImageView(image);
		imageView.setMouseTransparent(true);
		
		setCursor(Cursor.HAND);
		
		setOnMouseEntered(event -> setFill(hoverColour));
		
		setOnMouseExited(event -> setFill(baseColour));
	}
	
	void activate() {
		active = true;
		text.setFill(textCol);
		setCursor(Cursor.HAND);
	}
	
	void deactivate() {
		active = false;
		setFill(baseCol);
		text.setFill(inactiveTextCol);
		setCursor(Cursor.DEFAULT);
	}
	
	void repositionText() {  // Must be called after being resized and the rectangle repositioned
		text.setX(getLayoutX() + getWidth() / 2 - text.getLayoutBounds().getWidth() / 2);
		text.setY(getLayoutY() + getHeight() / 2);
	}
	
	void repositionImage() {  // Must be called after being resized and the rectangle repositioned
		imageView.setX(getLayoutX() + getWidth() / 2 - imageView.getFitWidth() / 2);
		imageView.setY(getLayoutY() + getHeight() / 2 - imageView.getFitHeight() / 2);
	}
}
