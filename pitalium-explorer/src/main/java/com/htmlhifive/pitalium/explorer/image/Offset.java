package com.htmlhifive.pitalium.explorer.image;

/**
 * offset class which contains x and y-coordinates
 */
public class Offset {
	
	private int x;
	private int y;

	/**
	 * Constructor
	 * @param x	offsetX
	 * @param y	offsetY
	 */
	public Offset(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return offsetX
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return offsetY
	 */
	public int getY() {
		return y;
	}
}
