package com.htmlhifive.pitalium.explorer.log;

import com.htmlhifive.pitalium.core.model.IndexDomSelector;
import com.htmlhifive.pitalium.core.model.ScreenAreaResult;

public class TargetResultChangePoint {
	private final ScreenshotResultChangePoint screenshot;

	private final ScreenAreaResult target;

	public TargetResultChangePoint(ScreenshotResultChangePoint screenshot, ScreenAreaResult target) {
		this.target = target;
		this.screenshot = screenshot;
	}

	public ScreenshotResultChangePoint getScreenshot() {
		return screenshot;
	}

	public IndexDomSelector getTarget() {
		return target.getSelector();
	}
}
