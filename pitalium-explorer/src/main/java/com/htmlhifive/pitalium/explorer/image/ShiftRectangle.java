package com.htmlhifive.pitalium.explorer.image;

import java.awt.Rectangle;

/**
 * This class has information about location shift of given rectangle area.
 */
public class ShiftRectangle {
	
	private Rectangle rectangle;

	/**
	 * The negative value of xShift means the left side, the positive value of it means the right side,
	 * and the negative value of yShift means the upper side, the positive value of it means the lower side.
	 */
	private int xShift = 0;
	private int yShift = 0;

	
	/**
	 * Constructor
	 * Set the shift information when the template image is contained in the entire image.
	 * @param rectangle the rectangle area of template image
	 * @param xShift how many pixels the template image is shifted rightward
	 * @param yShift how many pixels the template image is shifted downward
	 */
	public ShiftRectangle(Rectangle rectangle, int xShift, int yShift) {
		this.rectangle = rectangle;
		this.xShift = xShift;
		this.yShift = yShift;
	}


	/**
	 *	@return rectangle of different position
	 */
	public Rectangle getRectangle(){
		return rectangle;
	}

	/**
	 *	@return rightward shift
	 */
	public int getX() {
		return xShift;
	}

	/**
	 *	@return downward
	 */
	public int getY() {
		return yShift;
	}


}






