package com.htmlhifive.pitalium.explorer.request;

import javax.validation.constraints.NotNull;

public class TargetResultInputModel {

	@NotNull
	private Integer screenshotId;

	@NotNull
	private Integer targetId;

	@NotNull
	private Integer result;

	private String message;

	public Integer getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
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
