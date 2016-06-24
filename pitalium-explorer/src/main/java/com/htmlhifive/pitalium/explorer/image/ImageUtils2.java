package com.htmlhifive.pitalium.explorer.image;


import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.htmlhifive.pitalium.common.exception.TestRuntimeException;
import com.htmlhifive.pitalium.image.model.DiffPoints;


/**
 * image utils for image comparison.
 * In this class, I changed some methods in com.htmlhifive.pitalium.image.util
 */
public final class ImageUtils2 {

	/**
	 * Constructor
	 */
	private ImageUtils2() {
	}
	
	/**
	 * integer pixel value to aRGB array
	 * @param pixel
	 * @return aRGB value array
	 */
	public static int[] toARGB(int pixel){
		int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;
		return new int[]{alpha, red, green, blue};
	}


	/**
	 * RGB array to integer pixel value
	 * @param argb
	 * @return integer pixel value
	 */
	public static int toPixel(int[] argb){
		int ret = 0;
		ret |= (argb[0] << 24);
		ret |= (argb[1] << 16);
		ret |= (argb[2] << 8);
		ret |= (argb[3]);
		return ret;
	}
	
	/**
	 * RGB to integer pixel value (alpha is set as 255)
	 * @param r
	 * @param g
	 * @param b
	 * @return integer pixel value
	 */
	public static int toPixel(int r, int g, int b){
		return toPixel(new int[]{255, r, g, b});
	}
	
	/**
	 * aRGB to integer pixel value 
	 * @param a
	 * @param r
	 * @param g
	 * @param b
	 * @return integer pixel value
	 */
	public static int toPixel(int a, int r, int g, int b){
		return toPixel(new int[]{a, r, g, b});
	}

	/**
	 * get color - count map of image
	 * @param source
	 * @return map whose key is color and value is count
	 */
	private static Map<Integer, Integer> colorCountMap(BufferedImage source){
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		for (int y=0; y<source.getHeight(); y++){
			for (int x=0; x<source.getWidth(); x++){
				if(m.containsKey(source.getRGB(x, y))){
					m.put(source.getRGB(x, y), m.get(source.getRGB(x, y))+1);
				}
				else{
					m.put(source.getRGB(x, y), 1);
				}
			}
		}
		return m;
	}
	
	/**
	 * get estimated background color
	 * @param b
	 * @return most frequent color.
	 */
	public static int detectBackgroundColor(BufferedImage b){
		Map<Integer, Integer> map = colorCountMap(b);
		int max = 0;
		int max_color = toPixel(255, 255, 255);
		for(Integer color : map.keySet()){
			if(map.get(color) > max){
				max = map.get(color);
				max_color = color;
			}
		}
		return max_color;
	}


	/**
	 * find object rectangle in given rectangle area
	 * if find, replace given rectangle object with object rectangle
	 * @param image	
	 * @param rectangle
	 * @return true if finding object rectangle succeeds
	 */
	public static boolean getObjectRectangle (BufferedImage image, Rectangle rectangle) {
		
		int maxMargin = ComparisonParameters.getDefaultGroupDistance();
		int x = (int)rectangle.getX(), y = (int)rectangle.getY(),
			w = (int)rectangle.getWidth(), h = (int)rectangle.getHeight();
		BufferedImage subImage = image.getSubimage(x, y, w, h);
		
		// if rectangle is not big enough, do not find object rectangle
		// use 3*maxMargin here, in order to assume that object is bigger than margin by margin
		int width = w-2, height = h-2;	//	width and height of edge map
		if (width<=3*maxMargin || height<=3*maxMargin) {
			return false;
		}
		
		// initialize edge map using 8-directional independent edge detection
		boolean[][] edgeMap = new boolean[height][width];
		for (int i=0; i<maxMargin; i++) {
			// on the corner areas using diagonal filter.
			for (int j=0; j<maxMargin; j++) {
				// set true if different
				edgeMap[i][j] = subImage.getRGB(j+1,i+1) != subImage.getRGB(j,i);												// NW corner
				edgeMap[i][width-1-j] = subImage.getRGB(width-j,i+1) != subImage.getRGB(width-j+1,i);							// NE corner
				edgeMap[height-1-i][j] = subImage.getRGB(j+1,height-i) != subImage.getRGB(j,height-i+1);						// SW corner
				edgeMap[height-1-i][width-1-j] = subImage.getRGB(width-j,height-i) != subImage.getRGB(width-j+1,height-i+1);	// SE corner
			}
			
			// on the top and bottom bridge areas using vertical filter.
			for (int j=maxMargin; j<width-maxMargin; j++) {
				edgeMap[i][j] = subImage.getRGB(j+1,i+1) != subImage.getRGB(j+1,i);							// top bridge area
				edgeMap[height-1-i][j] = subImage.getRGB(j+1,height-i) != subImage.getRGB(j+1,height-i+1);	// bottom bridge area
			}
			
			// on the left and right bridge areas using horizontal filter.
			for (int j=maxMargin; j<height-maxMargin; j++) {
				edgeMap[j][i] = subImage.getRGB(i+1,j+1) != subImage.getRGB(i,j+1);	// left bridge area
				edgeMap[j][width-1-i] = subImage.getRGB(width-i,j+1) != subImage.getRGB(width-i+1,j+1);	// right bridge area
			}
		}
		
		// shrink edgeMap down to (2*maxMargin+1) by (2*maxMargin+1) by using these bridges.
		int shrinkLength = 2*maxMargin+1;
		boolean[][] shrinkMap = new boolean[shrinkLength][shrinkLength],
					verticalMap = new boolean[shrinkLength][shrinkLength],	// to check vertical direction
					horizontalMap = new boolean[shrinkLength][shrinkLength];	// to check horizontal direction

		// initialize shrink map
		for (int i=0; i<maxMargin; i++) {
			for (int j=0; j<maxMargin; j++) {
				shrinkMap[i][j] = edgeMap[i][j];
				shrinkMap[i][shrinkLength-1-j] = edgeMap[i][width-1-j];
				shrinkMap[shrinkLength-1-i][j] = edgeMap[height-1-i][j];
				shrinkMap[shrinkLength-1-i][shrinkLength-1-j] = edgeMap[height-1-i][width-1-j];
			}
		}
		shrinkMap[maxMargin][maxMargin]=false; // center value never used.
		
		// check if bridge exists
		for (int i=0; i<maxMargin; i++) {
			int leftIdx = maxMargin, rightIdx = maxMargin, topIdx = maxMargin, bottomIdx = maxMargin;
			
			// check left & right bridges
			while(leftIdx<height-maxMargin && edgeMap[leftIdx][i]) { leftIdx++; }
			shrinkMap[maxMargin][i] = (leftIdx == height-maxMargin);
			while(rightIdx<height-maxMargin && edgeMap[rightIdx][width-1-i]) { rightIdx++; }
			shrinkMap[maxMargin][shrinkLength-1-i] = (rightIdx == height-maxMargin);
					
			// check top & bottom bridges
			while(topIdx<width-maxMargin && edgeMap[i][topIdx]) { topIdx++; }
			shrinkMap[i][maxMargin] = (topIdx == width-maxMargin);
			while(bottomIdx<width-maxMargin && edgeMap[height-1-i][bottomIdx]) { bottomIdx++; }
			shrinkMap[shrinkLength-1-i][maxMargin] = (bottomIdx == width-maxMargin);	
		}
				
		// initialize expansion map
		for (int i=0; i<shrinkLength; i++) {
			for (int j=0; j<shrinkLength; j++) {
				verticalMap[i][j] = shrinkMap[i][j];
				horizontalMap[i][j] = shrinkMap[i][j];
			}
		}
		
		// expand continuous points from bridge
		for (int i=0; i<maxMargin; i++) {
			for (int j=1; j<=maxMargin; j++){
				// expand left
				verticalMap[maxMargin-j][i] &= verticalMap[maxMargin-j+1][i];
				verticalMap[maxMargin+j][i] &= verticalMap[maxMargin+j-1][i];
				// expand right
				verticalMap[maxMargin-j][shrinkLength-1-i] &= verticalMap[maxMargin-j+1][shrinkLength-1-i];
				verticalMap[maxMargin+j][shrinkLength-1-i] &= verticalMap[maxMargin+j-1][shrinkLength-1-i];
				// expand top
				horizontalMap[i][maxMargin-j] &= horizontalMap[i][maxMargin-j+1];
				horizontalMap[i][maxMargin+j] &= horizontalMap[i][maxMargin+j-1];
				// expand bottom
				horizontalMap[shrinkLength-1-i][maxMargin-j] &= horizontalMap[shrinkLength-1-i][maxMargin-j+1];
				horizontalMap[shrinkLength-1-i][maxMargin+j] &= horizontalMap[shrinkLength-1-i][maxMargin+j-1];
			}
		}
		
		// find points continuous to bridge along both vertical and horizontal directions
		for (int i=0; i<shrinkLength; i++) {
			for (int j=0; j<shrinkLength; j++) {
				shrinkMap[i][j] = verticalMap[i][j] & horizontalMap[i][j];				
			}
		}
		
		// find any rectangle 
		int top, bottom, left, right; // how may pixels apart from top, bottom, left, right
		for (top=0; top<maxMargin; top++) {
			if (!shrinkMap[top][maxMargin])
				continue;
			for (left=0; left<maxMargin; left++) {
				if (!shrinkMap[top][left])
					continue;
				for (bottom=0; bottom<maxMargin; bottom++) {
					if(!shrinkMap[shrinkLength-1-bottom][left])
						continue;
					for (right=0; right<maxMargin; right++) {
						if (shrinkMap[top][shrinkLength-1-right] && shrinkMap[shrinkLength-1-bottom][shrinkLength-1-right]) {
							width -= left + right;
							height -= top + bottom;
							x += left;
							y += top;
							
							// replace the given rectangle with object rectangle 
							rectangle.setBounds(x, y, width, height);
							return true;
						}

					}
				}
			}
		}
				
		return false;
	}
	
		
	/**
	 * L2 norm between two RGB pixel values
	 * @param pixel1
	 * @param pixel2
	 * @return d(R)^2 + d(G)^2 + d(B)^2
	 */
	public static double norm(int pixel1, int pixel2){
		int red1 = (pixel1 >> 16) & 0xff;
		int green1 = (pixel1 >> 8) & 0xff;
		int blue1 = (pixel1) & 0xff;
		int red2 = (pixel2 >> 16) & 0xff;
		int green2 = (pixel2 >> 8) & 0xff;
		int blue2 = (pixel2) & 0xff;
		int redDist = (red1-red2)*(red1-red2);
		int greenDist = (green1-green2)*(green1-green2);
		int blueDist = (blue1-blue2)*(blue1-blue2);
		return redDist + greenDist + blueDist;
	}

	/**
	 * calculate the features of sub-pixel rendered text image
	 * @param bimage
	 * @return smoothly rendered edges rate over all colored chunk and number of chunks per line.
	 */
	public static double[] countSubpixel(BufferedImage bimage){
		int width = bimage.getWidth();
		int height = bimage.getHeight();

		int bgColor = detectBackgroundColor(bimage);

		int count = 0;
		int total = 0;

		boolean wasBackground = true;
		boolean inside = false;

		int[] previous = null;
		int[] countPerLine = new int[height];

		for (int y=0; y<height; y++){
			int numberOfUpdown = 0;
			for(int x=0; x<width; x++){
				int pixel = bimage.getRGB(x, y);
				int[] argb = toARGB(pixel);
				if (norm(pixel, bgColor) < 25){
					if(!wasBackground){
						if(previous[1]>previous[2] || previous[2]>previous[3] || previous[1]==previous[3]){ // in-correct
						}else{ // correct
							if (inside){
								count++;
							}
						}
						inside = false;
						total++;
					}
					numberOfUpdown++;
					wasBackground = true;
				}else{
					if(wasBackground){
						if(argb[1]<argb[2] || argb[2]<argb[3] || argb[1]==argb[3]){ // in-correct
						}else{ // correct
							inside = true;
						}
					}
					wasBackground = false;
				}
				previous = argb;
			}
			countPerLine[y] = numberOfUpdown;
		}
		double rate = (double) count/total;

		double avg = 0;
		int countValid = 0;
		for(int i=0; i<countPerLine.length; i++){
			avg += (double) countPerLine[i];
			if(countPerLine[i] != 0){
				countValid++;
			}
		}
		avg /= (double) countValid;
		return new double[]{rate, avg};
	}

	
	/**
	 * if the given rectangle may occur raster error, reshape it
	 * @param rectangle	Rectangle which will be reshaped
	 * @param xLimit	limit of x+width of given rectangle
	 * @param yLimit	limit of y+height of given rectangle
	 */
	public static void reshapeRect(Rectangle rectangle, int xLimit, int yLimit)
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

		if (x + width >= xLimit)
			width = xLimit - x;
		if (y + height >= yLimit)
			height = yLimit - y;
		
		rectangle.setRect(x, y, Math.max(width,1), Math.max(height,1));
	}

	
	/**
	 * remove overlapping rectangles for better UI
	 * @param rectangles the list of rectangles which will be checker overlapping
	 */
	public static void removeOverlappingRectangles (List<Rectangle> rectangles) {
		// ignore small difference
		int smallDiff = 2;
		List <Rectangle> removeList = new ArrayList<Rectangle>();
		
		// check containing relation and record what to remove
		for (int i=0 ; i<rectangles.size(); i++) {
			Rectangle rect1 = rectangles.get(i);
			int xLeft1 = (int)rect1.getX(), xRight1 = (int)(rect1.getX()+rect1.getWidth());
			int yTop1 = (int)rect1.getY(), yBottom1 = (int)(rect1.getY()+rect1.getHeight());
			
			for (int j=i+1; j<rectangles.size(); j++) {
				Rectangle rect2 = rectangles.get(j);
				int xLeft2 = (int)rect2.getX(), xRight2 = (int)(rect2.getX()+rect2.getWidth());
				int yTop2 = (int)rect2.getY(), yBottom2 = (int)(rect2.getY()+rect2.getHeight());
				
				// check rect1 contains rect2
				if (xLeft1-smallDiff<=xLeft2 && yTop1-smallDiff <= yTop2
					&& xRight1+smallDiff >= xRight2 && yBottom1+smallDiff >= yBottom2) {
					removeList.add(rect2);
				} 
				
				// check rect2 contains rect1
				else if (xLeft2-smallDiff<=xLeft1 && yTop2-smallDiff <= yTop1
						&& xRight2+smallDiff >= xRight1 && yBottom2+smallDiff >= yBottom1) {
					removeList.add(rect1);
				}
			}
		}
		
		// remove recorded rectangles
		for (Rectangle removeRect : removeList) {
			rectangles.remove(removeRect);
		}		
	}


	public static List<Rectangle> convertObjectGroupsToAreas (List<ObjectGroup> objectGroups) {
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
	public static List<ObjectGroup> convertDiffPointsToObjectGroups(DiffPoints DP, int group_distance){
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
		return ObjectGroup.mergeAllPossibleObjects(diffGroups);
	}
	
	
	/**
	 * get sub-image from given image and rectangle
	 * @param image
	 * @param rectangle 
	 * @return sub-image of given area
	 */
	public static BufferedImage getSubImage(BufferedImage image, Rectangle rectangle) {

		// before getting subImage, reshape rectangle to avoid raster error 
		reshapeRect(rectangle,image.getWidth(), image.getHeight());
		
		// Initialize variables
		int width= (int)rectangle.getWidth(), height = (int)rectangle.getHeight();
		int x = (int)rectangle.getX(), y = (int)rectangle.getY();

		return image.getSubimage(x, y, width, height);
	}

	/**
	 * find dominant offset between two images
	 * @param expectedImage
	 * @param actualImage
	 * @param diffThreshold	threshold to ignore small difference
	 * @return	Offset contains offsetX and offsetY
	 */
	public static Offset findDominantOffset(BufferedImage expectedImage, BufferedImage actualImage, double diffThreshold) {
		
		// we don't need to check all elements, only check one element in every STEP*STEP elements 
		int STEP = 5;
		
		// we need to restrict the maximum offset to avoid redundant checking
		int maxOffset = 10;
		
		// initialize size
		int expectedWidth = expectedImage.getWidth(), expectedHeight = expectedImage.getHeight();
		int actualWidth = actualImage.getWidth(), actualHeight = actualImage.getHeight();
		int subWidth = Math.min(expectedWidth, actualWidth), subHeight = Math.min(expectedHeight, actualHeight);
		int xMax = Math.abs(expectedWidth-actualWidth), yMax = Math.abs(expectedHeight-actualHeight);
		xMax = Math.min(xMax,maxOffset);
		yMax = Math.min(yMax,maxOffset);
		
		// check the type of relationship between two image sizes
		// if one of two is contained in the other, it has type 1 or 2.
		// else, it has type 3 or 4.
		int sizeRelationType = getSizeRelationType (expectedWidth, expectedHeight, actualWidth, actualHeight);
		int expectedXOffset=0, expectedYOffset = 0, actualXOffset=0, actualYOffset=0;
		
		// calculation method to find dominant offset depends on the type of this relation.
		switch (sizeRelationType) {
			case 1:
				// for type 1, actualImage is bigger than expectedImage
				// so we need to move a sub-rectangle only in actualImage
				actualXOffset = 1;
				actualYOffset = 1;
				break;
				
			case 2:
				// for type 2, expectedImage is bigger than actualImage
				// so we need to move a sub-rectangle only in expectedImage
				expectedXOffset = 1;
				expectedYOffset = 1;
				break;
				
			case 3:
				// for type 3, the width of expectedImage is larger,
				// and the height of actualImage is larger.
				// so we need to move a sub-rectangle rightward in expectedImage,
				// and downward in actualImage
				expectedXOffset = 1;
				actualYOffset = 1;
				break;
								
			case 4:
				// for type 4, the width of actualImage is larger,
				// and the height of expectedImage is larger.
				// so we need to move a sub-rectangle rightward in actualImage,
				// and downward in expectedImage
				expectedYOffset = 1;
				actualXOffset = 1;
				break;
			}

		// initialize the color array.
		int[] expectedColors = new int[expectedWidth * expectedHeight];
		int[] actualColors = new int[actualWidth * actualHeight];
		expectedImage.getRGB(0, 0, expectedWidth, expectedHeight, expectedColors, 0, expectedWidth);
		actualImage.getRGB(0, 0, actualWidth, actualHeight, actualColors, 0, actualWidth);
		int[] expectedRed = new int[expectedColors.length];
		int[] expectedGreen = new int[expectedColors.length];
		int[] expectedBlue = new int[expectedColors.length];
		int[] actualRed = new int[actualColors.length];
		int[] actualGreen = new int[actualColors.length];
		int[] actualBlue = new int[actualColors.length];

		for (int i = 0; i < expectedColors.length; i++) {
			Color expectedColor = new Color(expectedColors[i]);
			expectedRed[i] = expectedColor.getRed();
			expectedGreen[i] = expectedColor.getGreen();
			expectedBlue[i] = expectedColor.getBlue();
		}

		for (int i = 0; i < actualColors.length; i++) {
			Color actualColor = new Color(actualColors[i]);
			actualRed[i] = actualColor.getRed();
			actualGreen[i] = actualColor.getGreen();
			actualBlue[i] = actualColor.getBlue();
		}

		// the difference of Red, Green, and Blue, respectively.
		int r, g, b, bestX=0, bestY=0;

		// count the number of different points using threshold
		int thresDiffCount, thresDiffMin = -1;

		// Find the dominant offset moving the subimage in the bigger image.
		if (sizeRelationType == 1 || sizeRelationType == 2) {
		// containing case
			
			for (int y = 0; y <= yMax; y++) {
				for (int x = 0; x <= xMax; x++) {
					
					// find dominant offset
					thresDiffCount = 0;	
					for (int i = 0; i < subHeight; i=i+STEP) {
						for (int j = 0; j < subWidth; j=j+STEP) {
							r = expectedRed[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
								-actualRed[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
							g = expectedGreen[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
								-actualGreen[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
							b = expectedBlue[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
								-actualBlue[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
							if (r*r+g*g+b*b > 3*255*255*diffThreshold*diffThreshold)
								thresDiffCount++;
						}
					}
					// Find the minimal number of threshold different pixels.
					if (thresDiffCount < thresDiffMin || thresDiffMin == -1) {
						thresDiffMin = thresDiffCount;
						bestX = x;
						bestY = y;
					}
				}
			}
		} else {
		// not containing case
			
			// move sub-rectangle downward
			int x = 0;
			for (int y = 0; y <= yMax; y++) {
				
				// find dominant offset
				thresDiffCount = 0;	
				for (int i = 0; i < subHeight; i=i+STEP) {
					for (int j = 0; j < subWidth; j=j+STEP) {
						r = expectedRed[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
							-actualRed[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
						g = expectedGreen[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
							-actualGreen[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
						b = expectedBlue[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
							-actualBlue[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
						if (r*r+g*g+b*b > 3*255*255*diffThreshold*diffThreshold)
							thresDiffCount++;
					}
				}
					// Find the minimal number of threshold different pixels.
					if (thresDiffCount < thresDiffMin || thresDiffMin == -1) {
						thresDiffMin = thresDiffCount;
						bestX = x;
						bestY = y;
					}
			}
			
			// move sub-rectangle rightward
			int y = 0;
			for (x = 1; x <= xMax; x++) {

				// find dominant offset
				thresDiffCount = 0;	
				for (int i = 0; i < subHeight; i=i+STEP) {
					for (int j = 0; j < subWidth; j=j+STEP) {
						r = expectedRed[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
							-actualRed[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
						g = expectedGreen[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
							-actualGreen[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
						b = expectedBlue[expectedWidth*(i+y*expectedYOffset)+(j+x*expectedXOffset)]
							-actualBlue[actualWidth*(i+y*actualYOffset)+(j+x*actualXOffset)];
						if (r*r+g*g+b*b > 3*255*255*diffThreshold*diffThreshold)
							thresDiffCount++;
					}
				}

				// Find the minimal number of threshold different pixels.
				if (thresDiffCount < thresDiffMin || thresDiffMin == -1) {
					thresDiffMin = thresDiffCount;
					bestX = x;
					bestY = y;
				}
			}
		}

		return new Offset(bestX, bestY);
	}
	
	/**
	 * using dominant offset, extract subImage from given expectedImage
	 * the size of subImage will be the same as the size of intersection of two images.
	 * @param expectedImage	the image which we want to extract sub-image from
	 * @param actualImage	the other image
	 * @param offset dominant offset between two images
	 * @return	subImage of expectedImage at dominant offset
	 */
	public static BufferedImage getDominantImage (BufferedImage expectedImage, BufferedImage actualImage, Offset offset) {
		
		// initialize size
		int expectedWidth = expectedImage.getWidth(), expectedHeight = expectedImage.getHeight();
		int actualWidth = actualImage.getWidth(), actualHeight = actualImage.getHeight();
		
		// dominant frame of expectedImage depends on size-relation type and offset
		int sizeRelationType = getSizeRelationType (expectedWidth, expectedHeight, actualWidth, actualHeight);
		switch (sizeRelationType) {
			case 1:
				return expectedImage;
			case 2:
				return expectedImage.getSubimage(offset.getX(), offset.getY(), actualWidth, actualHeight);
			case 3:
				if (offset.getX() > 0) {
					return expectedImage.getSubimage(offset.getX(), 0, actualWidth, expectedHeight); 
				} else {
					return expectedImage.getSubimage(0, 0, actualWidth, expectedHeight);
				}
			case 4:
				if (offset.getY() > 0) {
					return expectedImage.getSubimage(0, offset.getY(), expectedWidth, actualHeight);
				} else {
					return expectedImage.getSubimage(0,0,expectedWidth, actualHeight);
				}

			// never reach to default case
			default :
				return expectedImage;
		}
	}
	
	
	
	/**
	 * In order to find dominant offset, we have to consider the relationship between two image sizes.
	 * @param expectedWidth		width of expectedImage
	 * @param expectedHeight	height of expectedImage
	 * @param actualWidth		width of actualImage
	 * @param actualHeight		height of actualImage
	 * @return 	the number of type of size-relationship
	 * 			1 when actualImage is bigger than expectedImage
	 * 			2 when expectedImage is bigger than actualImage
	 * 			3 when the width of expectedImage is larger and the height of actualImage is larger.
	 *			4 when the width of actualImage is larger and the height of expectedImage is larger.
	 */
	public static int getSizeRelationType (int expectedWidth, int expectedHeight, int actualWidth, int actualHeight){
		if (expectedWidth <= actualWidth && expectedHeight <= actualHeight) {
			return 1;
		} else if (expectedWidth >= actualWidth && expectedHeight >= actualHeight) {
			return 2;
		} else if (expectedWidth >= actualWidth && expectedHeight < actualHeight) {
			return 3;
		} else {
			return 4;
		}
	}
	
	
	/***********************************************************************************/
	/** The below methods are from pitalium, but I needed to modify some part of them **/
	/** Before sending pull request, I use them here for the fast application 		  **/
	/**												- 2016.06.04. Yeongjin - 		  **/
	
	
	/***** com.htmlhifive.pitalium.image.util.ImageUtils *****/
	
	/**
	 * 元画像の積分画像を生成します。
	 * 
	 * @param source 元画像
	 * @return 積分結果の配列
	 */
	public static double[][] calcIntegralImage(BufferedImage source) {
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
	
	/***** com.htmlhifive.pitalium.image.util.ImageComparator *****/

	/**
	 * 2枚の画像を比較し、差分の一覧を取得します。
	 * 
	 * @param img1 画像1
	 * @param img1Area 画像1で比較の対象とする範囲
	 * @param img2 画像2
	 * @param img2Area 画像2で比較の対象とする範囲
	 * @param diffThreshold to extract 'real' difference, we need to use threshold.
	 * @return 比較結果の差分データ
	 */
	public static DiffPoints compare(BufferedImage img1, Rectangle img1Area, BufferedImage img2, Rectangle img2Area, double diffThreshold) {
		if (img1 == null || img2 == null) {
			throw new TestRuntimeException("Both img1 and img2 is required.");
		}
		//LOG.trace("[Compare] image1[w: {}, h: {}; {}]; image2[w: {}, h: {}: {}]", img1.getWidth(), img1.getHeight(),
		//		img1Area, img2.getWidth(), img2.getHeight(), img2Area);

		int offsetX = 0;
		int offsetY = 0;
		BufferedImage image1 = null;
		BufferedImage image2 = null;
		if (img1Area != null) {
			image1 = getSubImage(img1, img1Area);
			offsetX = (int) img1Area.getX();
			offsetY = (int) img1Area.getY();
		} else {
			image1 = img1;
		}
		if (img2Area != null) {
			image2 = getSubImage(img2, img2Area);
		} else {
			image2 = img2;
		}

		// In this comparison, we don't use sizeDiffPoints
		//List<Point> sizeDiffPoints = createSizeDiffPoints(image1, image2, offsetX, offsetY);
		List<Point> sizeDiffPoints = new ArrayList<Point>();
		List<Point> diffPoints = compare(image1, image2, offsetX, offsetY, diffThreshold);
		return new DiffPoints(diffPoints, sizeDiffPoints);
	}


	/***** com.htmlhifive.pitalium.image.util.DefaultImageComparator *****/


	protected static List<Point> compare(BufferedImage image1, BufferedImage image2, int offsetX, int offsetY, double diffThreshold) {
		//LOG.trace("[Compare] image1[w: {}, h: {}], image2[w: {}, h: {}], offset: ({}, {})", image1.getWidth(),
		//		image1.getHeight(), image2.getWidth(), image2.getHeight(), offsetX, offsetY);
		int width = Math.min(image1.getWidth(), image2.getWidth());
		int height = Math.min(image1.getHeight(), image2.getHeight());

		int[] rgb1 = getRGB(image1, width, height);
		int[] rgb2 = getRGB(image2, width, height);
		
		// different of red, green, and blue
		int r, g, b;
		
		List<Point> diffPoints = new ArrayList<Point>();
		for (int i = 0, length = rgb1.length; i < length; i++) {
			
			Color color1 = new Color(rgb1[i]), color2 = new Color(rgb2[i]);
			r = color1.getRed() - color2.getRed();
			g = color1.getGreen() - color2.getGreen();
			b = color1.getBlue() - color2.getBlue();
			if (r*r+g*g+b*b > 3*255*255*diffThreshold*diffThreshold) {
				int x = (i % width) + offsetX;
				int y = (i / width) + offsetY;

				Point diffPoint = new Point(x, y);
				diffPoints.add(diffPoint);
				//LOG.trace("[Compare] Diff found ({}, {}). #{} <=> #{}", diffPoint.x, diffPoint.y,
				//		Integer.toHexString(rgb1[i]), Integer.toHexString(rgb2[i]));
			}
		}

		if (!diffPoints.isEmpty()) {
			//LOG.debug("[Compare] {} diff found.", diffPoints.size());
		}
		return diffPoints;
	}
	/**
	 * 指定した画像のRGBベースのピクセル配列を取得します。
	 * 
	 * @param image 対象の画像
	 * @param width 読み込む幅
	 * @param height 読み込む高さ
	 * @return ピクセル配列
	 */
	private static int[] getRGB(BufferedImage image, int width, int height) {
		return image.getRGB(0, 0, width, height, null, 0, width);
	}
}

