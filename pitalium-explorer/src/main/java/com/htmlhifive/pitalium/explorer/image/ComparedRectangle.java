package com.htmlhifive.pitalium.explorer.image;

import java.awt.Rectangle;

/**
 * This class has information about location shift of given rectangle area.
 */
public class ComparedRectangle {

	private static enum RectType { UNCHECKED, SHIFT, SIMILAR }

	private int x;
	private int y;
	private int width;
	private int height;

	private RectType type = RectType.UNCHECKED;

	/**
	 * The negative value of xShift means the left side, the positive value of it means the right side,
	 * and the negative value of yShift means the upper side, the positive value of it means the lower side.
	 */
	private int xShift = 0;
	private int yShift = 0;
	
	// difference norm method comparing Pixel by Pixel
	private SimilarityUnit method1;

	// the second method counting the number of different pixels
	private SimilarityUnit method2;

	// feature Matrix method
	private SimilarityUnit method3;

	/**
	 * Constructor
	 * Set the smilarity information when the template image is not contained in the entire image.
	 * @param rectangle the rectangle area of template image
	 */
	public ComparedRectangle(Rectangle rectangle)  {
		this.x = (int) rectangle.getX();
		this.y = (int) rectangle.getY();
		this.width = (int) rectangle.getWidth();
		this.height= (int) rectangle.getHeight();
		this.type = RectType.SIMILAR;
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
		this.type = RectType.SHIFT;
	}
	public ComparedRectangle(int x, int y, int width, int height, RectType type, int xShift, int yShift,
							 SimilarityUnit method1, SimilarityUnit method2, SimilarityUnit method3){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.type = type;
		this.xShift = xShift;
		this.yShift = yShift;
		this.method1 = method1;
		this.method2 = method2;
		this.method3 = method3;
	}
	public ComparedRectangle(){
		this(0, 0, 0, 0, RectType.UNCHECKED, 0, 0, null, null, null);
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

	public boolean checkAvailable(int index) {
		if (!isChecked())
			return false;
		else if (isShifted())
			return false;
		else
			switch (index) {
				case 1:
					return method1 != null;
				case 2:
					return method2 != null;
				case 3:
					return method3 != null;
				default:
					return false;
			}	
	}

	/**
	 * @return true if this rectangle is shifted
	 */
	public boolean isShifted() {
		return (type == RectType.SHIFT);
	}

	/**
	 * @return true if this rectangle is not shifted but similar
	 */
	public boolean isSimilar() {
		return (type == RectType.SIMILAR);
	}
	
	/**
	 * @return true if this rectangle is unchecked
	 */
	public boolean isChecked() {
		return (type != RectType.UNCHECKED);
	}

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
	
	public RectType getType(){
		return type;
	}
	public void setType(RectType type){
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
	
	public SimilarityUnit getMethod1(){
		return method1;
	}
	public void setMethod1(SimilarityUnit method1){
		this.method1 = method1;
	}
	public SimilarityUnit getMethod2(){
		return method2;
	}
	public void setMethod2(SimilarityUnit method2){
		this.method2 = method2;
	}
	public SimilarityUnit getMethod3(){
		return method3;
	}
	public void setMethod3(SimilarityUnit method3){
		this.method3 = method3;
	}
}






