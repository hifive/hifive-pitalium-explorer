package com.htmlhifive.pitalium.explorer.response;

public class Result {
//	int id;
//	String expectedFilename;
	String targetFilename;
	double entireSimilarity;
	int numberOfDiffRec;
//	long executionTime;
	
	public Result(String targetFilename, double entireSimilarity, int numberOfDiffRec){
//		this.expectedFilename = expectedFilename;
		this.targetFilename = targetFilename;
		this.entireSimilarity = entireSimilarity;
		this.numberOfDiffRec = numberOfDiffRec;
//		this.executionTime = System.currentTimeMillis();
	}
	public Result(){
//		this(0, "", "", 0, 0);
		this("", 0, 0);
	}

//	public int getId(){
//		return id;
//	}
//	public void setId(int id){
//		this.id = id;
//	}
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
	public int getNumberOfDiffRec() {
		return numberOfDiffRec;
	}
	public void setNumberOfDiffRec(int numberOfDiffRec) {
		this.numberOfDiffRec = numberOfDiffRec;
	}
//	public long getExecutionTime(){
//		return executionTime;
//	}
//	public void setExecutionTime(long executionTime){
//		this.executionTime = executionTime;
//	}
}
