package com.htmlhifive.pitalium.explorer.response;

import java.util.ArrayList;
import java.util.List;

public class ResultListOfExpected {
	int id;
	String expectedFilename;
	List<Result> resultList;
	long executionTime;
	
	public ResultListOfExpected(int id, String expectedFilename, List<Result> resultList, long executionTime) {
		this.id = id;
		this.expectedFilename = expectedFilename;
		this.resultList = resultList;
		this.executionTime = executionTime;
	}
	
	public ResultListOfExpected(){
		this(0, "", new ArrayList<Result>(), 0);
	}

	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public String getExpectedFilename() {
		return expectedFilename;
	}

	public void setExpectedFilename(String expectedFilename) {
		this.expectedFilename = expectedFilename;
	}

	public List<Result> getResultList() {
		return resultList;
	}

	public void setResultList(List<Result> resultList) {
		this.resultList = resultList;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
}
