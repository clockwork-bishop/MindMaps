package mindmaps;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 * Tato třída se stará o správu dialogu pro tvorbu (a zároveň editaci) grafického myšlenkového uzlu - tzn. bere vlastnosti uzlu (tvar, barva, text, ...)
 * zadané od uživatele a shromáždí je všechny do objektu třídy MindNodeProfile, který je pak k dispozici pro metodu, která vyvolala dialog, jako výsledek.
 */
public class MindNodeDialogController implements Initializable {

	//stage kontrolovaného okna
	private final Stage mindNodeDialogStage;
	
	//model pro ChoiceBox
	ObservableList<String> shapeItems = FXCollections.observableArrayList("Circle", "Ellipse", "Rectangle");
	
	//vstupní textové pole pro jméno
	@FXML private TextField nameTextField;
	
	//výběr barev
	@FXML private ColorPicker colorPicker;
	
	//výběr tvaru
	@FXML private ChoiceBox choiceShape;
	
	//výběr barvy textu
	@FXML private CheckBox blackChoice;
	@FXML private CheckBox whiteChoice;
	
	//textové pole pro poznámku k mindNodu
	@FXML private TextArea noteTextArea;
	
	//profil se všemi vlastnostmi zadanými od uživatele
	private MindNodeProfile mindNodeProfile;
	
	//konstruktor 1 (nový mind node)
	public MindNodeDialogController(Stage stage) {
		mindNodeDialogStage = stage;
	}
	
	//konstruktor 2 (editace mind node)
	public MindNodeDialogController(Stage stage, MindNode mindNode) {
		mindNodeDialogStage = stage;
		mindNodeProfile = mindNode.getMindNodeProfile();
	}
	
	//AKCE
	@FXML public void okAction() {
		//pokud uživatel nic nezadal a stiskl OK, vyskočí varování
		if (nameTextField.getText().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Field is empty");
			alert.show();
		}
		else {
			//vytvoříme mindNodeProfile, kde budou pohromadě nastavené všechny zadané vlastnosti
			mindNodeProfile = new MindNodeProfile(nameTextField.getText(), 
												  colorPicker.getValue(),
											      getTickedTextColor(),
												  stringToShape(choiceShape.getValue().toString()),
												  noteTextArea.getText());
			mindNodeDialogStage.close();
		}
	}
	
	@FXML public void cancelAction() {
		mindNodeDialogStage.close();
	}
	
	//metody, které zajistí, že je vybrán jen jeden CheckBox
	@FXML public void tickBlack() {
		whiteChoice.setSelected(false);
	}
	@FXML public void tickWhite() {
		blackChoice.setSelected(false);
	}
	
	//GETTERY
	public MindNodeProfile getResultProfile() {
		return mindNodeProfile;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//editace
		if (mindNodeProfile != null) {
			choiceShape.setItems(shapeItems); //nastavení prvků v ChoiceBox
			choiceShape.setValue(shapeToObservableItem(mindNodeProfile.getShape()));
			colorPicker.setValue(mindNodeProfile.getColor());
			tickInitTextColor(mindNodeProfile.getTextColor());
			nameTextField.setText(mindNodeProfile.getName());
			noteTextArea.setText(mindNodeProfile.getNote());
		}
		//nový uzel
		else {
			blackChoice.setSelected(true); //výchozí barva textu je černá
			choiceShape.setItems(shapeItems); //nastavení tvarů v ChoiceBox
			choiceShape.getSelectionModel().selectFirst(); //nastavení výchozí hodnoty ChoiceBoxu
			colorPicker.setValue(Color.color(Math.random(), Math.random(), Math.random()));
		}
	}
	
	//POMOCNÉ METODY
	private Shape stringToShape(String input) {
		if (input.equals(shapeItems.get(0))) {
			Circle circle = new Circle();
			return circle;
		}
		else if (input.equals(shapeItems.get(1))) {
			Ellipse ellipse = new Ellipse();
			return ellipse;
			
		}
		else if (input.equals(shapeItems.get(2))) {
			Rectangle rectangle = new Rectangle();
			return rectangle;
		}
		return null;
	}
	
	//metoda na základě vstupního objektu typu Shape vrátí prvek ObservableListu, který je modelem pro ChoiceBox
	private String shapeToObservableItem(Shape input) {
		if (input instanceof Circle)
			return shapeItems.get(0);
		else if (input instanceof Ellipse)
			return shapeItems.get(1);
		else return shapeItems.get(2);
	}
	
	//získá barvu textu popisku v nodu zvolenou od uživatele (pokud by nezvolil žádnou, výchozí je černá)
	private Color getTickedTextColor() {
		if (whiteChoice.isSelected())
			return Color.WHITE;
		else return Color.BLACK; 
	}
	
	//inicializace barvy při editaci nodu
	private void tickInitTextColor(Color color) {
		if (color.equals(Color.WHITE)) 
			whiteChoice.setSelected(true);
		else blackChoice.setSelected(true);
	}
}