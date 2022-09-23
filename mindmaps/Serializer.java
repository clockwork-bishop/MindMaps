package mindmaps;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Třída zajišťuje vytvoření xml dokumentu, který uchová myšlenkovou mapu, již chce uživatel uložit, v zakódované podobě a to na základě údajů o mapě obsažených
 * v TreeView, který kontruktor této třídy bere jako argument.
 */
public class Serializer {
	
	private String nameOfMap;
	private TreeView<MindNode> treeView;
	
	//výstup serializace
	private OutputStream outputStream = new ByteArrayOutputStream();
	
	//serializer pro konkrétní TreeView
	public Serializer(String nameOfMap, TreeView<MindNode> treeView) {
		this.nameOfMap = nameOfMap;
		this.treeView = treeView;
	}
	
	//vrací string formátovaný jako xml dokument
	public String serialize() throws XMLStreamException {
		
		//writer do outputStream
		XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
		
		//začátek XML Dokumentu
		xmlWriter.writeStartDocument();
		
		//jméno mapy (tabu)
		xmlWriter.writeStartElement("map");
		xmlWriter.writeAttribute("name",nameOfMap);
		
		//projdeme treeView
		searchAllTree(treeView.getRoot(), xmlWriter);
		
		//KONEC - jméno mapy (tabu)
		xmlWriter.writeEndElement();
		
		//konec XML Dokumentu
		xmlWriter.writeEndDocument();
		
		return outputStream.toString();
	}
	
	private void searchAllTree(TreeItem<MindNode> rootTreeItem, XMLStreamWriter xmlWriter) throws XMLStreamException {
		
		//pokud se treeItem nerovná abstraktnímu rootu
		if (!rootTreeItem.equals(treeView.getRoot())) {
			
			//jméno mindNodu
			xmlWriter.writeStartElement("mindNode");
			xmlWriter.writeAttribute("name", rootTreeItem.getValue().getName());
			
			//vlastnosti mindNodu
			xmlWriter.writeStartElement("properties");
			
			//souřadnice
			xmlWriter.writeStartElement("coordinates");
			xmlWriter.writeAttribute("x", String.valueOf(rootTreeItem.getValue().getGraphicNode().getX()));
			xmlWriter.writeAttribute("y", String.valueOf(rootTreeItem.getValue().getGraphicNode().getY()));
			xmlWriter.writeEndElement();
			
			//souřadnice
			xmlWriter.writeStartElement("dimensions");
			xmlWriter.writeAttribute("width", String.valueOf(rootTreeItem.getValue().getGraphicNode().getRectangleWidth()));
			xmlWriter.writeAttribute("height", String.valueOf(rootTreeItem.getValue().getGraphicNode().getRectangleHeight()));
			xmlWriter.writeEndElement();
			
			//tvar
			xmlWriter.writeStartElement("shape");
			xmlWriter.writeCharacters(rootTreeItem.getValue().getShape().getClass().getName());
			xmlWriter.writeEndElement();
			
			//barva
			xmlWriter.writeStartElement("color");
			xmlWriter.writeCharacters(rootTreeItem.getValue().getColor().toString());
			xmlWriter.writeEndElement();
			
			//barva textu
			xmlWriter.writeStartElement("textColor");
			xmlWriter.writeCharacters(rootTreeItem.getValue().getTextColor().toString());
			xmlWriter.writeEndElement();
			
			//textová poznámka
			if (!rootTreeItem.getValue().getNote().isEmpty()) {
				xmlWriter.writeStartElement("note");
				xmlWriter.writeCharacters(rootTreeItem.getValue().getNote());
				xmlWriter.writeEndElement();
			}
			
			//KONEC - vlastnosti mindNodu
			xmlWriter.writeEndElement();
		
			if(!rootTreeItem.getChildren().isEmpty()) {
				//potomci mindNodu
				xmlWriter.writeStartElement("children");

				for(TreeItem<MindNode> childTreeItem : rootTreeItem.getChildren())
					//rekurzivní zanoření
					searchAllTree(childTreeItem, xmlWriter);

				//KONEC - potomci mindNodu
				xmlWriter.writeEndElement();
			}
			
			//KONEC - jméno mindNode
			xmlWriter.writeEndElement();
		}
		else {
			//prohedání děti abstraktního rootu (bez výpisu jakýchkoliv tagů pro abstract root)
			for (TreeItem<MindNode> childTreeItem : rootTreeItem.getChildren())
				searchAllTree(childTreeItem, xmlWriter);
		}
	}
}