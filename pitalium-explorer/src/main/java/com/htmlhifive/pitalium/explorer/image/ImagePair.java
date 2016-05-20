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

	/**
	 * Convert DiffPoints to the list of Rectangle areas
	 * @param DP Diffpoints
	 * @return list of Rectangle areas
	 */
	public List<Rectangle> convertDiffPointsToAreas(DiffPoints DP){
		List<Point> diffPoints = DP.getDiffPoints();
		if (diffPoints == null || diffPoints.isEmpty()) {
			return new ArrayList<Rectangle>();
		}

		int mergeFlag = 0;
		List<MarkerGroup> diffGroups = new ArrayList<MarkerGroup>();

		// Merge diffPoints belongs to the same object into one markerGroup.
		for (Point point : diffPoints) {
			MarkerGroup markerGroup = new MarkerGroup(new Point(point.x, point.y));
			for (MarkerGroup diffGroup : diffGroups) {
				if (diffGroup.canMarge(markerGroup)) {
					diffGroup.union(markerGroup);
					mergeFlag = 1;
					break;
				}
			}
			if (mergeFlag != 1) {
				diffGroups.add(markerGroup);
			}
			mergeFlag = 0;
		}
		
		// Count how many times merge occur for each case
		int num = -1;

		// loop until there is no merge
		while (num != 0) {
			num = 0;
			for (MarkerGroup rectangleGroup : diffGroups) {
				List<MarkerGroup> removeList = new ArrayList<MarkerGroup>();
				for (MarkerGroup rectangleGroup2 : diffGroups) {
					// Check if two distinct rectangles can be merged.
					if (!rectangleGroup.equals(rectangleGroup2) && rectangleGroup.canMarge(rectangleGroup2)) {
						rectangleGroup.union(rectangleGroup2);
						num++;
						// Record the rectangle which will be removed.
						removeList.add(rectangleGroup2);
					}
				}
				if (num > 0) {
					// Remove the merged rectangle.
					for (MarkerGroup removeModel : removeList) {
						diffGroups.remove(removeModel);
					}
					break;
				}
			}
		}
		
		// Create a list of the Rectangle from diffGroups
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
			if (ComparedRect.getType().equals("SHIFT")) {
				Rectangle rect = ComparedRect.rectangle();
				System.out.printf("x:%d, y:%d, w:%d, h:%d => shifted x:%d, y:%d\n",(int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), ComparedRect.getXShift(), ComparedRect.getYShift());
			}
		}
	}

}
