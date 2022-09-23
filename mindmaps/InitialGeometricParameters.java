package mindmaps;

/**
 * Tato třída v sobě uchovává iniciální geometrické hodnoty grafického uzlu (pozici a rozměry) před tím, než je uzel dynamicky upravován uživatelem
 * skrze posouvání nebo zvětšování pomocí zvětšovacího kolečka. Tyto hodnoty se do této třídy zabalí např. v případě vytvoření nového uzlu, jeho editace
 * nebo v případě načtení uzlů z xml souboru pomocí třídy Deserializer.
 * Třída byla vytvořena za účelem zpřehlednění předávání těchto hodnot jakožto argumentů v metodách, které s těmito hodnotami operují.
 */
public class InitialGeometricParameters {
	
	//iniciální pozice mindNodu před tím, než s ním jakkoliv začneme posouvat
	private double initX;
	private double initY;
	
	//iniciální velikost mindNodu, než ho začneme zvětšovat
	private double initWidth;
	private double initHeight;
	
	public InitialGeometricParameters(double x, double y, double width, double height) {
		initX = x;
		initY = y;
		initWidth = width;
		initHeight = height;
	}
	
	//GETTERY
	public double getInitX() {
		return initX;
	}
	
	public double getInitY() {
		return initY;
	}
	
	public double getInitWidth() {
		return initWidth;
	}
	
	public double getInitHeight() {
		return initHeight;
	}
	
	
}
