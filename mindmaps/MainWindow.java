package mindmaps;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;

/**
 * @author Denisa Nováková Andrýsková
 */
public class MainWindow extends Application {

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		//loader fxml dokumentu
		FXMLLoader loader =  new FXMLLoader(getClass().getResource("/sources/FXMLMainWindow.fxml"));
		
		//hlavní panel (= BorderPane)
		Parent mainPanel = loader.load();

		//provázání controlleru MainWindow a jeho primaryStage
		MainWindowController mainWindowController = loader.getController();
		mainWindowController.setMainWindowStage(primaryStage);
		
		//nastavení scény
		Scene scene = new Scene(mainPanel, 1200, 900, false, SceneAntialiasing.BALANCED);
		scene.getStylesheets().add("/sources/CSSMainWindow.css");
		
		//vlastnosti tohoto okna
		primaryStage.setTitle("Mind Maps");             
        primaryStage.setScene(scene);
        primaryStage.show();
	}
}
