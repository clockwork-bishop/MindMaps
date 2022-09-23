package mindmaps;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Tato třída představuje nosný zvětšovací obdélník grafického myšlenkového uzlu. Stará se o zachycení událostí kulatého handleru, kterým je možné
 * tento obdélník zvětšovat či zmenšovat.
 */
public class ResizableRectangle extends Rectangle {
	
	//komponenty ResizableRectanglu
	private Circle resizeHandle;
	
	//pozice myši
	private Point2D mousePosition;
	
	//KONSTANTY
	//minimální rozměry rectanglu (na které ho lze zmenšit)
	private final double MIN_WIDTH = 50;
	private final double MIN_HEIGHT = 50;
	
	public ResizableRectangle(double width, double height) {
		
		//nastavení vlastností rectanglu
		super(width, height);
		this.setStroke(Color.DIMGRAY);
		this.setOpacity(0.8);
		this.getStrokeDashArray().addAll(3.0, 7.0, 3.0, 7.0);
		this.setFill(Color.TRANSPARENT);
		
		//nastavení vlastností resizeHandle
		resizeHandle = new Circle();
		resizeHandle.setFill(Color.DIMGRAY);
		resizeHandle.setOpacity(50);
		resizeHandle.setRadius(3);
		
		//přichycení ovládacího kolečka na pravý dolní roh rectanglu
		resizeHandle.centerXProperty().bind(this.xProperty().add(this.widthProperty()));
		resizeHandle.centerYProperty().bind(this.yProperty().add(this.heightProperty()));
		
		//nastavení akcí handleru
		resizeHandle.setOnDragDetected(this::dragHandle);
		resizeHandle.setOnMouseReleased(this::releaseHandle);
		resizeHandle.setOnMouseDragged(this::scaleRectangle);
	}
	
	//AKCE HANDLERU
	public void dragHandle(MouseEvent ev) {
		resizeHandle.setCursor(Cursor.CLOSED_HAND);
		mousePosition = new Point2D(ev.getSceneX(), ev.getSceneY()); //nastavení výchozí pozice myši těsně před posunutím kolečkem (nesmí být null)
	}
	
	public void releaseHandle(MouseEvent ev) {
		resizeHandle.setCursor(Cursor.DEFAULT);
		mousePosition = null; //kolečkem jsme "doposouvali" -> výchozí pozice myši opět null
	}
	
	public void scaleRectangle(MouseEvent ev) {
		//provede se jen, pokud držíme handle (tzn. mousePosition != null)
		if (mousePosition != null) {
			//posun = (souřadnice po posunutí (event)) - (původní pozice myši při podržení kolečka (uchovaná v mousePosition))
			double changeX = ev.getSceneX() - mousePosition.getX();
			double changeY = ev.getSceneY() - mousePosition.getY();

			//nastavení nové šířky a délky rectanglu
			if ((this.getWidth() + changeX) < MIN_WIDTH)
				this.setWidth(MIN_WIDTH);
			else
				this.setWidth(this.getWidth() + changeX);

			if ((this.getHeight() + changeY) < MIN_HEIGHT)
				this.setHeight(MIN_HEIGHT);
			else
				this.setHeight(this.getHeight() + changeY);

			//přepočítání pozice myši
			mousePosition = new Point2D(ev.getSceneX(), ev.getSceneY());
		}
	}
	
	//GETTERY
	public Circle getResizeHandle() {
		return resizeHandle;
	}
	
	//SETTERY
	public void setHandleVisible(boolean bool) {
		resizeHandle.setVisible(bool);
	}
	
	public void setRectangleVisible(boolean bool) {
		this.setVisible(bool);
	}
}