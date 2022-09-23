package mindmaps;

import java.io.DataInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Tato třída zajišťuje načtení mapy z korektně formátovaného (třídou Serializer) xml dokumentu. Mimo přečtení dokumentu se také stará o vytvoření
 * nové mapy podle hodnot získaných z xml dokumentu a to prostřednictvím přístupu k MainWindowControlleru a jeho metodám pro tvorbu mapy.
 */
public class Deserializer {
	
	private DataInputStream inputStream;
	private MainWindowController mainWindowController;
	
	//aktuální přečtený element
	private String actualElement = null;
		
	//atributy související s mindNode
	private double x = 0;
	private double y = 0;
	private double width = 0;
	private double height = 0;
	private String mapName;
	private String mindNodeName;
	private String shapeClass;
	private String colorHexCode;
	private String textColorHexCode;
	private String note = new String();

	public Deserializer(DataInputStream inputStream, MainWindowController mainWindowController) {
		this.inputStream = inputStream;
		this.mainWindowController = mainWindowController;
	}

	public void deserialize() throws XMLStreamException {

		//reader xml vstupu
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputStream);

		//příznaky
		boolean mapNameElement = false;
		boolean startElement = false;

		while (xmlReader.hasNext()) {

			switch (xmlReader.next()) {
				case XMLStreamReader.START_ELEMENT:

					startElement = true;
					actualElement = xmlReader.getName().toString();

					if (!mapNameElement) {
						mapNameElement = true;
						mapName = xmlReader.getAttributeValue(null, "name");
						//vytvoření nové mapy
						if (!mainWindowController.createNewMap(mapName)) {
							return; //došlo k neúspěchu při vytváření mapy
						}
					} else {
						if (actualElement.equals("mindNode")) {
							mindNodeName = xmlReader.getAttributeValue(null, "name");
						}
						if (actualElement.equals("coordinates")) {
							x = Double.valueOf(xmlReader.getAttributeValue(null, "x"));
							y = Double.valueOf(xmlReader.getAttributeValue(null, "y"));
						}
						if (actualElement.equals("dimensions")) {
							width = Double.valueOf(xmlReader.getAttributeValue(null, "width"));
							height = Double.valueOf(xmlReader.getAttributeValue(null, "height"));
						}
					}
					break;

				case XMLStreamReader.CHARACTERS:
					if (startElement && actualElement != null) {
						if (actualElement.equals("shape")) {
							shapeClass = xmlReader.getText();
						}
						if (actualElement.equals("color")) {
							colorHexCode = xmlReader.getText();
						}
						if (actualElement.equals("textColor")) {
							textColorHexCode = xmlReader.getText();
						}
						if (actualElement.equals("note")) {
							note = xmlReader.getText();
						}
					}
					break;

				case XMLStreamReader.END_ELEMENT:
					startElement = false;
					//když už máme všechny vlastnosti, můžeme vytvořit uzel -> vytvoříme ho
					if (xmlReader.getName().toString().equals("properties")) {
						createMindNode();
					}
					//pokud jsme na konci elementu mindNode, označíme ve stromu uzel, který bude novým parentem ostatních uzlů, které dále vytvoříme
					if (xmlReader.getName().toString().equals("mindNode")) {
						selectNewParent();
					}
					break;
				case XMLStreamReader.END_DOCUMENT:
					//vyčistíme všechna označení ve stromu
					TreeView<MindNode> actualTreeView = mainWindowController.getTreeViewsList().get(mapName);
					actualTreeView.getSelectionModel().clearSelection();
					break;
			}
		}
	}

	private void createMindNode() {
		TreeView<MindNode> actualTreeView = mainWindowController.getTreeViewsList().get(mapName);
		Pane actualPane = mainWindowController.getPanesList().get(mapName);
		MindNode mindNode = null;
		MindNodeProfile mindNodeProfile = null;
		InitialGeometricParameters geomParams = new InitialGeometricParameters(x, y, width, height);
		
		try {
			mindNodeProfile = new MindNodeProfile(mindNodeName, Color.valueOf(colorHexCode), Color.valueOf(textColorHexCode), createShape(shapeClass), note);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(Deserializer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(Deserializer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(Deserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		if (mindNodeProfile != null) {
			mindNode = mainWindowController.createNewNodeAndAddToPane(actualTreeView, actualPane, mindNodeProfile, geomParams);

			//označíme mindNode ve stromu, protože vztahy mezi ním a jeho potenciálními potomky se určuje podle výběru ve stromu
			if (mindNode != null) {
				actualTreeView.getSelectionModel().select(mindNode.getTreeNode());
			}
		}
		
		//pozor, po vytvoření mindNodu, je potřeba note vynulovat, protože narozdíl od ostatních atributů může i nemusí existovat
		note = new String();
	}
	
	private void selectNewParent() {
		TreeView<MindNode> actualTreeView = mainWindowController.getTreeViewsList().get(mapName);
		TreeItem<MindNode> selected = actualTreeView.getSelectionModel().getSelectedItem();
		if (selected != null || !selected.equals(actualTreeView.getRoot())) {
			TreeItem<MindNode> parentOfSelected = selected.getParent();
			if (!parentOfSelected.equals(actualTreeView.getRoot())) {
				actualTreeView.getSelectionModel().select(parentOfSelected);
			}
		}
	}

	private Shape createShape(String shapeClassString) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class shapeClass = Class.forName(shapeClassString);
		return (Shape) shapeClass.newInstance();
	}
	
}
