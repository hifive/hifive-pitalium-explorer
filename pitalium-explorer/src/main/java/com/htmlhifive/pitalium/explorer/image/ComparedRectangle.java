package com.htmlhifive.pitalium.explorer.image;

import java.awt.Rectangle;

/**
 * This class has information about location shift of given rectangle area.
 */
public class ComparedRectangle {

	private int x;
	private int y;
	private int width;
	private int height;
	private String type = "UNCHECKED";

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
	
	/**
	 * Constructor
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param type
	 * @param xShift
	 * @param yShift
	 * @param similarityUnit
	 */
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






