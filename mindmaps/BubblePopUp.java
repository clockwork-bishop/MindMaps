package mindmaps;

import javafx.event.EventDispatcher;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;

/**
 * Třída představuje vyskakovací uživatelskou poznámku ve tvaru dialogové bubliny, která příšluší určitému uzlu. Tato poznámka se objeví ve chvíli, kdy je zaznamenán
 * vstup kurzoru myši do příslušného uzlu (zmizí po vystoupení z uzlu). V případě, že je uzel označen (objeví se přerušovaný obdélník), poznámka nezmizí
 * ani při opuštění uzlu, takže je možné z ní text kopírovat nebo v něm scrollovat (zmizí zase při kliknutí mimo).
 * Jakékoliv eventy pro tento PopUp jsou zachytávány jeho rodičovským oknem (MainWindow)
 */
public class BubblePopUp extends Popup {
	
	private StackPane stackPane = new StackPane();
	private Rectangle bubbleRectangle = new Rectangle(200, 100);
	private String note = new String();
	private TextArea textArea = new TextArea(note);;
	
	public BubblePopUp() {
		//nastavení rectanglu s obrázkem bubliny
		Image img = new Image("/sources/bubble.png");
		bubbleRectangle.setFill(new ImagePattern(img));
		
		//nastavení stackPane, ve kterém bude umístěn textArea
		stackPane.setPadding(new Insets(10));
		
		//nastavení TextArea
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(180);
		textArea.setMaxHeight(63);
		textArea.getStylesheets().add("/sources/CSSBubblePopUp.css");
		stackPane.getChildren().add(textArea);
		
		//přidání do panu
		this.getContent().add(bubbleRectangle);
		this.getContent().add(stackPane);
	}
	
	public TextArea getTextArea() {
		return textArea;
	}
	
	public double getBubbleHeight() {
		return bubbleRectangle.getHeight();
	}
	
	public void setNote(String note) {
		this.note = note;
		textArea.setText(note);
	}
	
	//eventy, které bude zachytávat náš popUp se přepošlou jeho parentovi
	public void setNewDispatcher(EventDispatcher parentEventDispatcher) {
		this.setEventDispatcher(parentEventDispatcher);
	}
}
