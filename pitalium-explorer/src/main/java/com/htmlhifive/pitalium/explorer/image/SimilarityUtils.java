package com.htmlhifive.pitalium.explorer.image;
import com.htmlhifive.pitalium.image.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Color;
import java.lang.Math;

/**
 * Utility class to calculate similarity.
 */
public class SimilarityUtils {

	/**
	 * Extract the feature matrix of size FeatureRow by FeatureCol from image.
	 * @param FeatureRow the row size of feature matrix
	 * @param FeatureCol the column size of feature matrix
	 */
	private static int FeatureRow = 5;
	private static int FeatureCol = 5;
				
	/*
	 * Set the way to calculate norm.
	 * if true, use the average of norm,
	 * otherwise, use the norm of color distances.
	 */
	public static boolean averageNorm = false;

	/**
	 * Threshold to distungish the different pixel when counting the number of different pixels.
	 */
	private static double diffThreshold = 0.1;

	/**
	 * Constructor
	 */
	public SimilarityUtils(){} 

	/**
	 * Calculate the distance of two feature matrices.
	 * @param expectedFeature The feature matrix of expected sub image
	 * @param actualFeature The feature matrix of actual sub image
	 * @return the norm of distance between two matrices
	 */
	public static double calcFeatureDistance(Color[][] expectedFeature, Color[][] actualFeature) {
		// the difference of Red, Green, and Blue.
		int r, g, b;
		double dist=0;
		for (int row=0; row<FeatureRow; row++) {
			for (int col=0; col<FeatureCol; col++) {
				r = expectedFeature[row][col].getRed() - actualFeature[row][col].getRed();			
				g = expectedFeature[row][col].getGreen() - actualFeature[row][col].getGreen();			
				b = expectedFeature[row][col].getBlue() - actualFeature[row][col].getBlue();			
				dist += r*r + g*g + b*b;
			}
		}

		// normalize and return.
		return Math.sqrt(dist)/(Math.sqrt(3*FeatureRow*FeatureCol)*255);
	}

	/** 
	 * Check if the size of rectangle is large enough to use feature method.
	 * @param width the width of compared rectangle
	 * @param heigth the hieght of compared rectangle
	 * @return if the rectangle is large enough, return true.
	 */
	public static boolean checkFeatureSize (int width, int height) {
		return width >= FeatureCol && height >= FeatureRow ;
	}

	/**
	 * Calculate the similarity using feature matrix and find the best match where it has the highest similarity
	 * This method should be implemented only when the size of actualSubImage is greater than or equal to FeatureCol by FeatureRow.
	 * @param expectedSubImage the subimage of given rectangle area of expected image
	 * @param actualSubImage the subimage of given 'template' rectangle area of actual image. it is smaller than expectedSubImage. 
	 * @param rectangle The rectangle area where to compare.
	 * @return the 'feature' similarity of given area between two images.
	 */
	public static double calcSimilarityByFeatureMatrix(BufferedImage expectedSubImage, BufferedImage actualSubImage, Rectangle rectangle, ComparedRectangle similarRectangle) {
	
		// initialize subimage.
		int expectedWidth = expectedSubImage.getWidth(), expectedHeight = expectedSubImage.getHeight();
		int actualWidth = actualSubImage.getWidth(), actualHeight = actualSubImage.getHeight();

		// initialize the color array.
		int[] expectedColors = new int[expectedWidth * expectedHeight];
		int[] actualColors = new int[actualWidth * actualHeight];

		expectedSubImage.getRGB(0, 0, expectedWidth, expectedHeight, expectedColors, 0, expectedWidth);
		actualSubImage.getRGB(0, 0, actualWidth, actualHeight, actualColors, 0, actualWidth);

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
		
		
		/* Calculate the feature matrix of actual subimage. */

		// the size of grid.
		int GridWidth = actualWidth/FeatureCol, GridHeight = actualHeight/FeatureRow;
		int GridArea = GridWidth * GridHeight;

		Color[][] actualFeature = new Color[FeatureRow][FeatureCol];
		for (int row=0; row<FeatureRow; row++) {
			for (int col=0; col<FeatureCol; col++) {
				
				// Sum of Red, Green, and Blue.
				int rSum=0, gSum=0, bSum=0;

				// Calculate the feature value actualFeature[row][col].
				for (int i=0; i<GridHeight; i++) {
					for (int j=0; j<GridWidth; j++) {
						rSum += actualRed[actualWidth*(GridHeight*row + i) + (GridWidth*col + j)];
						gSum += actualGreen[actualWidth*(GridHeight*row + i) + (GridWidth*col + j)];
						bSum += actualBlue[actualWidth*(GridHeight*row + i) + (GridWidth*col + j)];
					}
				}
				
				actualFeature[row][col] = new Color((int)(rSum/GridArea), (int)(gSum/GridArea), (int)(bSum/GridArea));
			}
		}

		/* Calculate the feature matrix of expected subimage.*/

		Color[][] expectedFeature = new Color[FeatureRow][FeatureCol];
	
		int bestX=0, bestY=0;
		double dist=0, min=-1;
		
		int yMax = expectedHeight - actualHeight + 1;
		int xMax = expectedWidth - actualWidth + 1;
		
		for (int y = 0; y < yMax; y++) {
			for (int x = 0; x < xMax; x++) {
				
				// Calculate the distance between the expected feature matrix and the actual feature matrix shifhted (x, y).
				for (int row=0; row<FeatureRow; row++) {
					for (int col=0; col<FeatureCol; col++) {
						
						// Sum of Red, Green, and Blue.
						int rSum=0, gSum=0, bSum=0;

						// Calculate the feature value expectedFeature[row][col].
						for (int i=0; i<GridHeight; i++) {
							for (int j=0; j<GridWidth; j++) {
								rSum += expectedRed[expectedWidth*(GridHeight*row + (y+i)) + (GridWidth*col + (x+j))];
								gSum += expectedGreen[expectedWidth*(GridHeight*row + (y+i)) + (GridWidth*col + (x+j))];
								bSum += expectedBlue[expectedWidth*(GridHeight*row + (y+i)) + (GridWidth*col + (x+j))];
							}
						}
						expectedFeature[row][col] = new Color((int)(rSum/GridArea), (int)(gSum/GridArea), (int)(bSum/GridArea));
					}
				}

				// Calculate the feature distance at each shift (x, y).
				dist = calcFeatureDistance(expectedFeature, actualFeature);

				// Find the best match.
				if (dist < min || min==-1)
				{
					min = dist;
					bestX = x - (expectedWidth - actualWidth)/2;
					bestY = y - (expectedHeight - actualHeight)/2;
				}
			}
		}

		double similarity = 1-min;

		// insert similarity information
		similarRectangle.setSimilarity(3, bestX, bestY, similarity);

		return similarity;
	}

	/**
	 * Calculate the similarity by comparing two images pixel by pixel,
	 * and find the best match where it has the highest similarity.
	 * In this method, we count the number of different pixels as well.
	 * @param expectedSubImage the subimage of given rectangle area of expected image
	 * @param actualSubImage the subimage of given 'template' rectangle area of actual image. it is smaller than expectedSubImage. 
	 * @param rectangle The rectangle area where to compare.
	 * @return the 'pixel by pixel' similarity of given area between two images.
	 */
	public static double calcSimilarityPixelByPixel(BufferedImage expectedSubImage,BufferedImage actualSubImage, Rectangle rectangle, ComparedRectangle similarRectangle) {

		// initialize subimage.
		int expectedWidth = expectedSubImage.getWidth(), expectedHeight = expectedSubImage.getHeight();
		int actualWidth = actualSubImage.getWidth(), actualHeight = actualSubImage.getHeight();

		// initialize the color array.
		int[] expectedColors = new int[expectedWidth * expectedHeight];
		int[] actualColors = new int[actualWidth * actualHeight];

		expectedSubImage.getRGB(0, 0, expectedWidth, expectedHeight, expectedColors, 0, expectedWidth);
		actualSubImage.getRGB(0, 0, actualWidth, actualHeight, actualColors, 0, actualWidth);

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

		// to count the number of different pixels.
		int diffCnt, diffMin = -1, bestDiffX=0, bestDiffY=0;
		double diffNumSimilarity;

		double norm=0, min=-1;
		// Fine the best match moving the subimage.
		int yMax = expectedHeight - actualHeight + 1;
		int xMax = expectedWidth - actualWidth + 1;
		for (int y = 0; y < yMax; y++) {
			for (int x = 0; x < xMax; x++) {
				
				// Calculate the similarity with the (x, y)-shifted subimage of actual image.
				diffCnt = 0;	
				norm = 0;
				for (int i = 0; i < actualHeight; i++) {
					for (int j = 0; j < actualWidth; j++) {
						r = expectedRed[expectedWidth*(i+y)+(j+x)] - actualRed[actualWidth*i+j];
						g = expectedGreen[expectedWidth*(i+y)+(j+x)] - actualGreen[actualWidth*i+j];
						b = expectedBlue[expectedWidth*(i+y)+(j+x)] - actualBlue[actualWidth*i+j];
						if (averageNorm) {
							norm += Math.sqrt(r*r + g*g + b*b);
						}	else {
							norm += r*r + g*g + b*b;
						}
						if (r*r+g*g+b*b > 3*255*255*diffThreshold*diffThreshold)
							diffCnt++;
					}
				}

				// Find the minimal difference.
				if (norm < min || min == -1)
				{
					min = norm;
					bestX = x - (expectedWidth - actualWidth)/2;
					bestY = y - (expectedHeight - actualHeight)/2;
				}
				
				// Find the minimal number of different pixels.
				if (diffCnt < diffMin || diffMin == -1)
				{
					diffMin = diffCnt;
					bestDiffX = x - (expectedWidth - actualWidth)/2;
					bestDiffY = y - (expectedHeight - actualHeight)/2;
				}
			}
		}
		double similarity;

		// normalize and calculate average.
		if (averageNorm) {
			similarity = 1-min/(Math.sqrt(3)*255*actualWidth*actualHeight);
		}	else {
			similarity = 1-Math.sqrt(min/(actualWidth*actualHeight))/(Math.sqrt(3)*255);
		}

		// normalize the number of different pixels.
		diffNumSimilarity = 1 - (double)diffMin/(actualWidth*actualHeight);

		// insert similarity informations
		similarRectangle.setSimilarity(1, bestX, bestY, similarity);
		similarRectangle.setSimilarity(2, bestDiffX, bestDiffY, diffNumSimilarity);

		return similarity;
	}
}
	
	
