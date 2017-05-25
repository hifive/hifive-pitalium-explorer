package com.htmlhifive.pitalium.explorer.request;

import javax.validation.constraints.NotNull;

public class TargetResultChangeRequest extends ChangeRequest {

	@NotNull
	private Integer screenshotId;

	@NotNull
	private Integer targetId;

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
}
