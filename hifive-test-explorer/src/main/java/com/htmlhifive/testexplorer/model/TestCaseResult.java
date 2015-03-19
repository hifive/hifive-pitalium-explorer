/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.model;

import java.io.Serializable;

public class TestCaseResult implements Serializable {
	private String executeTime;
	private String expectedId;
	private Screenshot[] screenshots;

	public String getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(String executeTime) {
		this.executeTime = executeTime;
	}

	public String getExpectedId() {
		return expectedId;
	}

	public void setExpectedId(String expectedId) {
		this.expectedId = expectedId;
	}

	public Screenshot[] getScreenShots() {
		return screenshots;
	}

	public void setScreenShots(Screenshot[] screenshots) {
		this.screenshots = screenshots;
	}
}
