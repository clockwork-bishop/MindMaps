package mindmaps;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * Tato třída uchovává vlastnosti grafického uzlu, které jsou nastavitelné od uživatele skrze dialogové okno.
 * Třída vznikla za účelem zjednodušení předávání těchto vlastností coby parametrů v metodách a usnadnění možnosti v budoucnu přidat další nastavitelné vlastnosti.
 */
public class MindNodeProfile {
	
	//jméno uzlu
	private String name;
	
	//barva uzlu
	private Color color;
	
	//barva textu uzlu
	private Color textColor;
	
	//tvar uzlu
	private Shape shape;
	
	//poznámka
	private String note;
	
	public MindNodeProfile(String name, Color color, Color textColor, Shape shape, String note) {
		this.name = name;
		this.color = color;
		this.textColor = textColor;
		this.shape = shape;
		this.note = note;
	}
	
	//GETTERY
	public String getName() {
		return name;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Color getTextColor() {
		return textColor;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public String getNote() {
		return note;
	}
	
	//SETTERY
	public void setName(String name) {
		this.name = name;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setTextColor(Color color) {
		this.textColor = color;
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
}