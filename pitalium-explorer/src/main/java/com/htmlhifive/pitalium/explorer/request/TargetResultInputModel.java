package com.htmlhifive.pitalium.explorer.request;

import com.sun.istack.internal.NotNull;

public class TargetResultInputModel {

	@NotNull
	private Integer screenshotscreenshotId;

	@NotNull
	private Integer targetId;

	@NotNull
	private Integer result;

	private String message;

	public Integer getScreenshotscreenshotId() {
		return screenshotscreenshotId;
	}

	public void setScreenshotscreenshotId(Integer screenshotscreenshotId) {
		this.screenshotscreenshotId = screenshotscreenshotId;
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
