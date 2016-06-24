package com.htmlhifive.pitalium.explorer.image;

import java.awt.image.BufferedImage;
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

	int sizeRelationType;
	Offset offset;				// Dominant offset between two images

	private List<ComparedRectangle> ComparedRectangles;
	private double entireSimilarity, minSimilarity;

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
	 * implement all comparison steps, so that we can use ComparedRectangles as results after constructor.
	 */
	public ImagePair(BufferedImage expectedImage, BufferedImage actualImage) {

		double diffThreshold = ComparisonParameters.getDiffThreshold();
		long startTime, endTime;
		totalTime = 0;

		// Find dominant offset
		startTime = System.currentTimeMillis();
		sizeRelationType = ImageUtils2.getSizeRelationType (expectedImage.getWidth(), expectedImage.getHeight(), actualImage.getWidth(), actualImage.getHeight());
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
		ComparedRectangles = new ArrayList<ComparedRectangle>();
		width = Math.min(expectedImage.getWidth(), actualImage.getWidth());
		height = Math.min(expectedImage.getHeight(), actualImage.getHeight());
		entireSimilarity = 0;
		minSimilarity = 1;	// assign dummy value to initialize
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
		long startTime, endTime, diffAreaTime, splitTime, removeTime, comparisonTime;

		// build different areas
		startTime = System.currentTimeMillis();
		List<Rectangle> rectangles  = buildDiffAreas(entireFrame, group_distance);
		endTime = System.currentTimeMillis();
		diffAreaTime = endTime - startTime;

		// split over-merged rectangles into smaller ones if possible
		startTime = System.currentTimeMillis();
		SplitRectangles (rectangles, SPLIT_ITERATION, group_distance);
		endTime = System.currentTimeMillis();
		splitTime = endTime - startTime;

		// remove redundant rectangles
		startTime = System.currentTimeMillis();
		ImageUtils2.removeOverlappingRectangles(rectangles);
		ImageUtils2.removeRedundantRectangles(rectangles,width,height);
		endTime = System.currentTimeMillis();
		removeTime = endTime - startTime;

		// compare two images using given rectangle areas
		startTime = System.currentTimeMillis();
		compareAllRectangles(rectangles);
		endTime = System.currentTimeMillis();
		comparisonTime = endTime - startTime;

		if (printRunningTime) {
			// total execution time of all comparison steps
			totalTime = totalTime + diffAreaTime + splitTime + removeTime + comparisonTime;
			System.out.printf("DIFF:%d SPLIT:%d REMOVE:%d CMP:%d TOTAL:%d\n", diffAreaTime, splitTime, removeTime, comparisonTime, totalTime);
			System.out.printf("minimum similarity :%.2f\n",minSimilarity);
		}		
	}

	private void compareAllRectangles(List<Rectangle> rectangles) {

		double similarityPixelByPixel, entireDifference = 0;

		for (Rectangle rectangle : rectangles)
		{
			// initialize result rectangle
			ComparedRectangle resultRectangle = new ComparedRectangle(rectangle);
			Offset offset = null;	// null means that when we calculate similarity, we try to find best match by moving actual sub-image

			/** if this rectangle is missing, set category 'MISSING' **/
			if (Categorizer.checkMissing(expectedImage, actualImage, rectangle)) {
				resultRectangle.setType("MISSING");
				offset = new Offset(0,0);	// we fix the position of actual sub-image.

				/** if this rectangle is shift, then process shift information in CheckShift method **/
			} else if (Categorizer.CheckShift(expectedImage, actualImage, ComparedRectangles, rectangle)) {
				// if shift, skip similarity calculation.
				continue;

				/** if this rectangle is image of subpixel rendered text, set category 'FONT' **/
			} else if (Categorizer.CheckSubpixel(expectedImage, actualImage, rectangle)){
				resultRectangle.setType("FONT");
			}

			/** calculate similarity **/

			// try object detection for better performance
			Rectangle expectedObject = new Rectangle(rectangle);
			Rectangle actualObject = new Rectangle(rectangle);

			// if object detection succeed for both images.
			if (ImageUtils2.getObjectRectangle(expectedImage, expectedObject) 
					&& ImageUtils2.getObjectRectangle(actualImage, actualObject)) {

				int x1 = (int)expectedObject.getX(), y1 = (int)expectedObject.getY(),
						w1 = (int)expectedObject.getWidth(), h1 = (int)expectedObject.getHeight(),
						x2 = (int)actualObject.getX(), y2 = (int)actualObject.getY(),
						w2 = (int)actualObject.getWidth(), h2 = (int)actualObject.getHeight();

				if (w1 == w2 && h1 == h2) {
					// case 1 : the same object size and the same location
					if (x1 == x2 && y1 == y2) {
						offset = new Offset(0,0);								

					// case 2 : the same object size but different location
					} else {
						offset = new Offset(x2-x1,y2-y1);
					}

				// case 3: different size
				} else {
					// check if two objects are the same but have different size
					if (Categorizer.checkScaling (expectedImage, actualImage, expectedObject, actualObject)){
						resultRectangle.setType("SCALING");	
						double	similarityFeatureMatrix = SimilarityUtils.calcSimilarityByFeatureMatrix(expectedImage, actualImage, expectedObject, actualObject);
						similarityPixelByPixel = SimilarityUtils.calcSimilarity(expectedImage, actualImage, rectangle, resultRectangle, offset, similarityFeatureMatrix);

						if (minSimilarity > similarityPixelByPixel) {
							minSimilarity = similarityPixelByPixel;
						}

						// calculate the similarity of entire image using pixel by pixel method
						int actualArea = (int)(rectangle.getWidth() * rectangle.getHeight());
						entireDifference += (1-similarityPixelByPixel)*actualArea;
				
						// insert the result rectangle into the list of ComparedRectangles
						ComparedRectangles.add(resultRectangle);
						continue;

					}
				}					
					}

			// implement all similarity calculations and categorization, and then build ComparedRectangle 
			similarityPixelByPixel = SimilarityUtils.calcSimilarity(expectedImage, actualImage, rectangle, resultRectangle, offset);
			if (minSimilarity > similarityPixelByPixel) {
				minSimilarity = similarityPixelByPixel;
			}

			// calculate the similarity of entire image using pixel by pixel method
			int actualArea = (int)(rectangle.getWidth() * rectangle.getHeight());
			entireDifference += (1-similarityPixelByPixel)*actualArea;
			
			// insert the result rectangle into the list of ComparedRectangles
			ComparedRectangles.add(resultRectangle);
		}

		// after calculating all similarities, calculate entire similarity of two images.
		entireSimilarity = 1-entireDifference/(width*height);
	}

	/**
	 * build different rectangles in the given frame area
	 * @param frame boundary area to build rectangles
	 * @param group_distance distance for grouping
	 * @return list of rectangles representing different area
	 */
	private List<Rectangle> buildDiffAreas (Rectangle frame, int group_distance) {
		List<ObjectGroup> objectGroups = buildObjectGroups(frame, group_distance, null);
		List<Rectangle> rectangles = ImageUtils2.convertObjectGroupsToAreas(objectGroups);

		return rectangles;
	}

	/**
	 * build different rectangles in the given frame area
	 * @param frame boundary area to build rectangles
	 * @param group_distance distance for grouping
	 * @Param offset offset from expected frame to actual frame
	 * @return list of rectangles representing different area
	 */
	private List<Rectangle> buildDiffAreas (Rectangle frame, int group_distance, Offset offset) {
		List<ObjectGroup> objectGroups = buildObjectGroups(frame, group_distance,  offset);
		List<Rectangle> rectangles = ImageUtils2.convertObjectGroupsToAreas(objectGroups);

		return rectangles;
	}


	/**
	 * build object groups for different areas in the given frame area
	 * @param frame	boundary area to build object
	 * @param group_distance distance for grouping
	 * @return list of object groups representing different area
	 */
	private List<ObjectGroup> buildObjectGroups (Rectangle frame, int group_distance, Offset offset) {

		// threshold for difference of color
		// if you want to compare STRICTLY, you should set this value as 0.
		double diffThreshold = ComparisonParameters.getDiffThreshold();

		// base case for recursive building
		int base_bound = 50;
		if (frame.getWidth() < base_bound || frame.getHeight() < base_bound) {
			Rectangle actualFrame = frame;
			if (offset != null) {
				actualFrame.setLocation((int)frame.getX()+offset.getX(), (int)frame.getY()+offset.getY());
			}

			DiffPoints DP = ImageUtils2.compare(expectedImage, frame, actualImage, actualFrame, diffThreshold);
			return ImageUtils2.convertDiffPointsToObjectGroups(DP, group_distance);	
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
		NW = buildObjectGroups(nw, group_distance, offset);
		NE = buildObjectGroups(ne, group_distance, offset);
		SW = buildObjectGroups(sw, group_distance, offset);
		SE = buildObjectGroups(se, group_distance, offset);

		// merge 4 sub-frames
		List<ObjectGroup> mergeGroups = new ArrayList<ObjectGroup>();
		mergeGroups.addAll(NW);
		mergeGroups.addAll(NE);
		mergeGroups.addAll(SW);
		mergeGroups.addAll(SE);

		// merge all possible object groups
		return ObjectGroup.mergeAllPossibleObjects(mergeGroups);	
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
	 * @return the minimum similarity among all different area
	 */
	public double getMinSimilarity () {
		return minSimilarity;
	}

	public Offset getDominantOffset () {
		return offset;
	}

	/**
	 * we need to decide which image of expected and actual is applied dominant offset.
	 * @return true if we need to apply dominant offset to expectedImage
	 */
	public boolean isExpectedMoved () {
		switch (sizeRelationType) {
			case 1:
				return false;
			case 2:
				return true;
			case 3:
				if (offset.getX() > 0)
					return true;
				else
					return false;
			case 4:
				if (offset.getX() > 0)
					return false;
				else
					return true;

				// never reach here
			default:
				return false;					
		}
	}
	/**
	 * print method for debugging
	 * @param rectangle
	 */
	void printRectangle (Rectangle rectangle) {
		System.out.printf("x:%d y:%d w:%d h:%d\n", rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
}
