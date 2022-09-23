package mindmaps;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Třída spravuje dialog pro vytvoření nové mapy.
 */
public class NewMapDialogController {
	
	//stage kontrolovaného okna
	private final Stage newMapDialogStage;
	
	//jméno mapy zadané od uživatele
	private String resultName;
	
	//textové vstupní pole
	@FXML private TextField mapNameTextField;
	
	//konstruktor
	public NewMapDialogController(Stage stage) {
		newMapDialogStage = stage;
	}
	
	//AKCE
	@FXML public void okAction() {
		resultName = mapNameTextField.getText();
		
		//pokud uživatel nezadal jméno a stiskl OK, vyskočí varování
		if (resultName.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Field 'Name' is empty");
			alert.show();
		}
		else newMapDialogStage.close();
	}
	
	@FXML public void cancelAction() {
		newMapDialogStage.close();
	}
	
	//GETTERY
	public String getResult() {
		return resultName;
	}
}