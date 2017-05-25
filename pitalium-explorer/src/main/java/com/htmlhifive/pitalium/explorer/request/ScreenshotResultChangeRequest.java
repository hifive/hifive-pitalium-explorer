package com.htmlhifive.pitalium.explorer.request;

import javax.validation.constraints.NotNull;

public class ScreenshotResultChangeRequest extends ChangeRequest {

	@NotNull
	private Integer screenshotId;

	public Integer getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
	}
}
