package com.htmlhifive.pitalium.explorer.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.htmlhifive.pitalium.core.model.IndexDomSelector;

public class TargetResultChangePoint {
	private final ScreenshotResultChangePoint screenshot;

	private final IndexDomSelector target;

	/**
	 *
	 * @param screenshot
	 * @param target
	 */
	@JsonCreator
	public TargetResultChangePoint(@JsonProperty("screenshot") ScreenshotResultChangePoint screenshot,
			@JsonProperty("target") IndexDomSelector target) {
		this.screenshot = screenshot;
		this.target = target;
	}

	public ScreenshotResultChangePoint getScreenshot() {
		return screenshot;
	}

	public IndexDomSelector getTarget() {
		return target;
	}
}
