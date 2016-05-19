package com.htmlhifive.pitalium.explorer.image;

import java.awt.Rectangle;

/**
 * This class has information about location shift of given rectangle area.
 */
public class ComparedRectangle {

//	private static enum RectType { UNCHECKED, SHIFT, SIMILAR }

	private int x;
	private int y;
	private int width;
	private int height;
	private String type = "UNCHECKED";
//	private RectType type = RectType.UNCHECKED;

	/**
	 * The negative value of xShift means the left side, the positive value of it means the right side,
	 * and the negative value of yShift means the upper side, the positive value of it means the lower side.
	 */
	private int xShift = 0;
	private int yShift = 0;
	
	// difference norm method comparing Pixel by Pixel
	private SimilarityUnit similarityUnit;

	/**
	 * Constructor
	 * Set the area information.
	 * @param rectangle the rectangle area of template image
	 */
	public ComparedRectangle(Rectangle rectangle)  {
		this.x = (int) rectangle.getX();
		this.y = (int) rectangle.getY();
		this.width = (int) rectangle.getWidth();
		this.height= (int) rectangle.getHeight();
	}

	/**
	 * Constructor
	 * Set the shift information when the template image is contained in the entire image.
	 * @param rectangle the rectangle area of template image
	 * @param xShift how many pixels the template image is shifted rightward
	 * @param yShift how many pixels the template image is shifted downward
	 */
	public ComparedRectangle(Rectangle rectangle, int xShift, int yShift) {
		this(rectangle);
		this.xShift = xShift;
		this.yShift = yShift;
		this.type = "SHIFT";
	}
	public ComparedRectangle(int x, int y, int width, int height, String type, int xShift, int yShift,
							 SimilarityUnit similarityUnit){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.type = type;
		this.xShift = xShift;
		this.yShift = yShift;
		this.similarityUnit = similarityUnit;
	}
	public ComparedRectangle(){
		this(0, 0, 0, 0, "UNCHECKED", 0, 0, null);
	}

	
//	/**
//	 * insert the similarity unit
//	 */
//	public void setSimilarity(int index, int XShift, int YShift, double similarity) {
//		if (isSimilar()) {
//
//			SimilarityUnit newUnit = new SimilarityUnit(XShift, YShift, similarity);
//			switch (index) {
//				case 1:
//					method1 = newUnit;
//					break;
//				case 2:
//					method2 = newUnit;
//					break;
//				case 3:
//					method3 = newUnit;
//					break;
//				default:
//				break;
//			}
//		}
//	}
//
//
//
//
//	/**
//	 * get the similarity of index-th method
//	 * @param index means the index of method : 1-Pixel-By-Pixel 2-Counting 3-FeatureMatrix
//	 * @return -1 if not valid, 1 if shifted, or similarity otherwise.
//	 */
//	public double getSimilarity(int index) {
//		if (!isChecked())
//			return -1;
//		else if (isShifted())
//			return 1;
//		else
//			switch (index) {
//				case 1:
//					return method1.getSimilarity();
//				case 2:
//					return method2.getSimilarity();
//				case 3:
//					return method3.getSimilarity();
//				default:
//					return -1;
//			}
//	}
//
//	/**
//	 * get XSimilar at the best match of index-th method
//	 * @param index means the index of method : 1-Pixel-By-Pixel 2-Counting 3-FeatureMatrix
//	 * @return XSimilar at the best match 
//	 */
//	public int getXSimilar(int index) {
//		if (!isChecked())
//			return 0;
//		else if (isShifted())
//			return 1;
//		else
//			switch (index) {
//				case 1:
//					return method1.getXSimilar();
//				case 2:
//					return method2.getXSimilar();
//				case 3:
//					return method3.getXSimilar();
//				default:
//					return 0;
//			}
//	}
//
//	/**
//	 * get YSimilar at the best match of index-th method
//	 * @param index means the index of method : 1-Pixel-By-Pixel 2-Counting 3-FeatureMatrix
//	 * @return YSimilar at the best match 
//	 */
//	public int getYSimilar(int index) {
//		if (!isChecked())
//			return 0;
//		else if (isShifted())
//			return 1;
//		else
//			switch (index) {
//				case 1:
//					return method1.getYSimilar();
//				case 2:
//					return method2.getYSimilar();
//				case 3:
//					return method3.getYSimilar();
//				default:
//					return 0;
//			}
//	}
//
//
	/**
	 * check if index-th method is available
	 * @param index means the index of method : 1-Pixel-By-Pixel 2-Counting 3-FeatureMatrix
	 * @return true if it's not null
	 */

//	public boolean checkAvailable(int index) {
//		if (!isChecked())
//			return false;
//		else if (isShifted())
//			return false;
//		else
//			switch (index) {
//				case 1:
//					return method1 != null;
//				case 2:
//					return method2 != null;
//				case 3:
//					return method3 != null;
//				default:
//					return false;
//			}	
//	}

	/**
	 * @return true if this rectangle is shifted
	 */
//	public boolean isShifted() {
////		return (type == RectType.SHIFT);
//		return type.equals("SHIFT");
//	}
//
//	/**
//	 * @return true if this rectangle is not shifted but similar
//	 */
//	public boolean isSimilar() {
////		return (type == RectType.SIMILAR);
//		return type.equals("SIMILAR");
//	}
//	
//	/**
//	 * @return true if this rectangle is unchecked
//	 */
//	public boolean isChecked() {
////		return (type != RectType.UNCHECKED);
//		return type.equals("UNCHECKED");
//	}

	/**
	 *	@return rectangle area
	 */
	public Rectangle rectangle(){
		return new Rectangle(this.x, this.y, this.width, this.height);
	}
	
	
	public int getX(){
		return x;
	}
	public void setX(int x){
		this.x = x;
	}
	
	public int getY(){
		return y;
	}
	public void setY(int y){
		this.y = y;
		
	}
	public int getWidth(){
		return width;
	}
	public void setWidth(int width){
		this.width = width;
	}
	public int getHeight(){
		return height;
	}
	public void setHeight(int height){
		this.height = height;
	}
	
	public String getType(){
		return type;
	}
	public void setType(String type){
		this.type = type;
	}

	public int getXShift() {
		return xShift;
	}
	public void setXShift(int xShift){
		this.xShift = xShift;
	}

	public int getYShift() {
		return yShift;
	}
	public void setYShift(int yShift){
		this.yShift = yShift;
	}
	
	public SimilarityUnit getSimilarityUnit(){
		return similarityUnit;
	}
	public void setSimilarityUnit(SimilarityUnit similarityUnit){
		this.similarityUnit = similarityUnit;
	}
}






