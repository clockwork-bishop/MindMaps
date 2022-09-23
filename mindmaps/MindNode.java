package mindmaps;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;


/**
 * Třída je logickou reprezentací myšlenkového uzlu, která v sobě uchovává všechny ostatní reprezentace tohoto uzlu (grafickou, stromovou).
 */
public class MindNode {
	
	//grafický objekt reprezentující MindNode
	private GraphicNode graphicNode;
	
	//mindNode jako prvek stromové struktury
	private TreeItem<MindNode> treeNode;
	
	//rodič mindNodu ve stromové struktuře
	private TreeItem<MindNode> parentTreeNode;
	
	//vlastnost, zda je mindNode vybraný (kvůli propojení s grafickou reprezentací)
	private BooleanProperty selected = new SimpleBooleanProperty(false);
	private BooleanProperty selectedFromPane = new SimpleBooleanProperty(false);
	
	//všechny nastavitelné vlastnosti mindNodu
	private MindNodeProfile mindNodeProfile;
	
	public MindNode(TreeItem<MindNode> parentNode) {
		parentTreeNode = parentNode;
		
		//výběr mindNodu způsobený v Pane (kliknutím na grafický objekt) -> musíme předat MainWindowControlleru sebe, aby nás označil ve TreeView
		selectedFromPane.addListener((observable) -> {
			if (selectedFromPane.get())
				MainWindowController.setSelectedMindNode(treeNode);
		});
	}
	
	//GETTERY
	public ResizableRectangle getRectangle() {
		return graphicNode.getRectangle();
	}
	
	public GraphicNode getGraphicNode() {
		return graphicNode;
	}
	
	public String getName() {
		return mindNodeProfile.getName();
	}
	
	public Color getColor() {
		return mindNodeProfile.getColor();
	}
	
	public Color getTextColor() {
		return mindNodeProfile.getTextColor();
	}
	
	public Shape getShape() {
		return mindNodeProfile.getShape();
	}
	
	public String getNote() {
		return mindNodeProfile.getNote();
	}
	
	public MindNode getParentMindNode() {
		if (parentTreeNode != null)
			return parentTreeNode.getValue();
		else return null;
	}
	
	public MindNodeProfile getMindNodeProfile() {
		return mindNodeProfile;
	}
	
	public TreeItem<MindNode> getTreeNode() {
		return treeNode;
	}
	
	
	//SETTERY

	public void setMindNodeProfile(MindNodeProfile mindNodeProfile) {
		this.mindNodeProfile = mindNodeProfile;
	}
	
	public void setGraphicNode(GraphicNode graphicNode) {
		this.graphicNode = graphicNode;
	}
	
	public void setSelected(boolean val) {
		selectedProperty().set(val);
	}
	
	public void setTreeNode(TreeItem<MindNode> treeNode) {
		this.treeNode = treeNode;
	}
	
	public void setParentTreeNode(TreeItem<MindNode> parentTreeNode) {
		this.parentTreeNode = parentTreeNode;
	}
	
	//JAVAFX VLASTNOSTI
	public final BooleanProperty selectedProperty() {
		return selected;
	}
	
	public final BooleanProperty selectedFromPaneProperty() {
		return selectedFromPane;
	}
	
	//...aby se v TreeView zobrazovala jména MindNodů
	@Override
	public String toString() {
		return mindNodeProfile.getName();
	}
}
