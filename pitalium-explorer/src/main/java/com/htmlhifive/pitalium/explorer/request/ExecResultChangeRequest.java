package com.htmlhifive.pitalium.explorer.request;

import javax.validation.constraints.NotNull;

public class ExecResultChangeRequest extends ChangeRequest {

	@NotNull
	private Integer testExecutionId;

	public Integer getTestExecutionId() {
		return testExecutionId;
	}

	public void setTestExecutionId(Integer testExecutionId) {
		this.testExecutionId = testExecutionId;
	}
}
