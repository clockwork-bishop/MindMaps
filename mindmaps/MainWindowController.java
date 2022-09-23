package mindmaps;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

/**
 * Tato třída se stará o správu hlavního okna aplikace. Jsou v ní implementovány akce pro vytváření a manipulaci s mapami, dialogovými okny, kreslícím panelem a uzly.
 */
public class MainWindowController implements Initializable {

	//stage kontrolovaného okna
	public static Stage mainWindowStage;

	//asociační seznam názvů Tabů a k nim příslušných TreeViews
	private Map<String, TreeView> treeViewsList = new HashMap<>();

	//asociační seznam názvů Tabů a k nim příslušných Panes, v nichž pracujeme s mapou
	private Map<String, Pane> panesList = new HashMap<>();

	//asociační seznam stromů a cest k souborům, kde jsou uloženy (pokud už jednou uloženy byly) (nebude to dělat problémy později?)
	private Map<TreeView, File> fileDirectoriesList = new HashMap<>();

	//panel Tabů
	@FXML
	private TabPane tabPane;

	//hlavní panel okna
	@FXML
	private BorderPane mainWindowPane;

	public static TreeItem<MindNode> selectedMindTreeNode;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//vybraný tab bude možné zavřít
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
	}

	/**
	 * ***********************************
	 * AKCE PRO MANIPULACI S OKNEM
	 ************************************
	 */
	//zavření okna aplikace
	@FXML
	public void close() {
		mainWindowStage.close();
	}

	/**
	 * ***********************************
	 * AKCE PRO MANIPULACI S MAPAMI
	 ************************************
	 */
	//export do formátu .png
	@FXML
	public void exportAsPNG() throws IOException {

		//aktuální Tab
		Tab actualTab = tabPane.getSelectionModel().getSelectedItem();

		if (actualTab != null) {
			//jméno aktuálně vybraného tabu
			String actualTabName = actualTab.getText();

			//aktuální Pane, v němž pracujeme
			Pane actualPane = panesList.get(actualTabName);

			//dialog pro výběr cesty pro uložení
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Map");

			//defaultní cesta (=složka projektu)
			fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

			FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG file", ".png");
			fileChooser.getExtensionFilters().add(pngFilter);

			//vybraná cesta pro uložení
			File fileDirectory = fileChooser.showSaveDialog(mainWindowStage);

			if (fileDirectory != null) {
				//rozměry ctuálního Pane
				Double width = actualPane.getWidth();
				Double height = actualPane.getHeight();
				WritableImage writableImage = new WritableImage(width.intValue(), height.intValue());
				actualPane.snapshot(null, writableImage);

				//uložení do png formátu
				ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", fileDirectory);
			}
		}
	}

	//vytvoření nové mapy
	@FXML
	public void newMap() throws IOException {

		//pokud je zobrazena nějaká bublina -> skryjeme ji
		Tab actualTab = tabPane.getSelectionModel().getSelectedItem();
		if (actualTab != null) {
			hideBubble(treeViewsList.get(actualTab.getText()));
		}

		//vytvoření dialogu pro novou mapu
		NewMapDialogController dialogController = createNewMapDialog();

		//získání dat z dialogu
		String newMapName = dialogController.getResult();

		//vytvoření nové mapy včetně TreeView a Pane
		createNewMap(newMapName);
	}

	//uložení mapy
	@FXML
	public void saveMap() throws XMLStreamException {

		//aktuální Tab
		Tab actualTab = tabPane.getSelectionModel().getSelectedItem();

		//pokud je mapa výbec vytvořená (a Tab tedy existuje)
		if (actualTab != null) {
			//jméno aktuálně vybraného tabu
			String actualTabName = actualTab.getText();

			//efekt -> dokud probíhá ukládání změníme kurzor na čekající
			panesList.get(actualTabName).setCursor(Cursor.WAIT);

			//aktuální strom, s nímž pracujeme
			TreeView actualTreeView = treeViewsList.get(actualTabName);

			//jestliže strom existuje a není prázdný
			if (actualTreeView != null && !actualTreeView.getRoot().getChildren().isEmpty()) {
				//serializace aktuálního stromu 
				Serializer serializer = new Serializer(actualTabName, actualTreeView);
				String outputXML = serializer.serialize();

				//cesta k ukládanému souboru
				File fileDirectory;

				//jestliže již existuje soubor s tímto uloženým stromem -> přepíšeme ho
				if (fileDirectoriesList.containsKey(actualTreeView)) {
					fileDirectory = fileDirectoriesList.get(actualTreeView);
				} //jinak vybereme novou cestu pro uložení
				else {
					//dialog pro výběr cesty pro uložení
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Save Map");

					//defaultní cesta (=složka projektu)
					fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

					//vybrané formáty souborů
					FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML file", ".xml");
					FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text file", ".txt");
					fileChooser.getExtensionFilters().addAll(xmlFilter, textFilter);

					//vybraná cesta pro uložení
					fileDirectory = fileChooser.showSaveDialog(mainWindowStage);

					//cestu uložíme do seznamu cest
					fileDirectoriesList.put(actualTreeView, fileDirectory);
				}
				//pokud uživatel nedal cancel
				if (fileDirectory != null) {

					//zápis do souboru na vybrané directory
					try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(fileDirectory))) {
						outputWriter.write(outputXML);
						outputWriter.close();
					} catch (IOException ex) {
						System.out.println("Problem with saving to file.");
					}
				}
			}
			//vrátím vzhled kurzoru do původního stavu
			panesList.get(actualTabName).setCursor(Cursor.DEFAULT);
		}
	}

	//načtení mapy z xml souboru
	@FXML
	public void loadMap() throws FileNotFoundException, XMLStreamException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load Map");

		//defaultní cesta (=složka projektu)
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

		//vybrané formáty souborů, které budeme načítat
		FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML file", ".xml");
		FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text file", ".txt");
		fileChooser.getExtensionFilters().addAll(xmlFilter, textFilter);

		File inputXMLFile = fileChooser.showOpenDialog(mainWindowStage);

		if (inputXMLFile != null) {
			DataInputStream inputStream = new DataInputStream(new FileInputStream(inputXMLFile));

			//deserializace
			Deserializer deserializer = new Deserializer(inputStream, this);
			deserializer.deserialize();
		}
	}

	//zavření mapy -> vyskočí dialog s možností uložení mapy
	private void closeMap() throws XMLStreamException {
		//jméno aktuálně vybraného tabu
		String actualTabName = tabPane.getSelectionModel().getSelectedItem().getText();
		TreeView<MindNode> actualTreeView = treeViewsList.get(actualTabName);
		hideBubble(actualTreeView); //pokud je zobrazena nějaká bublina -> skryjeme ji

		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to save this map?", ButtonType.YES, ButtonType.NO);
		Optional<ButtonType> result = alert.showAndWait();
		//uživatel si přeje mapu uložit
		if (result.get() == ButtonType.YES) {
			saveMap();
		}
		//odstraníme treeview a pane ze seznamů
		treeViewsList.remove(actualTabName);
		panesList.remove(actualTabName);
	}

	/**
	 * ****************************************************
	 * AKCE PRO MANIPULACII S KRESLÍCÍM PANELEM A UZLY
	 *****************************************************
	 */
	//klikání do Pane
	private void clickToPane(MouseEvent clickEvent) {

		//jméno aktuálně vybraného tabu
		String actualTabName = tabPane.getSelectionModel().getSelectedItem().getText();

		//aktuální Pane, v němž pracujeme
		Pane actualPane = panesList.get(actualTabName);

		//aktuální strom, s nímž pracujeme
		TreeView actualTreeView = treeViewsList.get(actualTabName);

		//UŽIVATEL KLIKL LEVÝM TLAČÍTKEM (PRIMARY)
		if (clickEvent.getButton().equals(MouseButton.PRIMARY)) {
			//jestliže jsme klikli MIMO jakýkoliv grafický objekt
			if (clickEvent.getTarget().equals(actualPane)) {
				//jestliže je nějaký mindNode (tedy i příslušný TreeItem) vybraný -> odznačíme
				unselect(actualTreeView);
			} //jestliže bylo kliknuto NA grafický objekt -> musí dojít k označení ve stromu
			else {
				if (selectedMindTreeNode != null) {
					//odznačíme neaktuální
					unselect(actualTreeView);
					//jestli bylo kliknuto z Panu na grafický objekt -> označíme ho
					actualTreeView.getSelectionModel().select(selectedMindTreeNode);
				}
			}
		} //UŽIVATEL KLIKL PRAVÝM TLAČÍTKEM (SECONDARY)
		else if (clickEvent.getButton().equals(MouseButton.SECONDARY)) {
			//jestliže jsme klikli MIMO grafický objekt
			if (clickEvent.getTarget().equals(actualPane)) {
				//skryjeme bublinu, aby nám nepřekryla případný dialog nového uzlu
				hideBubble(actualTreeView);
				//přidáme nový Node
				addNode(clickEvent);
				//odznačíme neaktuální
				unselect(actualTreeView);
			} //jestliže jsme klikli NA grafický objekt
			else {
				editNode(clickEvent);
			}
		}
	}

	//přidání nového uzlu (jako reakce na kliknutím pravým tlačítkem do Panu)
	private void addNode(MouseEvent clickEvent) {
		//jméno aktuálně vybraného tabu
		String actualTabName = tabPane.getSelectionModel().getSelectedItem().getText();

		//aktuální Pane, v němž pracujeme
		Pane actualPane = panesList.get(actualTabName);

		//aktuální strom, s nímž pracujeme
		TreeView actualTreeView = treeViewsList.get(actualTabName);

		//souřadnice, kde uživatel kliknul
		double x = clickEvent.getX();
		double y = clickEvent.getY();

		//konstantní iniciální rozměry nosného rectanglu
		double widthRect = 100;
		double heightRect = 70;

		//profil (těchto) počátečních geometrických vlastností
		InitialGeometricParameters geomParams = new InitialGeometricParameters(x, y, widthRect, heightRect);

		//DIALOGOVÉ OKNO
		MindNodeDialogController dialogController = createMindNodeDialog();

		//získání dat z dialogu (zabalené v mindNodeProfilu)
		MindNodeProfile resultMindNodeProfile = dialogController.getResultProfile();

		if (resultMindNodeProfile != null) //vytvoření nového uzlu se vším všudy
		{
			createNewNodeAndAddToPane(actualTreeView, actualPane, resultMindNodeProfile, geomParams);
		}
	}

	//editace vybraného uzlu
	@FXML
	public void editNode(Event ev) {

		//aktuální Tab
		Tab actualTab = tabPane.getSelectionModel().getSelectedItem();

		if (actualTab != null) {
			//jméno aktuálně vybraného tabu
			String actualTabName = actualTab.getText();

			//aktuální Pane, v němž pracujeme
			Pane actualPane = panesList.get(actualTabName);

			//aktuální strom, s nímž pracujeme
			TreeView actualTreeView = treeViewsList.get(actualTabName);

			//vybraná položka ve stromu
			TreeItem<MindNode> selected = (TreeItem<MindNode>) actualTreeView.getSelectionModel().getSelectedItem();

			if (selected != null) {

				hideBubble(actualTreeView); //pokud je zobrazena nějaká bublina -> skryjeme ji

				//starý mindNode
				MindNode oldMindNode = selected.getValue();

				//stará grafická reprezentace mindNode
				GraphicNode oldGraphicNode = oldMindNode.getGraphicNode();

				//čára k rodiči
				Line lineToParent = oldGraphicNode.getLineToParent();

				//seznam čar k potomkům
				List<Line> linesToChildren = oldGraphicNode.getLinesToChildren();

				//nově upravený mindNode
				MindNode editedMindNode = editMindNode(oldMindNode);

				if (editedMindNode != null) {
					//současné souřadnice starého GraphicNode (= iniciální souřadnice nového)
					double actualX = oldGraphicNode.getX();
					double actualY = oldGraphicNode.getY();

					//současné rozměry starého GraphicNode
					double actualWidth = oldGraphicNode.getRectangleWidth();
					double actualHeight = oldGraphicNode.getRectangleHeight();

					//geometrický profil
					InitialGeometricParameters geomParams = new InitialGeometricParameters(actualX, actualY, actualWidth, actualHeight);

					//odstraníme z actualPane starýGraphicNode
					actualPane.getChildren().remove(oldGraphicNode);

					//vytvoříme nový graphicNode, nastavíme ho novému MindNodu a přidáme do actualPane
					GraphicNode newGraphicNode = createGraphicMindNode(editedMindNode.getMindNodeProfile(), geomParams);
					editedMindNode.setGraphicNode(newGraphicNode);
					newGraphicNode.relocateGroup(actualX, actualY);
					actualPane.getChildren().add(newGraphicNode);

					//navázání čar k novému uzlu
					//pokud má rodiče
					if (lineToParent != null) {
						defineLine(lineToParent, null, editedMindNode);
					}
					//také projdeme seznam čar k dětem k uzlu (pokud není prázdný)
					for (Line line : linesToChildren) {
						defineLine(line, editedMindNode, null);
					}

					//propojení vizuálních vlastností
					newGraphicNode.selectedProperty().bind(editedMindNode.selectedProperty()); //(výběr ve stromu)
					editedMindNode.selectedFromPaneProperty().bind(newGraphicNode.selectedFromPaneProperty()); //(výběr v Pane)

					//aktualizujeme položku ve stromu
					selected.setValue(editedMindNode);
					actualTreeView.refresh();
				}
			}
		}
	}

	//vymazání uzlu
	@FXML
	public void deleteNode(Event ev) {
		//aktuální Tab
		Tab actualTab = tabPane.getSelectionModel().getSelectedItem();

		if (actualTab != null) {
			//jméno aktuálně vybraného tabu
			String actualTabName = actualTab.getText();

			//aktuální Pane, v němž pracujeme
			Pane actualPane = panesList.get(actualTabName);

			//aktuální strom, s nímž pracujeme
			TreeView actualTreeView = treeViewsList.get(actualTabName);

			//vybraná položka ve stromu
			TreeItem<MindNode> selected = (TreeItem<MindNode>) actualTreeView.getSelectionModel().getSelectedItem();

			if (selected != null) {
				//odstraňovaný mindNode
				MindNode deletingNode = selected.getValue();

				//projdeme děti odstraňovaného mindNode a přepojíme je na "prarodiče"
				for (TreeItem<MindNode> child : selected.getChildren()) {
					MindNode childMindNode = child.getValue();

					//čára vedoucí od dítěte k mazanému uzlu
					Line lineToParent = childMindNode.getGraphicNode().getLineToParent();

					//rodič mazaného uzlu (prarodič jeho dětí)
					MindNode parentMindNode = deletingNode.getParentMindNode();

					//přepojení čáry na prarodiče, pokud prarodič existuje
					if (parentMindNode != null) {
						defineLine(lineToParent, parentMindNode, null);

						//změna příbuzenských vztahu ve stromu
						childMindNode.setParentTreeNode(parentMindNode.getTreeNode());
						parentMindNode.getTreeNode().getChildren().add(child); //přesunutí ve stromu pod nového rodiče
					} // pokud prarodič dětí neexistuje, smažeme čáry
					else {
						actualPane.getChildren().remove(lineToParent);

						//změna příbuzenských vztahu ve stromu
						childMindNode.setParentTreeNode(null);
						actualTreeView.getRoot().getChildren().add(child);
					}
				}
				//odstraníme čáru odstraňovaného mindNodu (k jeho rodiči)
				if (deletingNode.getParentMindNode() != null) {
					actualPane.getChildren().remove(deletingNode.getGraphicNode().getLineToParent());
				}

				//odstraníme grafický mindNode z Pane
				actualPane.getChildren().remove(deletingNode.getGraphicNode());

				//odstraníme mindNode ve stromové hierarchii
				selected.getParent().getChildren().remove(selected);
				actualTreeView.getSelectionModel().clearSelection();
			}
		}
	}

	/**
	 * *********************************
	 * METODY PRO VYTVÁŘENÍ OBJEKTŮ
	 **********************************
	 */
	/*vytvoří nový Tab, TreeView a Pane pro nově vzniklou mapu a vyobrazí je 
	- vrací true, při úspěšném provedení; false, pokud vstupní jméno mapy již existuje */
	public boolean createNewMap(String newMapName) {
		if (newMapName != null) {
			//testování, zda už stejné jméno mapy neexistuje
			for (Tab tab : tabPane.getTabs()) {
				if (tab.getText().equals(newMapName)) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setHeaderText("This name of new map already exists!");
					alert.showAndWait();
					return false;
				}
			}

			//vytvoření nového Tabu, TreeView a Pane pro tuto mapu (a jejich přidání do příslušných seznamů + nastavení vlastností)
			Tab newTab = new Tab(newMapName);
			TreeView newTreeView = new TreeView();
			Pane newPane = new Pane();
			newTreeView.setRoot(new TreeItem()); //nastavení abstraktního rootu novému stromu (abychom mohli mít více reálných rootů)
			newTreeView.setShowRoot(false); //tento root skryjeme
			treeViewsList.put(newMapName, newTreeView);
			panesList.put(newMapName, newPane);
			tabPane.getTabs().add(newTab); //přidání Tabu do TabPane
			tabPane.getSelectionModel().select(newTab); //přepneme se rovnou do nového tabu
			newTab.setOnSelectionChanged(e -> {
				mainWindowPane.setLeft(treeViewsList.get(newMapName)); //když uživatel překlikne tab, změní se aktuální strom
			});

			//nastavení vlastností
			newPane.setStyle("-fx-background-color: #e0e8ee");
			newPane.setOnMouseClicked(this::clickToPane); //nastavení akce pro kliknutí na tento panel
			newTab.setContent(newPane); //nastavení obsahu pro tento Tab
			newTab.setOnCloseRequest(e -> { //nastavení události pro zavření
				try {
					closeMap();
				} catch (XMLStreamException ex) {
					Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
				}
			});
			mainWindowPane.setLeft(newTreeView);
		}
		return true;
	}

	//nejdůležitější metoda - stará se o vytvoření nového uzlu, přidání do stromu a vykreslení v Pane
	public MindNode createNewNodeAndAddToPane(TreeView<MindNode> actualTreeView, Pane actualPane, MindNodeProfile mindNodeProfile, InitialGeometricParameters geomParams) {
		//rodič právě vznikajícího uzlu (na základě toho, který uzel je vybraný v TreeView)
		TreeItem<MindNode> parentNode;
		Object selectedTreeItem = actualTreeView.getSelectionModel().getSelectedItem();
		if (selectedTreeItem != null) {
			parentNode = (TreeItem<MindNode>) selectedTreeItem;
		} else {
			parentNode = null; //děláme jakoby abstraktní root neexistoval (má to svůj smysl při tvorbě čar)
		}

		//VYTVOŘENÍ NOVÉHO MIND NODU - LOGICKÉHO I GRAFICKÉHO
		if (mindNodeProfile != null) {

			//nový logický MindNode
			MindNode newMindNode = new MindNode(parentNode); //argumenty = jeho rodič ve stromu a jeho grafická reprezentace

			//nastavení jeho vlastností
			newMindNode.setMindNodeProfile(mindNodeProfile);

			//vytvoření MindNodu jako grafického objektu
			GraphicNode graphicNode = createGraphicMindNode(mindNodeProfile, geomParams);

			//propojení logického a grafického uzlu
			newMindNode.setGraphicNode(graphicNode);

			//propojení vizuálních vlastností
			graphicNode.selectedProperty().bind(newMindNode.selectedProperty()); //(výběr ve stromu)
			newMindNode.selectedFromPaneProperty().bind(graphicNode.selectedFromPaneProperty()); //(výběr v Pane)

			//umístíme graphicNode na místo, kde uživatel klikl
			graphicNode.relocateGroup(geomParams.getInitX(), geomParams.getInitY());

			//spojovací čára
			if (parentNode != null) {
				Line line = defineLine(null, parentNode.getValue(), newMindNode);
				actualPane.getChildren().add(line);
				line.toBack();
			}

			//zobrazení grafického uzlu
			actualPane.getChildren().add(graphicNode);

			//přidání uzlu do stromové hierarchie
			TreeItem<MindNode> newTreeItem = new TreeItem<>(newMindNode);
			if (parentNode == null) {
				TreeItem abstractRoot = actualTreeView.getRoot();
				abstractRoot.getChildren().add(newTreeItem); //newTreeItem je nový 'reálný' root (potomek abstraktního)
			} else {
				parentNode.getChildren().add(newTreeItem);
			}
			//nastavení akce na treeItem
			actualTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
					-> {
				TreeItem<MindNode> oldSelected = (TreeItem<MindNode>) oldValue;
				TreeItem<MindNode> newSelected = (TreeItem<MindNode>) newValue;
				if (oldSelected != null) {
					oldSelected.getValue().setSelected(false);
				}
				if (newSelected != null) {
					newSelected.getValue().setSelected(true);
				}
			});
			//nastavení treeItem mindNodu
			newMindNode.setTreeNode(newTreeItem);

			return newMindNode;
		}
		return null;
	}

	private GraphicNode createGraphicMindNode(MindNodeProfile mindNodeProfile, InitialGeometricParameters geomParams) {
		//aktuální Pane, v němž pracujeme
		Pane actualPane = panesList.get(tabPane.getSelectionModel().getSelectedItem().getText());

		//nosný rectangle
		ResizableRectangle resizableRectangle = new ResizableRectangle(geomParams.getInitWidth(), geomParams.getInitHeight());

		//nadefinuje vlastnosti shapu a jeho vazby na nosný rectangle
		createMindShape(mindNodeProfile, resizableRectangle);

		//nový graphicNode
		GraphicNode graphicNode = new GraphicNode(resizableRectangle, mindNodeProfile);
		graphicNode.setParentPane(actualPane); //nastavíme mu rodičovský panel, aby si z něj mohl vytáhnout jeho rozměry (které jsou proměnné) (možná nešikovné)
		return graphicNode;
	}

	//definice vazeb na resizableRectangle a vlastností shapu v mindNodu
	public Shape createMindShape(MindNodeProfile mindNodeProfile, ResizableRectangle rect) {
		Shape shape = mindNodeProfile.getShape();
		Color color = mindNodeProfile.getColor();

		//efekt
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(2.0f);
		dropShadow.setOffsetY(2.0f);
		shape.setEffect(dropShadow);
		//barva
		shape.setFill(color);
		//vazby
		if (shape instanceof Circle) {
			Circle circle = (Circle) shape;
			circle.centerXProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)));
			circle.centerYProperty().bind(rect.yProperty().add(rect.heightProperty().divide(2)));
			circle.radiusProperty().bind(Bindings.min(rect.heightProperty(), rect.widthProperty()).divide(2));
			return circle;
		} else if (shape instanceof Ellipse) {
			Ellipse ellipse = (Ellipse) shape;
			ellipse.centerXProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)));
			ellipse.centerYProperty().bind(rect.yProperty().add(rect.heightProperty().divide(2)));
			ellipse.radiusXProperty().bind(rect.widthProperty().divide(2));
			ellipse.radiusYProperty().bind(rect.heightProperty().divide(2));
			return ellipse;
		} else if (shape instanceof Rectangle) {
			Rectangle rectangle = (Rectangle) shape;
			rectangle.xProperty().bind(rect.xProperty());
			rectangle.yProperty().bind(rect.yProperty());
			rectangle.widthProperty().bind(rect.widthProperty());
			rectangle.heightProperty().bind(rect.heightProperty());
			//zaoblení hran
			rectangle.setArcWidth(20);
			rectangle.setArcHeight(20);
			return rectangle;
		} else {
			return shape;
		}
	}

	/**
	 * *********************************
	 * METODY PRO VYTVÁŘENÍ DIALOGŮ
	 **********************************
	 */
	public MindNodeDialogController createMindNodeDialog() {
		//vytvoření dialogového okna a nastavení jeho vlastností
		Stage mindNodeDialogStage = new Stage();
		mindNodeDialogStage.initOwner(mainWindowStage);
		mindNodeDialogStage.initModality(Modality.WINDOW_MODAL);

		//načtení fxml dokumentu pro tento dialog a nastavení kontroleru tomuto formuláři
		MindNodeDialogController dialogController = new MindNodeDialogController(mindNodeDialogStage); //kontroler
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/sources/FXMLMindNodeDialog.fxml")); //načtení fxml dokumentu
		loader.setControllerFactory(c -> dialogController);

		try {
			//vizuální vlastnosti dialogu a zobrazení
			Parent mainDialogPanel;
			mainDialogPanel = loader.load(); //hlavní panel dialogu = GridPane
			Scene scene = new Scene(mainDialogPanel, 300, 350);
			scene.getStylesheets().add("/sources/CSSNewMapDialog.css");
			mindNodeDialogStage.setTitle("Mind Node");
			mindNodeDialogStage.centerOnScreen();
			mindNodeDialogStage.setScene(scene);
			mindNodeDialogStage.showAndWait();
		} catch (IOException ex) {
			System.err.println("Problem with loading the root panel from FXML Document");
			ex.printStackTrace();
		}

		return dialogController;
	}

	public NewMapDialogController createNewMapDialog() throws IOException {
		//VYTVOŘENÍ DIALOGU
		//vytvoření dialogového okna a nastavení jeho vlastností
		Stage newMapDialogStage = new Stage();
		newMapDialogStage.initOwner(mainWindowStage);
		newMapDialogStage.initModality(Modality.WINDOW_MODAL);

		//načtení fxml dokumentu pro tento dialog a nastavení kontroleru tomuto formuláři
		NewMapDialogController dialogController = new NewMapDialogController(newMapDialogStage); //kontroler
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/sources/FXMLNewMapDialog.fxml")); //načtení fxml dokumentu
		loader.setControllerFactory(c -> dialogController);

		//vizuální vlastnosti dialogu a zobrazení
		Parent mainDialogPanel = loader.load(); //hlavní panel dialogu = GridPane
		Scene scene = new Scene(mainDialogPanel, 300, 180);
		scene.getStylesheets().add("/sources/CSSNewMapDialog.css");
		newMapDialogStage.setTitle("New Map");
		newMapDialogStage.centerOnScreen();
		newMapDialogStage.setScene(scene);
		newMapDialogStage.showAndWait();

		return dialogController;
	}

	/**
	 * ************************
	 * METODY PRO EDITACI
	 *************************
	 */
	public MindNode editMindNode(MindNode node) {
		//DIALOGOVÉ OKNO PRO EDITACI
		//vytvoření dialogového okna a nastavení jeho vlastností
		Stage mindNodeDialogStage = new Stage();
		mindNodeDialogStage.initOwner(mainWindowStage);
		mindNodeDialogStage.initModality(Modality.WINDOW_MODAL);

		//načtení fxml dokumentu pro tento dialog a nastavení kontroleru tomuto formuláři
		MindNodeDialogController dialogController = new MindNodeDialogController(mindNodeDialogStage, node); //kontroler
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/sources/FXMLMindNodeDialog.fxml")); //načtení fxml dokumentu
		loader.setControllerFactory(c -> dialogController);

		try {
			//vizuální vlastnosti dialogu a zobrazení
			Parent mainDialogPanel;
			mainDialogPanel = loader.load(); //hlavní panel dialogu = GridPane
			Scene scene = new Scene(mainDialogPanel, 300, 350);
			scene.getStylesheets().add("/sources/CSSNewMapDialog.css");
			mindNodeDialogStage.setTitle("Mind Node Edit");
			mindNodeDialogStage.centerOnScreen();
			mindNodeDialogStage.setScene(scene);
			mindNodeDialogStage.showAndWait();
		} catch (IOException ex) {
			System.err.println("Problem with loading the root panel from FXML Document");
			ex.printStackTrace();
		}

		//získání nových dat z dialogu
		MindNodeProfile resultMindNodeProfile = dialogController.getResultProfile();

		if (resultMindNodeProfile != null) {
			node.setMindNodeProfile(resultMindNodeProfile);
			return node;
		} else {
			return null;
		}
	}

	/**
	 * *************
	 * SETTERY
	 **************
	 */
	//nastavení stage kontrolovaného okna
	public void setMainWindowStage(Stage stage) {
		mainWindowStage = stage;
	}

	/**
	 * *************
	 * GETTERY
	 **************
	 */
	public Map<String, TreeView> getTreeViewsList() {
		return treeViewsList;
	}

	public Map<String, Pane> getPanesList() {
		return panesList;
	}

	/**
	 * *************************
	 * DALŠÍ POMOCNÉ METODY
	 **************************
	 */
	//odznačení uzlů
	private void unselect(TreeView<MindNode> treeView) {
		TreeItem<MindNode> selectedTreeItem = (TreeItem<MindNode>) treeView.getSelectionModel().getSelectedItem();
		if (selectedTreeItem != null) {
			selectedTreeItem.getValue().setSelected(false);
			selectedTreeItem.getValue().getGraphicNode().setSelectedFromPane(false);
		}
		treeView.getSelectionModel().clearSelection(); //odznačíme označený TreeItem
	}

	private void hideBubble(TreeView<MindNode> actualTreeView) {
		if (actualTreeView != null) {
			TreeItem<MindNode> selected = actualTreeView.getSelectionModel().getSelectedItem();

			if (selected != null) {
				GraphicNode graphicNode = selected.getValue().getGraphicNode();
				graphicNode.hideBubble();
			}
		}
	}

	//definice čáry mezi 2 uzly
	private Line defineLine(Line line, MindNode parent, MindNode child) {
		//upravujeme vztahy u existující čáry
		if (line != null) {
			//startovní vlastnost uzlu vážeme k rodiči
			if (parent != null) {
				line.startXProperty().bind(parent.getGraphicNode().xProperty().add(parent.getRectangle().widthProperty().divide(2)));
				line.startYProperty().bind(parent.getGraphicNode().yProperty().add(parent.getRectangle().heightProperty().divide(2)));
				parent.getGraphicNode().addLineToChild(line);
			} //koncovou vlastnost uzlu vážeme k dítěti
			else if (child != null) {
				line.endXProperty().bind(child.getGraphicNode().xProperty().add(child.getRectangle().widthProperty().divide(2)));
				line.endYProperty().bind(child.getGraphicNode().yProperty().add(child.getRectangle().heightProperty().divide(2)));
				child.getGraphicNode().setLineToParent(line);
			}
			return line;
		} //vytváříme novou čáru
		else {
			GraphicNode parentGraphic = parent.getGraphicNode();
			GraphicNode childGraphic = child.getGraphicNode();

			//souřadnice
			double startX = parentGraphic.getX() + (parent.getRectangle().getWidth() / 2);
			double startY = parentGraphic.getY() + (parent.getRectangle().getHeight() / 2);
			double endX = childGraphic.getX() + (child.getRectangle().getWidth() / 2);
			double endY = childGraphic.getY() + (child.getRectangle().getHeight() / 2);

			//nová čára
			Line newLine = new Line(startX, startY, endX, endY);

			//grafické vlastnosti čáry
			newLine.setStroke(Color.DIMGRAY);
			newLine.setOpacity(0.6);
			DropShadow dropShadow = new DropShadow();
			dropShadow.setOffsetX(2.0f);
			dropShadow.setOffsetY(2.0f);
			newLine.setEffect(dropShadow);

			//vazby čar ke svým objektům
			newLine.startXProperty().bind(parentGraphic.xProperty().add(parent.getRectangle().widthProperty().divide(2)));
			newLine.startYProperty().bind(parentGraphic.yProperty().add(parent.getRectangle().heightProperty().divide(2)));
			newLine.endXProperty().bind(childGraphic.xProperty().add(child.getRectangle().widthProperty().divide(2)));
			newLine.endYProperty().bind(childGraphic.yProperty().add(child.getRectangle().heightProperty().divide(2)));

			//nastavení čáry vlastníkům (rodiči i potomku)
			parentGraphic.addLineToChild(newLine);
			childGraphic.setLineToParent(newLine);

			return newLine;
		}
	}

	/**
	 * *********************
	 * STATICKÉ METODY
	 **********************
	 */
	/*metoda pro případ, že dojde k označení uzlu jinde než ve stromu -> musíme zjistit, který to byl bez toho, aniž
	by se do TreeView nastavoval přímo uzel sám -> použije tedy tuto statickou metodu, aby se nastavil do této třídy a my ten případ již ošetříme*/
	public static void setSelectedMindNode(TreeItem<MindNode> mindTreeNode) {
		selectedMindTreeNode = mindTreeNode;
	}
}
