package com.htmlhifive.pitalium.explorer.request;

import com.sun.istack.internal.NotNull;

public class ScreenshotResultInputModel {

	@NotNull
	private Integer screenshotId;

	@NotNull
	private Integer result;

	private String message;

	public Integer getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
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
