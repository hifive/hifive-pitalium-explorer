package com.htmlhifive.pitalium.explorer.image;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.pitalium.image.model.DiffPoints;

public class ImagePair {

	// after constructor, expectedImage and actualImage are assigned.
	// they can be sub-image of their own image to make their sizes the same.
	BufferedImage expectedImage;
	BufferedImage actualImage;
	private int width, height;	// the size of intersection of two images
	Offset offset;				// Dominant offset between two images
	
	
	private List<ComparedRectangle> ComparedRectangles;
	private double entireSimilarity;

	// Default criteria to split over-merged rectangle
	private static final int BORDER_WIDTH = 10;
	private static final int OVERMERGED_WIDTH = 200;
	private static final int OVERMERGED_HEIGHT = 300;
	private static final int SPLIT_ITERATION = 10;

	// temporal variable to estimate total running time
	long totalTime;
	boolean printRunningTime = true;

	/**
	 * Constructor
	 */
	public ImagePair(BufferedImage expectedImage, BufferedImage actualImage) {

		double diffThreshold = ComparisonParameters.getDiffThreshold();
		long startTime, endTime;
		totalTime = 0;

		// Find dominant offset
		startTime = System.currentTimeMillis();
		offset = ImageUtils2.findDominantOffset(expectedImage, actualImage, diffThreshold);
		endTime = System.currentTimeMillis();
		if (printRunningTime) {
			System.out.printf("Dominant offset : x:%d y:%d\n", offset.getX(), offset.getY());
			System.out.printf("OFFSET:%d ",endTime-startTime);
			totalTime = totalTime + endTime-startTime;
		}
		
		// assign (sub) image with same size
		this.expectedImage = ImageUtils2.getDominantImage (expectedImage, actualImage, offset);
		this.actualImage = ImageUtils2.getDominantImage (actualImage, expectedImage, offset);
		width = Math.min(expectedImage.getWidth(), actualImage.getWidth());
		height = Math.min(expectedImage.getHeight(), actualImage.getHeight());
		compareImagePair();
	}

	/**
	 * 	Execute every comparison steps of two given images,
	 *  build ComparedRectangles list, and calculate entireSimilarity.
	 */
	private void compareImagePair () {

		// initial group distance
		int group_distance = ComparisonParameters.getDefaultGroupDistance();

		// Do not use sizeDiffPoints and consider only intersection area 
		Rectangle entireFrame = new Rectangle (width, height);
		
		// To check running time
		long startTime, endTime, totalTime = 0;

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
		SplitRectangles (rectangles, SPLIT_ITERATION, group_distance);
		ImageUtils2.removeOverlappingRectangles(rectangles);
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
		double diffThreshold = ComparisonParameters.getDiffThreshold();

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
	 * @param group_distance distance for grouping
	 */
	private void SplitRectangles (List<Rectangle> rectangles, int splitIteration, int group_distance) {

		// Terminate recursion after splitIteration-times
		if (splitIteration < 1) {
			return;
		}
		
		
		int margin = (int)(group_distance/2);   // To extract ACTUAL different region
		int sub_margin = margin + BORDER_WIDTH; // Remove border from actual different region
		List<Rectangle> removeList = new ArrayList<Rectangle>();
		List<Rectangle> addList = new ArrayList<Rectangle>();
		
		// for sub-rectangles, we apply split_group_distance instead of group_distance
		int split_group_distance = ComparisonParameters.getSplitGroupDistance();
		
		// split implementation for each rectangle
		for (Rectangle rectangle : rectangles) {

			// check if this rectangle can be split
			if (canSplit(rectangle)) {

				/** split is divided into two parts : inside sub-rectangle, boundary rectan gle **/
				
				/* build inside rectangles */
				
				// get sub rectangle by subtracting border information 
				int subX = (int)rectangle.getX() + sub_margin;
				int subY = (int)rectangle.getY() + sub_margin;
				int subWidth = (int)rectangle.getWidth() - 2*sub_margin;
				int subHeight = (int)rectangle.getHeight() - 2*sub_margin;
				Rectangle subRectangle = new Rectangle(subX, subY, subWidth, subHeight);

				// use smaller group_distance to union Rectangle Area than what we used for the first different area recognition
				List<Rectangle> splitRectangles = buildDiffAreas(subRectangle, split_group_distance);

				
				/* build boundary rectangles */
				
				// boundary area
				int boundary_margin = 1;        //      margin of boundary rectangle
				int padding = BORDER_WIDTH + 2*boundary_margin;
				int x = (int)rectangle.getX() + margin - boundary_margin;
				int y = (int)rectangle.getY() + margin - boundary_margin;
				int width = (int)rectangle.getWidth() - 2*margin + 2*boundary_margin;
				int height = (int)rectangle.getHeight() - 2*margin + 2*boundary_margin;

				Rectangle leftBoundary   = new Rectangle(x, subY, padding, subHeight);
				Rectangle rightBoundary  = new Rectangle(x+width-padding, subY, padding, subHeight);
				Rectangle topBoundary    = new Rectangle(subX, y, subWidth, padding);
				Rectangle bottomBoundary = new Rectangle(subX, y+height-padding, subWidth, padding);

				// build different area in boundary areas
				int minWidth = expectedImage.getWidth(), minHeight = expectedImage.getHeight();
				ImageUtils2.reshapeRect(leftBoundary, minWidth, minHeight);
				ImageUtils2.reshapeRect(rightBoundary, minWidth, minHeight);
				ImageUtils2.reshapeRect(topBoundary, minWidth, minHeight);
				ImageUtils2.reshapeRect(bottomBoundary, minWidth, minHeight);
				List<Rectangle> boundaryList= new ArrayList<Rectangle>();
				boundaryList.addAll(buildDiffAreas(leftBoundary, split_group_distance));
				boundaryList.addAll(buildDiffAreas(rightBoundary, split_group_distance));
				boundaryList.addAll(buildDiffAreas(topBoundary, split_group_distance));
				boundaryList.addAll(buildDiffAreas(bottomBoundary, split_group_distance));

				
				// if split succeed
				if (splitRectangles.size() != 1 || !subRectangle.equals(splitRectangles.get(0))) {

					// if there exists splitRectangle which is still over-merged, split it recursively
					SplitRectangles (splitRectangles, splitIteration-1, split_group_distance);

					// Record the rectangles which will be removed and added 
					for (Rectangle splitRectangle : splitRectangles) {

						// expand splitRectangle if it borders on subRectangle
						expand(subRectangle, splitRectangle, sub_margin);
						List<Rectangle> expansionRectangles = buildDiffAreas(splitRectangle, split_group_distance);
										
						// remove overlapping rectangles after expansion
						for (Rectangle boundaryRect : boundaryList) {
							for (Rectangle expansionRect : expansionRectangles) {
								Rectangle wrapper = new Rectangle ((int)expansionRect.getX()-boundary_margin-1, (int)expansionRect.getY()-boundary_margin-1, (int)expansionRect.getWidth()+2*boundary_margin+2, (int)expansionRect.getHeight()+2*boundary_margin+2);
								if (wrapper.contains(boundaryRect)) {
									removeList.add(boundaryRect);
									break;
								}
							}
						}
						addList.addAll(boundaryList);
						boundaryList.clear();
						addList.addAll(expansionRectangles);
					}
					removeList.add(rectangle);
				} else {
					System.out.printf("split fails\nsubRectangle - x:%d y:%d w:%d h:%d size:%d", subRectangle.getX(), subRectangle.getY(), subRectangle.getWidth(), subRectangle.getHeight(), splitRectangles.size());
				}
			}
		}

		// add recorded rectangles
		for (Rectangle rectangle : addList) {
			rectangles.add(rectangle);
		}

		// remove recorded rectangles
		for (Rectangle rectangle : removeList) {
			rectangles.remove(rectangle);
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
