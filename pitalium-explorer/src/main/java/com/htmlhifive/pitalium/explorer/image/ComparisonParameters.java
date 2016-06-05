package com.htmlhifive.pitalium.explorer.image;

/**
 * Default parameter setting for comparison
 */
public class ComparisonParameters {

	/* default setting for comparison parameters */
	
	// recognizing threshold for sub-pixel rendered font
	private static double subpixelThreshold = 0.9;
	
	// difference threshold to ignore small differences
	private static double diffThreshold = 0.1;
	
	// parameters for categorization
	private static double scalingDiffCriterion = 0.3;
	private static double scalingFeatureCriterion = 0.85;
		
	// group distance for building different area
	private static int defaultGroupDistance = 10;
	private static int splitGroupDistance = 4;
	
	// maximum range for shift checking, similarity calculation
	private static int maxShift = 10;
	private static int maxMove = 1;		// moving range for similarity calculation
	/**
	 * Constructor
	 */
	public ComparisonParameters() {};

	public static double getSubpixelThreshold() {
		return subpixelThreshold;
	}
	public static void setSubpixelThreshold(double subpixelThreshold) {
		ComparisonParameters.subpixelThreshold = subpixelThreshold;
	}
	public static void setDiffThreshold(double threshold) {
		diffThreshold = threshold;
	}
	public static double getDiffThreshold() {
		return diffThreshold;
	}
	public static void setScalingDiffCriterion (double criterion) {
		scalingDiffCriterion = criterion;
	}
	public static double getScalingDiffCriterion () {
		return scalingDiffCriterion;
	}
	public static void setScalingFeatureCriterion (double criterion) {
		scalingFeatureCriterion = criterion;
	}
	public static double getScalingFeatureCriterion () {
		return scalingFeatureCriterion;
	}
	public static void setDefaultGroupDistance(int group_distance) {
		defaultGroupDistance = group_distance;
	}
	public static int getDefaultGroupDistance() {
		return defaultGroupDistance;
	}
	public static void setSplitGroupDistance(int group_distance) {
		splitGroupDistance = group_distance;
	}
	public static int getSplitGroupDistance() {
		return splitGroupDistance;
	}
	public static void setMaxShift(int max) {
		maxShift = max;
	}
	public static int getMaxShift() {
		return maxShift;
	}
	public static void setMaxMove(int max) {
		maxMove = max;
	}
	public static int getMaxMove() {
		return maxMove;
	}
	
}
