package com.htmlhifive.pitalium.explorer.log;

import java.util.Map;

public class ScreenshotResultChangePoint {

	private final String screenshotId;

	private final String testClass;

	private final String testMethod;

	private final Map<String, ?> capabilities;

	public ScreenshotResultChangePoint(String screenshotId, String testClass, String testMethod, Map<String, ?> capabilities) {
		this.screenshotId = screenshotId;
		this.testClass = testClass;
		this.testMethod = testMethod;
		this.capabilities = capabilities;
	}

	public String getScreenshotId() {
		return screenshotId;
	}

	public String getTestClass() {
		return testClass;
	}

	public String getTestMethod() {
		return testMethod;
	}

	public Map<String, ?> getCapabilities() {
		return capabilities;
	}
}
