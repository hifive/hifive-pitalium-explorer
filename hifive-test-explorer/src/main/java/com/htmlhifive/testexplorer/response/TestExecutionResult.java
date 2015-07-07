package com.htmlhifive.testexplorer.response;

import com.htmlhifive.testexplorer.entity.TestExecution;

public class TestExecutionResult {

	private TestExecution testExecution;

	private Integer passedCount;

	private Integer totalCount;

	public TestExecutionResult(TestExecution testExecution, Long passedCount, Long totalCount) {
		this.testExecution = testExecution;
		this.passedCount = passedCount.intValue();
		this.totalCount = totalCount.intValue();
	}

	public TestExecution getTestExecution() {
		return testExecution;
	}

	public void setTestExecution(TestExecution testExecution) {
		this.testExecution = testExecution;
	}

	public Integer getPassedCount() {
		return passedCount;
	}

	public void setPassedCount(Integer passedCount) {
		this.passedCount = passedCount;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
}
