package com.htmlhifive.pitalium.explorer.image;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.pitalium.image.model.DiffPoints;
import com.htmlhifive.pitalium.image.util.ImageUtils;

public class ImagePair {

	BufferedImage expectedImage;
	BufferedImage actualImage;
	private List<ComparedRectangle> ComparedRectangles;
	private double entireSimilarity;
	
	// Default criteria to split over-merged rectangle
	private static final int BORDER_WIDTH = 10;
	private static final int OVERMERGED_WIDTH = 200;
	private static final int OVERMERGED_HEIGHT = 300;
		
	// Dominant offset between two images
	Offset offset;
	
	/**
	 * Constructor
	 */
	public ImagePair(BufferedImage expectedImage, BufferedImage actualImage) {
	
		double diffThreshold = 0.1;
		offset = ImageUtils2.findDominantOffset(expectedImage, actualImage, diffThreshold);
		this.expectedImage = ImageUtils2.getDominantImage (expectedImage, actualImage, offset);
		this.actualImage = ImageUtils2.getDominantImage (actualImage, expectedImage, offset);
		
		compareImagePair();
	}
	
	/**
	 * 	Execute every comparison steps of two given images,
	 *  build ComparedRectangles list, and calculate entireSimilarity.
	 */
	private void compareImagePair () {

		// initial group distance
		int group_distance = 10;
		
		Rectangle expectedFrame = new Rectangle(expectedImage.getWidth(), expectedImage.getHeight());
		Rectangle actualFrame = new Rectangle(actualImage.getWidth(), actualImage.getHeight());

		// Do not use sizeDiffPoints and consider only intersection area 
		Rectangle entireFrame = expectedFrame.intersection(actualFrame);
		
		// To check running time
		long startTime, endTime, totalTime = 0;
		boolean printRunningTime = true;
		
		// Find dominant offset
		startTime = System.currentTimeMillis();
		offset = ImageUtils2.findDominantOffset(expectedImage, actualImage, 0.1);
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("Dominant offset : x:%d y:%d\n", offset.getX(), offset.getY());
			System.out.printf("OFFSET:%d ",endTime-startTime);
			totalTime = totalTime + endTime-startTime;
		}

		
		
		// build different areas
		startTime = System.currentTimeMillis();
		List<Rectangle> rectangles  = buildDiffAreas(entireFrame, group_distance);
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("DIFF AREA:%d ",endTime-startTime);
			totalTime = totalTime + endTime-startTime;
		}

		// split over-merged rectangles into smaller ones if possible
		startTime = System.currentTimeMillis();
		SplitRectangles (rectangles, 5);
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("SPLIT:%d ",endTime-startTime);
			totalTime = totalTime + endTime-startTime;
		}

		// compare two images using given rectangle areas
		startTime = System.currentTimeMillis();
		LocationShift LS = new LocationShift(expectedImage, actualImage, rectangles);
		LS.execute();
		ComparedRectangles = LS.getComparedRectangles();
		entireSimilarity = LS.getEntireSimilarity ();
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("CMP:%d ",endTime-startTime);
			totalTime = totalTime + endTime-startTime;
			System.out.printf("TOTAL:%d\n",totalTime);
		}
		
	}
	
	/**
	 * build different rectangles in the given frame area
	 * @param frame boundary area to build rectangles
	 * @param group_distance distance for grouping
	 * @return list of rectangles representing different area
	 */
	private List<Rectangle> buildDiffAreas (Rectangle frame, int group_distance) {
		List<ObjectGroup> objectGroups = buildObjectGroups(frame, group_distance);
		List<Rectangle> rectangles = convertObjectGroupsToAreas(objectGroups);
		
		return rectangles;
	}
	
	
	/**
	 * build object groups for different areas in the given frame area
	 * @param frame	boundary area to build object
	 * @param group_distance distance for grouping
	 * @return list of object groups representing different area
	 */
	private List<ObjectGroup> buildObjectGroups (Rectangle frame, int group_distance) {
		
		// threshold for difference of color
		// if you want to compare STRICTLY, you should set this value as 0.
		double diffThreshold = 0.1;
		
		// base case for recursive building
		int base_bound = 50;
		if (frame.getWidth() < base_bound || frame.getHeight() < base_bound) {
			DiffPoints DP = ImageUtils2.compare(expectedImage, frame, actualImage, frame, diffThreshold);
			return convertDiffPointsToObjectGroups(DP, group_distance);	
		}
		
		// divide into 4 sub-frames
		Rectangle nw, ne, sw, se;
		int x = (int)frame.getX(), y = (int)frame.getY(), w = (int)frame.getWidth(), h = (int)frame.getHeight();
		int subW = Math.round(w/2), subH = Math.round(h/2);
		nw = new Rectangle(x,y,subW,subH);
		ne = new Rectangle(x+subW,y,w-subW,subH);
		sw = new Rectangle(x,y+subH,subW,h-subH);
		se = new Rectangle(x+subW,y+subH,w-subW,h-subH);
		
		// list of object groups built in each sub-frame
		List<ObjectGroup> NW, NE, SW, SE;		
		NW = buildObjectGroups(nw, group_distance);
		NE = buildObjectGroups(ne, group_distance);
		SW = buildObjectGroups(sw, group_distance);
		SE = buildObjectGroups(se, group_distance);
		
		// merge 4 sub-frames
		List<ObjectGroup> mergeGroups = new ArrayList<ObjectGroup>();
		mergeGroups.addAll(NW);
		mergeGroups.addAll(NE);
		mergeGroups.addAll(SW);
		mergeGroups.addAll(SE);
		
		// merge all possible object groups
		return mergeAllPossibleObjects(mergeGroups);	
	}
	
	/**
	 * merge all possible object groups
	 * @param objectGroups list of object groups
	 * @return	list of object groups which are completely merged 
	 */
	private List<ObjectGroup> mergeAllPossibleObjects (List<ObjectGroup> objectGroups) {

		// Count how many times merge occur for each case
		int num = -1;

		// loop until there is no merge
		while (num != 0) {
			num = 0;
			for (ObjectGroup object1 : objectGroups) {
				List<ObjectGroup> removeList = new ArrayList<ObjectGroup>();
				for (ObjectGroup object2 : objectGroups) {
					
					// Check if two distinct rectangles can be merged.
					if (!object1.equals(object2) && object1.canMerge(object2)) {
						object1.union(object2);
						num++;
						
						// Record the rectangle which will be removed.
						removeList.add(object2);
					}
				}
				if (num > 0) {
					// Remove the merged rectangle.
					for (ObjectGroup removeModel : removeList) {
						objectGroups.remove(removeModel);
					}
					break;
				}
			}
		}
		
		return objectGroups;
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
	private void SplitRectangles (List<Rectangle> rectangles, int splitIteration) {
		
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
				
				// use smaller value to union Rectangle Area than what we used for the first different area recognition
				List<Rectangle> splitRectangles = buildDiffAreas(subRectangle, 2);
								
				// if split succeed
				if (splitRectangles.size() != 1 || !subRectangle.equals(splitRectangles.get(0))) {
					
					// if there exists splitRectangle which is still over-merged, split it recursively
					SplitRectangles (splitRectangles, splitIteration-1);
					
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
	private void expand(Rectangle subRectangle, Rectangle splitRectangle, int sub_margin) {
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
	

	private List<Rectangle> convertObjectGroupsToAreas (List<ObjectGroup> objectGroups) {
		// Create a list of the Rectangle from diffGroups
		List<Rectangle> rectangles = new ArrayList<Rectangle>();

		for (ObjectGroup objectGroup : objectGroups) {
			rectangles.add(objectGroup.getRectangle());
		}
		
		return rectangles;		
	}
		
	/**
	 * convert different points to the list of object groups which are completely merged
	 * @param DP DiffPoints
	 * @param group_distance distance for grouping
	 * @return list of object groups which are completely merged
	 */
	private List<ObjectGroup> convertDiffPointsToObjectGroups(DiffPoints DP, int group_distance){
		List<Point> diffPoints = DP.getDiffPoints();
		if (diffPoints == null || diffPoints.isEmpty()) {
			return new ArrayList<ObjectGroup>();
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
		
		// merge all possible object groups
		return mergeAllPossibleObjects(diffGroups);
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
