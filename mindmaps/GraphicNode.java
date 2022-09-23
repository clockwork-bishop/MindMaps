package mindmaps;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Tato třída představuje grafickou reprezentaci MindNodu. Ta je realizována seskupením množiny objektů tvořících mindNode (tvar uzlu, zvětšovací obdélník, popisek,
 * příp. poznámka v bublině) v Group, ze které tato třída dědí.
 */
public class GraphicNode extends Group {
	
	private MindNodeProfile mindNodeProfile;
	
	//komponenty grafického uzlu
	private ResizableRectangle rectangle;
	private Shape shape;
	private Label label;
	
	//poznámka v bublině
	private BubblePopUp bubblePopUp = new BubblePopUp();;
	
	//čára 
	/*(tyto informace jsou potřebné především při editaci uzlu, aby došlo k převázání na nový uzel (pokud vytváříme zcela nový))
	nebo pokud bychom chtěli děti přesměrovat k jinému rodiči*/
	private Line lineToParent; //čára vedoucí k rodiči
	private List<Line> linesToChildren = new ArrayList<Line>(); //seznam čar vedoucích k dětem
	
	//pozice
	private DoubleProperty xProperty = new SimpleDoubleProperty();
	private DoubleProperty yProperty = new SimpleDoubleProperty();
	
	//vlastnost označení
	private BooleanProperty selected = new SimpleBooleanProperty(false);
	private BooleanProperty selectedFromPane = new SimpleBooleanProperty(false); //označení objektu v Pane
	
	//rodičovský panel uzlu (potřebujeme zjišťovat jeho aktuální rozměry, abychom mohli určovat hranice umístění uzlu)
	private Pane parentPane;
	
	//panel pro popisek a rectangle
	private StackPane stackPane = new StackPane();
	
	private Point2D mousePosition;

	public GraphicNode(ResizableRectangle rectangle, MindNodeProfile mindNodeProfile) {
		this.mindNodeProfile = mindNodeProfile;
		this.rectangle = rectangle;
		this.shape = mindNodeProfile.getShape();
		
		Color textColor = mindNodeProfile.getTextColor();
		String text = mindNodeProfile.getName();
		
		//nastavení popisku
		label = new Label(text);
		label.setTextFill(textColor);

		//navázání velikosti popisků na vlastnosti rozměrů shapu
		bindLabelToShapeSize();
		
		//nastavení popisku, shapu a resizableRectanglu do stackPane
		stackPane.getChildren().addAll(this.rectangle, this.shape, label);
		
		//akce shapu a rectanglu
		this.shape.setOnDragDetected(this::dragShape);
		this.shape.setOnMouseReleased(this::releaseShape);
		this.shape.setOnMouseDragged(this::moveShape);
		this.label.setOnMouseDragged(this::moveShape);
		this.shape.setOnMouseClicked(this::selectShape); //výběr kliknutím na objekt
		this.label.setOnMouseClicked(this::selectShape);
		this.shape.setOnMouseEntered(this::showBubbleNote);
		this.shape.setOnMouseExited(this::hideBubbleNote);
		
		//označení graphicNodu způsobené akcí zvnějšku -> provedeme vykreslení/zmizení
		selected.addListener((observable) -> {
			if (selectedProperty().get())
				setResizableRectangleVisible(true);
			else {
				setResizableRectangleVisible(false);
				hideBubble(); //jako vedlejší efekt skryjeme bublinu
			}
		});
		
		//nastavení nového dispatcheru eventů pro bubblePopUp (aby nám popUp nezachytával klávesové eventy, které nejsou určeny pro nej)
		bubblePopUp.setNewDispatcher(this.getEventDispatcher());
		
		//nastavení této group
		this.getChildren().addAll(stackPane, rectangle.getResizeHandle());
	}
	
	//GETTERY
	public ResizableRectangle getRectangle() {
		return rectangle;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public double getX() {
		return xProperty.get();
	}
	
	public double getY() {
		return yProperty.get();
	}
	
	public double getRectangleWidth() {
		return rectangle.getWidth();
	}
	
	public double getRectangleHeight() {
		return rectangle.getHeight();
	}
	
	//JAVAFX VLASTNOSTI
	
	public DoubleProperty xProperty() {
		return xProperty;
	}
	
	public DoubleProperty yProperty() {
		return yProperty;
	}

	public final BooleanProperty selectedProperty() {
		return selected;
	}
	
	public final BooleanProperty selectedFromPaneProperty() {
		return selectedFromPane;
	}
	
	public Line getLineToParent() {
		return lineToParent;
	}
	
	public List<Line> getLinesToChildren() {
		return linesToChildren;
	}
	
	//SETTERY
	private void setResizableRectangleVisible(boolean bool) {
		rectangle.setRectangleVisible(bool);
		rectangle.setHandleVisible(bool);
	}

	public void setParentPane(Pane parentPane) {
		this.parentPane = parentPane;
	}

	public void setX(double x) {
		this.xProperty.set(x);
	}
	
	public void setY(double y) {
		this.yProperty.set(y);
	}
	
	public void setLineToParent(Line line) {
		this.lineToParent = line;
	}
	
	public void addLineToChild(Line line) {
		this.linesToChildren.add(line);
	}
	
	public void setSelectedFromPane(boolean val) {
		selectedFromPaneProperty().set(val);
	}

	//AKCE
	public void dragShape(MouseEvent ev) {
		shape.setCursor(Cursor.CLOSED_HAND);
		mousePosition = new Point2D(ev.getSceneX(), ev.getSceneY());
	}
	
	public void releaseShape(MouseEvent ev) {
		shape.setCursor(Cursor.DEFAULT);
		mousePosition = null;
	}
	
	public void moveShape(MouseEvent ev) {
		//když táhneme tvarem, skryjeme i bubble poznámku
		if (bubblePopUp != null)
			bubblePopUp.hide();
		if (mousePosition != null) {
			double changeX = ev.getSceneX() - mousePosition.getX();
			double changeY = ev.getSceneY() - mousePosition.getY();
			double newX = (this.getX() /*+ rectangle.getWidth()/2*/) + changeX; //pozor, co jsme v relocateGroup odečetli, musíme přičíst
			double newY = (this.getY() /*+ rectangle.getHeight()/2*/) + changeY;
			
			//hranice posunu
			if(newX < 0)
				newX = 0;
			if(newY < 0)
				newY = 0;
			if(newX > parentPane.getWidth() - getRectangleWidth())
				newX = parentPane.getWidth() - getRectangleWidth();
			if(newY > parentPane.getHeight() - getRectangleHeight())
				newY = parentPane.getHeight() - getRectangleHeight();
			
			this.relocateGroup(newX, newY);

			mousePosition = new Point2D(ev.getSceneX(), ev.getSceneY());
		}
	}
	
	public void selectShape(MouseEvent ev) {
		if (ev.getButton().equals(MouseButton.PRIMARY)) {
			this.setSelectedFromPane(true);
			this.setResizableRectangleVisible(true);
		}
		else hideBubble();
	}
	
	public void showBubbleNote(MouseEvent ev) {
		if (!mindNodeProfile.getNote().isEmpty()) {
			Bounds bounds = parentPane.localToScreen(parentPane.getBoundsInLocal()); //souřadnice parentPane relativně k celé screen
			double x = getX() + getRectangleWidth() - 22;
			double y = getY() - bubblePopUp.getBubbleHeight() + 14;

			bubblePopUp.setNote(mindNodeProfile.getNote());
			bubblePopUp.show(parentPane, bounds.getMinX() + x,  bounds.getMinY() + y);
		}
	}
	
	public void hideBubbleNote(MouseEvent ev) {
		if (!selected.get())
			hideBubble();
	}
	
	//DALŠÍ METODY
	
	//upravené relocate, abychom brali střed objektu, nikoliv jeho okraj
	public void relocateGroup(double x, double y) {
//		double newX = x - (rectangle.getWidth()/2);
//		double newY = y - (rectangle.getHeight()/2);
//		this.relocate(newX, newY);
//		this.setX(newX);
//		this.setY(newY);
		this.setX(x);
		this.setY(y);
		this.relocate(x, y);
	}
	
	private void bindLabelToShapeSize() {
		if (shape instanceof Rectangle) {
			label.scaleXProperty().bind(((Rectangle)shape).widthProperty().divide(110));
			label.scaleYProperty().bind(((Rectangle)shape).widthProperty().divide(110));
		}
		else if (shape instanceof Circle) {
			label.scaleXProperty().bind(((Circle)shape).radiusProperty().divide(40));
			label.scaleYProperty().bind(((Circle)shape).radiusProperty().divide(40));
		}
		else if(shape instanceof Ellipse) {
			label.scaleXProperty().bind(((Ellipse)shape).radiusXProperty().divide(52));
			label.scaleYProperty().bind(((Ellipse)shape).radiusXProperty().divide(52));
		}
	}
	
	public void hideBubble() {
		bubblePopUp.hide();
	}
}
