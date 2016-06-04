package com.htmlhifive.pitalium.explorer.image;

/**
 * Default parameter setting for comparison
 */
public class ComparisonParameters {

	/* default setting for comparison parameters */
	
	// difference threshold to ignore small differences
	private static double diffThreshold = 0.1;
	
	// parameters for categorization
	private static double scalingDiffCriterion = 0.3;
	private static double scalingFeatureCriterion = 0.85;
	private static double missingDiffCriterion = 0.6;
	
	/**
	 * Constructor
	 */
	public ComparisonParameters() {};

	public void setDiffThreshold(double threshold) {
		diffThreshold = threshold;
	}
	public static double getDiffThreshold() {
		return diffThreshold;
	}
	public void setScalingDiffCriterion (double criterion) {
		scalingDiffCriterion = criterion;
	}
	public static double getScalingDiffCriterion () {
		return scalingDiffCriterion;
	}
	public void setScalingFeatureCriterion (double criterion) {
		scalingFeatureCriterion = criterion;
	}
	public static double getScalingFeatureCriterion () {
		return scalingFeatureCriterion;
	}
	public void setMissingDiffCriterion (double criterion) {
		missingDiffCriterion = criterion;
	}
	public static double getMissingDiffCriterion () {
		return missingDiffCriterion;
	}	

}
