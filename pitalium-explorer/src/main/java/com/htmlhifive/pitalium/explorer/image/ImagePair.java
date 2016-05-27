package com.htmlhifive.pitalium.explorer.image;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.pitalium.image.model.DiffPoints;
import com.htmlhifive.pitalium.image.util.ImageUtils;

public class ImagePair {


	private List<ComparedRectangle> ComparedRectangles;
	private double entireSimilarity;
	
	// Default criteria to split over-merged rectangle
	private static final int BORDER_WIDTH = 10;
	private static final int OVERMERGED_WIDTH = 200;
	private static final int OVERMERGED_HEIGHT = 300;
	
	/**
	 * Constructor
	 * Execute every comparison steps of two given images,
	 * build ComparedRectangles list, and calculate entireSimilarity.
	 */
	public ImagePair(BufferedImage expectedImage, BufferedImage actualImage) {
		
		Rectangle expectedFrame = new Rectangle(expectedImage.getWidth(), expectedImage.getHeight());
		Rectangle actualFrame = new Rectangle(actualImage.getWidth(), actualImage.getHeight());

		// To check running time
		long startTime, endTime;
		boolean printRunningTime = false;
		

		// get different points
		startTime = System.currentTimeMillis();
		DiffPoints DP = ImageUtils.compare(expectedImage, expectedFrame, actualImage, actualFrame, null);
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("DP:%d ",endTime-startTime);
		}
		
		// convert different points to rectangle areas
		startTime = System.currentTimeMillis();
		List<Rectangle> rectangles = this.convertDiffPointsToAreas(DP, 10);	
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("AREA:%d ",endTime-startTime);
		}
		
		// split over-merged rectangles into smaller ones if possible
		startTime = System.currentTimeMillis();
		SplitRectangles (expectedImage, actualImage, rectangles, 5);
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("SPLIT:%d ",endTime-startTime);
		}
		
		// compare two images using given rectangle areas
		startTime = System.currentTimeMillis();
		LocationShift LS = new LocationShift(expectedImage, actualImage, rectangles);
		LS.execute();
		ComparedRectangles = LS.getComparedRectangles();
		entireSimilarity = LS.getEntireSimilarity ();
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("CMP:%d\n",endTime-startTime);
		}

	}
	
	/**
	 * Check if given rectangle is bigger than over-merged rectangle criteria
	 * @param rectangle Rectangle
	 * @return true if it is over-merged
	 */
	private boolean canSplit (Rectangle rectangle) {
		int width = (int)rectangle.getWidth(), height = (int)rectangle.getHeight();
		return (width >= OVERMERGED_WIDTH && height >= OVERMERGED_HEIGHT);	
	}
	
	/**
	 * Split rectangles which are over-merged into smaller ones if possible
	 * 
	 * @param expectedImage
	 * @param actualImage
	 * @param rectangles list of Rectangles
	 * @param splitIteration Iteration number for split implementation
	 */
	public void SplitRectangles (BufferedImage expectedImage, BufferedImage actualImage, List<Rectangle> rectangles, int splitIteration) {
		
		// Terminate recursion after splitIteration-times
		if (splitIteration < 1) {
			return;
		}
		
		int TEMPLATE_MARGIN = LocationShift.getTemplateMargin();	// To extract ACTUAL different region
		int sub_margin = TEMPLATE_MARGIN + BORDER_WIDTH;			// Remove border from actual different region
		List<Rectangle> removeList = new ArrayList<Rectangle>();
		List<Rectangle> addList = new ArrayList<Rectangle>();
		
		// split implementation for each rectangle
		for (Rectangle rectangle : rectangles) {
			
			// check if this rectangle can be split
			if (canSplit(rectangle)) {
				
				// get sub rectangle by subtracting border information 
				int subX = (int)rectangle.getX() + sub_margin;
				int subY = (int)rectangle.getY() + sub_margin;
				int subWidth = (int)rectangle.getWidth() - 2*sub_margin;
				int subHeight = (int)rectangle.getHeight() - 2*sub_margin;
				Rectangle subRectangle = new Rectangle(subX, subY, subWidth, subHeight);
				
				DiffPoints subDiffPoints = ImageUtils.compare(expectedImage, subRectangle, actualImage, subRectangle, null);
				
				// use smaller value to union Rectangle Area than what we used for the first different area recognition
				List<Rectangle> splitRectangles = this.convertDiffPointsToAreas(subDiffPoints, 2);		
								
				// if split succeed
				if (splitRectangles.size() != 1 || !subRectangle.equals(splitRectangles.get(0))) {
					
					// if there exists splitRectangle which is still over-merged, split it recursively
					SplitRectangles (expectedImage, actualImage, splitRectangles, splitIteration-1);
					
					// Record the rectangles which will be removed and added 
					for (Rectangle splitRectangle : splitRectangles) {
						
						// expand splitRectangle if it borders on subRectangle
						expand(subRectangle, splitRectangle, sub_margin);
						
						addList.add(splitRectangle);
					}
					removeList.add(rectangle);
				}
			}
		}
		
		
		// remove recorded rectangles
		for (Rectangle rectangle : removeList) {
			rectangles.remove(rectangle);
		}

		// add recorded rectangles
		for (Rectangle rectangle : addList) {
			rectangles.add(rectangle);
		}
	}
	

	/**
	 *  Expand the splitRectangle, if it borders on subRectangle, as much as border removed 
	 *  
	 * @param subRectangle Rectangle for checking expansion
	 * @param splitRectangle Rectangle which is expanded
	 * @param sub_margin how much border removed
	 */
	public void expand(Rectangle subRectangle, Rectangle splitRectangle, int sub_margin) {
		int subX = (int)subRectangle.getX(), subY = (int)subRectangle.getY();
		int subWidth = (int)subRectangle.getWidth(), subHeight = (int)subRectangle.getHeight();
		int splitX = (int)splitRectangle.getX(), splitY = (int) splitRectangle.getY();
		int splitWidth = (int)splitRectangle.getWidth(), splitHeight = (int)splitRectangle.getHeight();

		// Left-directional expansion
		if (splitX <= subX) {
			splitX = subX - sub_margin;
			splitWidth = splitWidth + sub_margin;
		}
		
		// Top-directional expansion
		if (splitY <= subY) {
			splitY = subY - sub_margin;
			splitHeight = splitHeight + sub_margin;
		}
		
		// Right-directional expansion
		if (splitX + splitWidth >= subX + subWidth) {
			splitWidth = subX + subWidth + sub_margin - splitX;
		}
		
		// Down-directional expansion
		if (splitY + splitHeight >= subY + subHeight) {
			splitHeight = subY + subHeight + sub_margin - splitY;
		}
		
		splitRectangle.setBounds(splitX, splitY, splitWidth, splitHeight);
	}
	
	/**
	 * Convert DiffPoints to the list of Rectangle areas
	 * @param DP Diffpoints
	 * @param group_distance distance for grouping
	 * @return list of Rectangle areas
	 */
	public List<Rectangle> convertDiffPointsToAreas(DiffPoints DP, int group_distance){
		List<Point> diffPoints = DP.getDiffPoints();
		if (diffPoints == null || diffPoints.isEmpty()) {
			return new ArrayList<Rectangle>();
		}

		int mergeFlag = 0;
		List<ObjectGroup> diffGroups = new ArrayList<ObjectGroup>();

		// Merge diffPoints belongs to the same object into one objectGroup.
		for (Point point : diffPoints) {
			ObjectGroup objectGroup = new ObjectGroup(new Point(point.x, point.y), group_distance);
			for (ObjectGroup diffGroup : diffGroups) {
				if (diffGroup.canMerge(objectGroup)) {
					diffGroup.union(objectGroup);
					mergeFlag = 1;
					break;
				}
			}
			if (mergeFlag != 1) {
				diffGroups.add(objectGroup);
			}
			mergeFlag = 0;
		}
		
		// Count how many times merge occur for each case
		int num = -1;

		// loop until there is no merge
		while (num != 0) {
			num = 0;
			for (ObjectGroup rectangleGroup : diffGroups) {
				List<ObjectGroup> removeList = new ArrayList<ObjectGroup>();
				for (ObjectGroup rectangleGroup2 : diffGroups) {
					// Check if two distinct rectangles can be merged.
					if (!rectangleGroup.equals(rectangleGroup2) && rectangleGroup.canMerge(rectangleGroup2)) {
						rectangleGroup.union(rectangleGroup2);
						num++;
						// Record the rectangle which will be removed.
						removeList.add(rectangleGroup2);
					}
				}
				if (num > 0) {
					// Remove the merged rectangle.
					for (ObjectGroup removeModel : removeList) {
						diffGroups.remove(removeModel);
					}
					break;
				}
			}
		}
		
		// Create a list of the Rectangle from diffGroups
		List<Rectangle> rectangles = new ArrayList<Rectangle>();

		for (ObjectGroup objectGroup : diffGroups) {
			rectangles.add(objectGroup.getRectangle());
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
