package com.htmlhifive.pitalium.explorer.image;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.util.List;

public class ImagePair {

	private List<ComparedRectangle> ComparedRectangles;
	private double entireSimilarity;

	/**
	 * Constructor
	 * Execute every comparison steps of two given images,
	 * build ComparedRectangles list, and calculate entireSimilarity.
	 */
	public ImagePair(BufferedImage expectedImage, BufferedImage actualImage) {
		
		Rectangle rectangle1 = new Rectangle(expectedImage.getWidth(), expectedImage.getHeight());
		Rectangle rectangle2 = new Rectangle(actualImage.getWidth(), actualImage.getHeight());

		// get different points
		DiffPoints DP = ImageUtils.compare(expectedImage, rectangle1, actualImage, rectangle2, null);

		// convert different points to rectangle areas
		List<Rectangle> rectangles = ImageUtils.convertDiffPointsToAreas(DP);	

		// compare two images using given rectangle areas
		LocationShift LS = new LocationShift(expectedImage, actualImage, rectangles);
		LS.execute();
		ComparedRectangles = LS.getComparedRectangles();
		entireSimilarity = LS.getEntireSimilarity ();
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
	 * print the shift rectangle information
 	 */
	public void printShiftRectangles() {
		for (ComparedRectangle ComparedRect : ComparedRectangles) {
			if (ComparedRect.isShifted()) {
				Rectangle rect = ComparedRect.getRectangle();
				System.out.printf("x:%d, y:%d, w:%d, h:%d => shifted x:%d, y:%d\n",(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), ComparedRect.getXShift(), ComparedRect.getYShift());
			}
		}
	}

	/**
	 * print the similar rectangle information
 	 */
	public void printSimilarRectangles() {
		for (ComparedRectangle ComparedRect : ComparedRectangles) {
			if (ComparedRect.isSimilar()) {
				Rectangle rect = ComparedRect.getRectangle();
				if(ComparedRect.checkAvailable(1))
					System.out.printf("x:%d, y:%d, w:%d, h:%d => %.2f at x:%d, y:%d shifted (Difference Norm)\n",(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), ComparedRect.getSimilarity(1), ComparedRect.getXSimilar(1), ComparedRect.getYSimilar(1));
				if(ComparedRect.checkAvailable(2))
				System.out.printf("x:%d, y:%d, w:%d, h:%d => %.2f at x:%d, y:%d shifted (Number of Diffs)\n",(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), ComparedRect.getSimilarity(2), ComparedRect.getXSimilar(2), ComparedRect.getYSimilar(2));
				if(ComparedRect.checkAvailable(3))
				System.out.printf("x:%d, y:%d, w:%d, h:%d => %.2f at x:%d, y:%d shifted (Feature Matrix)\n",(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), ComparedRect.getSimilarity(3), ComparedRect.getXSimilar(3), ComparedRect.getYSimilar(3));
				System.out.println();
			}
		}
		System.out.printf("entire similarity : %.2f\n", entireSimilarity);
	
	}


}
