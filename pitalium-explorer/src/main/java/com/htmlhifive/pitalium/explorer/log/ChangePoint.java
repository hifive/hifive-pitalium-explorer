package com.htmlhifive.pitalium.explorer.log;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangePoint {

	private Boolean execResult;

	private List<ScreenshotResultChangePoint> screenshotResults;

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
