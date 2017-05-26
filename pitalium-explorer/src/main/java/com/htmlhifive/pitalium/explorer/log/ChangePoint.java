package com.htmlhifive.pitalium.explorer.log;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ChangePoint {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean execResult;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<ScreenshotResultChangePoint> screenshotResults;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<TargetResultChangePoint> targetResults;

	public Boolean getExecResult() {
		return execResult;
	}

	public void setExecResult(Boolean execResult) {
		this.execResult = execResult;
	}

	public List<ScreenshotResultChangePoint> getScreenshotResults() {
		return screenshotResults;
	}

	public void setScreenshotResults(List<ScreenshotResultChangePoint> screenshotResults) {
		this.screenshotResults = screenshotResults;
	}

	public List<TargetResultChangePoint> getTargetResults() {
		return targetResults;
	}

	public void setTargetResults(List<TargetResultChangePoint> targetResults) {
		this.targetResults = targetResults;
	}

}
