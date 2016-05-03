package com.htmlhifive.pitalium.explorer.image;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.pitalium.image.model.DiffPoints;
import com.htmlhifive.pitalium.image.util.ImageUtils;
import com.htmlhifive.pitalium.image.util.MarkerGroup;

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
		List<Rectangle> rectangles = this.convertDiffPointsToAreas(DP);	

		// compare two images using given rectangle areas
		LocationShift LS = new LocationShift(expectedImage, actualImage, rectangles);
		LS.execute();
		ComparedRectangles = LS.getComparedRectangles();
		entireSimilarity = LS.getEntireSimilarity ();
	}
	
	public List<Rectangle> convertDiffPointsToAreas(DiffPoints DP){
		List<Point> diffPoints = DP.getDiffPoints();
		if (diffPoints == null || diffPoints.isEmpty()) {
			return new ArrayList<Rectangle>();
		}

		List<MarkerGroup> diffGroups = new ArrayList<MarkerGroup>();
		List<MarkerGroup> stack = new ArrayList<MarkerGroup>();

		for (Point p : diffPoints){
			MarkerGroup newGroup = new MarkerGroup(new Point(p.x, p.y));
			for(MarkerGroup group : diffGroups){
				if (group.canMarge(newGroup)){
					stack.add(group);
				}
			}
			for(MarkerGroup group : stack){
				newGroup.union(group);
				diffGroups.remove(group);
			}
			stack.removeAll(stack);
			diffGroups.add(newGroup);
		}

		List<Rectangle> rectangles = new ArrayList<Rectangle>();

		for (MarkerGroup markerGroup : diffGroups) {
			rectangles.add(markerGroup.getRectangle());
		}

		return rectangles;
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
				Rectangle rect = ComparedRect.rectangle();
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
				Rectangle rect = ComparedRect.rectangle();
				if(ComparedRect.checkAvailable(1)){
					SimilarityUnit method1 = ComparedRect.getMethod1();
					System.out.printf("x:%d, y:%d, w:%d, h:%d => %.2f at x:%d, y:%d shifted (Difference Norm)\n",
							(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), method1.getSimilarity(), method1.getXSimilar(), method1.getYSimilar());
				}
				if(ComparedRect.checkAvailable(2)){
					SimilarityUnit method2 = ComparedRect.getMethod2();
					System.out.printf("x:%d, y:%d, w:%d, h:%d => %.2f at x:%d, y:%d shifted (Number of Diffs)\n",
							(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), method2.getSimilarity(), method2.getXSimilar(), method2.getYSimilar());
				}
				if(ComparedRect.checkAvailable(3)){
					SimilarityUnit method3 = ComparedRect.getMethod3();
					System.out.printf("x:%d, y:%d, w:%d, h:%d => %.2f at x:%d, y:%d shifted (Feature Matrix)\n",
							(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), method3.getSimilarity(), method3.getXSimilar(), method3.getYSimilar());
					System.out.println();
				}
			}
		}
		System.out.printf("entire similarity : %.2f\n", entireSimilarity);
	
	}


}
