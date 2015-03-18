/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.response;

import java.io.Serializable;

import com.htmlhifive.testexplorer.model.ResultFile;
import com.htmlhifive.testexplorer.model.ScreenShot;

public class TestExecutionTime implements Serializable {

	private final String executionTime;
	private Boolean comparisonResult;

	public TestExecutionTime(ResultFile resultFile) {
		executionTime = resultFile.getExecuteTime();

		ScreenShot[] screenShots = resultFile.getScreenShots();
		for (int j = 0, screenLen = screenShots.length; j < screenLen; j++) {
			Boolean result = screenShots[j].getResult();
			if (result == null) {
				continue;
			}
			if (comparisonResult == null) {
				comparisonResult = result;
			} else {
				comparisonResult = comparisonResult & screenShots[j].getResult();
			}
		}
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public Boolean getComparisonResult() {
		return comparisonResult;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comparisonResult == null) ? 0 : comparisonResult.hashCode());
		result = prime * result + ((executionTime == null) ? 0 : executionTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestExecutionTime other = (TestExecutionTime) obj;
		if (comparisonResult == null) {
			if (other.comparisonResult != null)
				return false;
		} else if (!comparisonResult.equals(other.comparisonResult))
			return false;
		if (executionTime == null) {
			if (other.executionTime != null)
				return false;
		} else if (!executionTime.equals(other.executionTime))
			return false;
		return true;
	}

}
