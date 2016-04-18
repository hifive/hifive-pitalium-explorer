package com.htmlhifive.pitalium.explorer.image;


import com.htmlhifive.pitalium.image.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;


/**
 * For given two images which are expected and actual,
 * this class check whether sub image of actual image is shifted
 * and matched at some position in expected image.
 * Then it stores ShiftRectangle which is the information of location shift of given area.
 */
public class LocationShift {

	/**
	 * The maximal range of location shift.
	 */
	private static final int MAXIMUM_SHIFT = 5;
	
	private BufferedImage expectedImage;
	private BufferedImage actualImage;

	private List<ShiftRectangle> ShiftRectangles;
	private List<Rectangle> rectangles;
	
	/**
	 * Constructor
	 * @param expectedImage The image which we will compare with the template
	 * @param actualImage The image which we will take the template
	 * @param rectangles This rectangles represent area where two images are different.
	 */
	public LocationShift(BufferedImage expectedImage, BufferedImage actualImage, List<Rectangle> rectangles) {
		this.expectedImage = expectedImage;
		this.actualImage = actualImage;
		this.rectangles = rectangles;
		this.ShiftRectangles = new ArrayList<ShiftRectangle>();
	}
	
	/**
	 * Check every rectangle and fill the list of ShiftRectangle with every shifted area
	 */
	public void excute() {
		for (Rectangle rectangle : rectangles)
		{
			CheckShift(rectangle);
		}
	}
	
	/**
	 * Check the template is contained and shifted in this area
	 * if then, create ShiftRectangle and insert it into ShiftRectangles list.
	 * @param rectangle One of the areas where two images are different.
	 */
	private void CheckShift (Rectangle rectangle)
	{
		BufferedImage entireImage = getSubImage(expectedImage, rectangle);
		BufferedImage templateImage = getSubImage(actualImage, getTemplateArea(rectangle));
		
		
		double[][] integralImage = calcIntegralImage(entireImage);

		double sumTemplate = 0;
		Raster r = templateImage.getRaster();
		
		int[] dArray = new int[r.getNumDataElements()];
		for (int x = 0; x < r.getWidth(); x++) {
			for (int y = 0; y < r.getHeight(); y++) {
				sumTemplate += r.getPixel(x, y, dArray)[0];
			}
		}

		int templateWidth = templateImage.getWidth();
		int templateHeight = templateImage.getHeight();
		double topLeft, topRight, bottomLeft, bottomRight;
		double sumEntire;
		final int yMax = entireImage.getHeight() - templateImage.getHeight() + 1;
		final int xMax = entireImage.getWidth() - templateImage.getWidth() + 1;
		for (int y = 0; y < yMax; y++) {
			for (int x = 0; x < xMax; x++) {
				bottomRight = integralImage[y + templateHeight - 1][x + templateWidth - 1];
				bottomLeft = (x == 0) ? 0 : integralImage[y + templateHeight - 1][x - 1];
				topRight = (y == 0) ? 0 : integralImage[y - 1][x + templateWidth - 1];
				topLeft = (x == 0 || y == 0) ? 0 : integralImage[y - 1][x - 1];
				sumEntire = bottomRight - bottomLeft - topRight + topLeft;

				if (Double.compare(sumEntire, sumTemplate) == 0) {
					BufferedImage cropEntire = entireImage.getSubimage(x, y, templateWidth, templateHeight);
					
					// If the template match at this position, create new ShiftRectangle and add it in the list
					if (ImageUtils.imageEquals(cropEntire, templateImage)) {
						ShiftRectangle newMatch = new ShiftRectangle(rectangle, x-MAXIMUM_SHIFT, y-MAXIMUM_SHIFT);
						ShiftRectangles.add(newMatch);
						return;
					}
				}
			}
		}
	}


	/**
	 * Transform given rectangle area into the template area
	 * @param rectangle The different area of two images
	 * @return the template area
	 */
	public Rectangle getTemplateArea (Rectangle rectangle)
	{
		int x = (int) rectangle.getX() + MAXIMUM_SHIFT;
		int y = (int) rectangle.getY() + MAXIMUM_SHIFT;
		int w = (int) rectangle.getWidth() - 2*MAXIMUM_SHIFT;
		int h = (int) rectangle.getHeight() - 2*MAXIMUM_SHIFT;
		
		return new Rectangle(x,y,w,h);
	}
	
	/**
	 * calculate integral value of given image
	 * @param source source image
	 * @return integral value of source image
	 */
	private static double[][] calcIntegralImage(BufferedImage source) {
		double[][] integralImage = new double[source.getHeight()][source.getWidth()];
		Raster raster = source.getRaster();
		int[] pixel = new int[raster.getNumDataElements()];
		double leftNum;
		double upNum;
		double leftUpNum;
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				leftNum = (x == 0) ? 0 : integralImage[y][x - 1];
				upNum = (y == 0) ? 0 : integralImage[y - 1][x];
				leftUpNum = (x == 0 || y == 0) ? 0 : integralImage[y - 1][x - 1];
				integralImage[y][x] = leftNum + upNum + raster.getPixel(x, y, pixel)[0] - leftUpNum;
			}
		}
		return integralImage;
	}	
	
	/**
	 * get sub image from given image and rectangle
	 * @param image
	 * @param rectangle 
	 * @return
	 */
	private BufferedImage getSubImage(BufferedImage image, Rectangle rectangle) {
		return image.getSubimage((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getWidth(), (int) rectangle.getHeight());
	}
	
}
