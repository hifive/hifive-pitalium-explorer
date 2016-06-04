package com.htmlhifive.pitalium.explorer.image;

import com.htmlhifive.pitalium.image.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.List;
import java.lang.Math;

/**
 * For given two images which are expected and actual,
 * this class check whether sub-image of actual image is shifted
 * and matched at some position in expected image.
 * Then it stores ShiftRectangle which is the information of location shift of given area.
 */
public class ShiftUtils {

	/**
	 * Constructor
	 */
	public ShiftUtils() {};
	

	/**
	 * Check the sub-image of actualImage in the given rectangle area is contained in expectedImage at the same or nearby location
	 * if then, create ComparedRectangle with shift information and insert it into ComparedRectangles list.
	 * @param expectedImage
	 * @param actualImage
	 * @param ComparedRectangles list of ComparedRectangle
	 * @param rectangle	sub-image area of actual image
	 * @return	true if this rectangle is shifted
	 */
	public static boolean CheckShift (BufferedImage expectedImage, BufferedImage actualImage, List<ComparedRectangle> ComparedRectangles, Rectangle rectangle)
	{
		int minWidth = Math.min(expectedImage.getWidth(), actualImage.getWidth()),
			minHeight = Math.min(expectedImage.getHeight(), actualImage.getHeight());
		
		// set range to be checked
		int x = (int)rectangle.getX(), y = (int)rectangle.getY(),
			w = (int)rectangle.getWidth(), h = (int) rectangle.getHeight();
		int maxShift = ComparisonParameters.getMaxShift();
		int leftMove = Math.min(maxShift, x-1),
			rightMove = Math.min(maxShift, minWidth-(x+w)),
			topMove = Math.min(maxShift, y-1),
			downMove = Math.min(maxShift, minHeight-(y+h));
		Rectangle entireFrame = new Rectangle(x-leftMove,y-topMove,w+leftMove+rightMove,h+topMove+downMove);
		BufferedImage entireImage = ImageUtils2.getSubImage(expectedImage, entireFrame);
		BufferedImage templateImage = ImageUtils2.getSubImage(actualImage, rectangle);
		
		double[][] integralImage = calcIntegralImage(entireImage);

		double sumTemplate = 0;
		Raster r = templateImage.getRaster();
		
		int[] dArray = new int[r.getNumDataElements()];
		for (int i = 0; i < r.getWidth(); i++) {
			for (int j = 0; j < r.getHeight(); j++) {
				sumTemplate += r.getPixel(i, j, dArray)[0];
			}
		}

		int templateWidth = templateImage.getWidth();
		int templateHeight = templateImage.getHeight();
		double topLeft, topRight, bottomLeft, bottomRight;
		double sumEntire;

		for (int i = 0; i <= topMove + downMove; i++) {
			for (int j = 0; j <= leftMove + rightMove; j++) {
				bottomRight = integralImage[i + templateHeight - 1][j + templateWidth - 1];
				bottomLeft = (j == 0) ? 0 : integralImage[i + templateHeight - 1][j - 1];
				topRight = (i == 0) ? 0 : integralImage[i - 1][j + templateWidth - 1];
				topLeft = (j == 0 || i == 0) ? 0 : integralImage[i - 1][j - 1];
				sumEntire = bottomRight - bottomLeft - topRight + topLeft;

				if (Double.compare(sumEntire, sumTemplate) == 0) {
					BufferedImage cropEntire = entireImage.getSubimage(j, i, templateWidth, templateHeight);
					
					// If the template matches at this position, create new ComparedRectangle and add it in the list
					if (ImageUtils.imageEquals(cropEntire, templateImage)) {
						ComparedRectangle newMatch = new ComparedRectangle(rectangle, j-leftMove, i-topMove);
						ComparedRectangles.add(newMatch);
						return true;
					}
				}
			}
		}
		return false;
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
}


