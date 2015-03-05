/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.model;

public class ResultFile {
	private String executeTime;
	private String expectedId;
	private ScreenShot[] screenShots;

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
	public ScreenShot[] getScreenShots() {
		return screenShots;
	}
	public void setScreenShots(ScreenShot[] screenShots) {
		this.screenShots = screenShots;
	}
}
