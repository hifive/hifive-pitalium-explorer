package com.htmlhifive.pitalium.explorer.log;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScreenshotResultChangePoint {

	private final String screenshotId;

	private final String testClass;

	private final String testMethod;

	private final Map<String, ?> capabilities;

	/**
	 *
	 * @param screenshotId
	 * @param testClass
	 * @param testMethod
	 * @param capabilities
	 */
	@JsonCreator
	public ScreenshotResultChangePoint(@JsonProperty("screenshotId") String screenshotId,
			@JsonProperty("testClass") String testClass, @JsonProperty("testMethod") String testMethod,
			@JsonProperty("capabilities") Map<String, ?> capabilities) {
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
