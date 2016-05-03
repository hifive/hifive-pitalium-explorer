package com.htmlhifive.pitalium.explorer.response;

public class Result {
	String expectedFilename;
	String targetFilename;
	double entireSimilarity;
	int numberOfDiffRec;
	
	public Result(String expectedFilename, String targetFilename, double entireSimilarity, int numberOfDiffRec){
		this.expectedFilename = expectedFilename;
		this.targetFilename = targetFilename;
		this.entireSimilarity = entireSimilarity;
		this.numberOfDiffRec = numberOfDiffRec;
	}
	public Result(){
		this("", "", 0, 0);
	}

	public String getExpectedFilename() {
		return expectedFilename;
	}
	public void setExpectedFilename(String expectedFilename) {
		this.expectedFilename = expectedFilename;
	}
	public String getTargetFilename() {
		return targetFilename;
	}
	public void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}
	public double getEntireSimilarity() {
		return entireSimilarity;
	}
	public void setEntireSimilarity(double entireSimilarity) {
		this.entireSimilarity = entireSimilarity;
	}
	public int getNumberOfDiffRec() {
		return numberOfDiffRec;
	}
	public void setNumberOfDiffRec(int numberOfDiffRec) {
		this.numberOfDiffRec = numberOfDiffRec;
	}
}
