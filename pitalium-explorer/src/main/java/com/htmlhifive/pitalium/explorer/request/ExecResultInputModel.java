package com.htmlhifive.pitalium.explorer.request;

import com.sun.istack.internal.NotNull;

public class ExecResultInputModel {

	@NotNull
	private Integer testExecutionId;

	@NotNull
	private Integer result;

	private String message;

	public Integer getTestExecutionId() {
		return testExecutionId;
	}

	public void setTestExecutionId(Integer testExecutionId) {
		this.testExecutionId = testExecutionId;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
