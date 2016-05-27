package com.htmlhifive.pitalium.explorer.image;

import com.htmlhifive.pitalium.image.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;

/**
 * For given two images which are expected and actual,
 * this class check whether subimage of actual image is shifted
 * and matched at some position in expected image.
 * Then it stores ShiftRectangle which is the information of location shift of given area.
 */
public class LocationShift {

	/**
	 * The maximal range of location shift.
	 */
	private static final int MAXIMUM_SHIFT = 5;
	private static final int TEMPLATE_MARGIN = 5;

	private BufferedImage expectedImage;
	private BufferedImage actualImage;

	private List<ComparedRectangle> ComparedRectangles;
	private List<Rectangle> rectangles;
	
	private static double entireSimilarity = 0;

	/**
	 *The size of result image is set as minimum width and height of given two images.
	 */
	public static int minWidth;
	public static int minHeight;

	// Q. After reshaping, what is minimum width and height of rectangle?
	// Do we have to set this value?
	public static double minLength = 3;

	// image save option
	public boolean save = true;

	/**
	 * Constructor
	 * @param expectedImage The image which we will compare with the template.
	 * @param actualImage The image which we will take the template.
	 * @param rectangles This rectangles represent area where two images are different.
	 */
	public LocationShift(BufferedImage expectedImage, BufferedImage actualImage, List<Rectangle> rectangles) {
		this.expectedImage = expectedImage;
		this.actualImage = actualImage;
		this.rectangles = rectangles;
		this.ComparedRectangles = new ArrayList<ComparedRectangle>();

		this.minWidth = expectedImage.getWidth();
		if(minWidth > actualImage.getWidth())
			minWidth = actualImage.getWidth();

		this.minHeight = expectedImage.getHeight();
		if(minHeight > actualImage.getHeight())
			minHeight = actualImage.getHeight();
	}
	
	/**
	 * @return the list of result ComparedRectangles
	 */
	public List<ComparedRectangle> getComparedRectangles () {
		return ComparedRectangles;
	}

	/**
	 * @return the similarity of entire area of two images
	 */
	public double getEntireSimilarity () {
		return entireSimilarity;
	}

	/**
	 * execute comparison for every rectangle area.
	 * calculate entireSimilarity and build ComparedRectangles list.
	 */
	public void execute() {

		// variables for similarity
		double similarityPixelByPixel, similarityFeatureMatrix;
		BufferedImage	expectedSubImage,	actualSubImage;
		double entireDifference = 0;

		for (Rectangle rectangle : rectangles)
		{
			// if the rectangle is still useful after reshping, check shift.
			reshapeRect(rectangle);
			if (checkRect(rectangle) && checkRect(getTemplateArea(rectangle))) {

				// get subImages
				expectedSubImage = getExpectedSubImage(rectangle);
				actualSubImage = getActualSubImage(rectangle);


				/** if this rectangle is shift, then process shift information in CheckShift method **/
				if (CheckShift(rectangle))
					continue;

				/** else calculate similarity **/
				else {

					// construct new similar rectangle
					ComparedRectangle newSimilar = new ComparedRectangle(rectangle);
		
					// implement all similarity calculations and categorization, and then build ComparedRectangle 
					similarityPixelByPixel = SimilarityUtils.calcSimilarity(expectedSubImage, actualSubImage, rectangle, newSimilar);

					// calculate the similarity of entire image using pixel by pixel method
					int actualArea = actualSubImage.getWidth() * actualSubImage.getHeight();

					if (SimilarityUtils.averageNorm) {
						entireDifference += (1-similarityPixelByPixel)*actualArea;
					} else {
						entireDifference += (1-similarityPixelByPixel)*(1-similarityPixelByPixel)*actualArea;
					}

					// insert the similar rectangle into the list of ComparedRectangles
					ComparedRectangles.add(newSimilar);
				}
			}
		}

		if (SimilarityUtils.averageNorm) {
			entireSimilarity = 1-entireDifference/((minWidth-2*MAXIMUM_SHIFT)*(minHeight-2*MAXIMUM_SHIFT));
		}	else {
			entireSimilarity = 1-Math.sqrt(entireDifference/((minWidth-2*MAXIMUM_SHIFT)*(minHeight-2*MAXIMUM_SHIFT)));
		}
	}

	/**
	 * Check if this rectangle is available
	 * @param rectangle the rectangle checked
	 * @return true if this rectangle area is available
	 */
	public static boolean checkRect(Rectangle rectangle)
	{
		return (rectangle.getX() < (minWidth-1) && rectangle.getY() < (minHeight-1) && rectangle.getWidth() >= minLength && rectangle.getHeight() >= minLength);
	}

	/**
	 * Reshape rectangle in order to avoid raster error
	 */
	private void reshapeRect(Rectangle rectangle)
	{
		double width= rectangle.getWidth(), height = rectangle.getHeight();
		double x = rectangle.getX(), y = rectangle.getY();
		
		if (x < 0) {
			width += x;
			x = 0;
		}
		if (y < 0) {
			height += y;
			y = 0;
		}
		if (x + width >= minWidth - minLength)
			width = minWidth - minLength - x;
		if (y + height >= minHeight - minLength)
			height = minHeight - minLength - y;
	
		rectangle.setRect(x, y, width, height);
	}

	/**
	 * Check the template is contained and shifted in this area
	 * if then, create ComparedRectangle with shift information and insert it into ComparedRectangles list.
	 * @param rectangle One of the areas where two images are different.
	 * @return true if thie rectangle is shifted
	 */
	private boolean CheckShift (Rectangle rectangle)
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
		int yMax = entireImage.getHeight() - templateImage.getHeight() + 1;
		int xMax = entireImage.getWidth() - templateImage.getWidth() + 1;
		for (int y = 0; y < yMax; y++) {
			for (int x = 0; x < xMax; x++) {
				bottomRight = integralImage[y + templateHeight - 1][x + templateWidth - 1];
				bottomLeft = (x == 0) ? 0 : integralImage[y + templateHeight - 1][x - 1];
				topRight = (y == 0) ? 0 : integralImage[y - 1][x + templateWidth - 1];
				topLeft = (x == 0 || y == 0) ? 0 : integralImage[y - 1][x - 1];
				sumEntire = bottomRight - bottomLeft - topRight + topLeft;

				if (Double.compare(sumEntire, sumTemplate) == 0) {
					BufferedImage cropEntire = entireImage.getSubimage(x, y, templateWidth, templateHeight);
					
					// If the template match at this position, create new ComparedRectangle and add it in the list
					if (ImageUtils.imageEquals(cropEntire, templateImage)) {
						ComparedRectangle newMatch = new ComparedRectangle(rectangle, x-TEMPLATE_MARGIN, y-TEMPLATE_MARGIN);
						ComparedRectangles.add(newMatch);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Transform given rectangle area into the template area
	 * @param rectangle The different area of two images
	 * @return the template area
	 */
	public static Rectangle getTemplateArea (Rectangle rectangle)
	{
		int x = (int) rectangle.getX() + TEMPLATE_MARGIN;
		int y = (int) rectangle.getY() + TEMPLATE_MARGIN;
		int w = (int) rectangle.getWidth() - 2*TEMPLATE_MARGIN;
		int h = (int) rectangle.getHeight() - 2*TEMPLATE_MARGIN;
		
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
	 * get subimage from given image and rectangle
	 * @param image
	 * @param rectangle 
	 * @return
	 */
	private BufferedImage getSubImage(BufferedImage image, Rectangle rectangle) {
		// Initialize variables
		int width= (int)rectangle.getWidth(), height = (int)rectangle.getHeight();
		int x = (int)rectangle.getX(), y = (int)rectangle.getY();
		
		return image.getSubimage(x, y, width, height);
	}

	/**
	 * return the subimage of the given rectangle area of expectedImage
	 * @param rectangle rectangle area where expectedImage is taken
	 * @return the subimage of expectedImage
	 */
	public BufferedImage getExpectedSubImage (Rectangle rectangle) {
		return getSubImage(expectedImage, rectangle);
	}

	/**
	 * return the subimage of the template size of given rectangle area of actualImage
	 * @param rectangle rectangle area where actualImage is taken
	 * @return the subimage of actualImage
	 */
	public BufferedImage getActualSubImage (Rectangle rectangle) {
		return getSubImage(actualImage, getTemplateArea(rectangle));
	}
	
	/**
	 * @return TEMPLATE_MARGIN
	 */
	public static int getTemplateMargin() {
		return TEMPLATE_MARGIN;
	}
}


