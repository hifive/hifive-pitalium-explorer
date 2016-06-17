package com.htmlhifive.pitalium.explorer.response;

public class Result {
	int id;
//	String expectedFilename;
	String targetFilename;
	double entireSimilarity;
	double minSimilarity;
	int numberOfDiffRec;
	int offsetX;
	int offsetY;
	boolean moveExpected;
//	long executionTime;
	
	public Result(int id, String targetFilename, double entireSimilarity, double minSimilarity, int numberOfDiffRec, int offsetX, int offsetY, boolean moveExpected){
		this.id = id;
//		this.expectedFilename = expectedFilename;
		this.targetFilename = targetFilename;
		this.entireSimilarity = entireSimilarity;
		this.minSimilarity = minSimilarity;
		this.numberOfDiffRec = numberOfDiffRec;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.moveExpected = moveExpected;		
		
//		this.executionTime = System.currentTimeMillis();
	}
	public Result() {
	//int i, String targetFilePath, double entireSimilarity2, int j, int offsetX2, int offsetY2, boolean moveExpectedImage2){
//		this(0, "", "", 0, 0);
		this(0, "", 0, 0, 0, 0, 0, false);
	}

	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
//	public String getExpectedFilename() {
//		return expectedFilename;
//	}
//	public void setExpectedFilename(String expectedFilename) {
//		this.expectedFilename = expectedFilename;
//	}
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
	public double getMinSimilarity() {
		return minSimilarity;
	}
	public void setMinSimilarity(double minSimilarity) {
		this.minSimilarity = minSimilarity;
	}
	public int getNumberOfDiffRec() {
		return numberOfDiffRec;
	}
	public void setNumberOfDiffRec(int numberOfDiffRec) {
		this.numberOfDiffRec = numberOfDiffRec;
	}
	public int getOffsetX() {
		return offsetX;
	}
	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}
	public int getOffsetY() {
		return offsetY;
	}
	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}
	public boolean getMoveExpected() {
		return moveExpected;
	}
	public void setMoveExpected(boolean moveExpected) {
		this.moveExpected = moveExpected;
	}
//	public long getExecutionTime(){
//		return executionTime;
//	}
//	public void setExecutionTime(long executionTime){
//		this.executionTime = executionTime;
//	}
}
