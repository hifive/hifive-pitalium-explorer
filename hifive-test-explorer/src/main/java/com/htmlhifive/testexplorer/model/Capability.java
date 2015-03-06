/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.model;

import java.io.Serializable;

public class Capability implements Serializable {
	private String testClass;
	private String testMethod;
	private String platform;
	private String platformVersion;
	private String deviceName;
	private String browserName;
	private String browserVersion;
	private String label;
	private ScreenShot screenShot;

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public String getTestMethod() {
		return testMethod;
	}

	public void setTestMethod(String testMethod) {
		this.testMethod = testMethod;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	public void setPlatformVersion(String plartformVersion) {
		this.platformVersion = plartformVersion;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getBrowserName() {
		return browserName;
	}

	public void setBrowserName(String browserName) {
		this.browserName = browserName;
	}

	public String getBrowserVersion() {
		return browserVersion;
	}

	public void setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ScreenShot getScreenShot() {
		return screenShot;
	}

	public void setScreenShot(ScreenShot screenShot) {
		this.screenShot = screenShot;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((browserName == null) ? 0 : browserName.hashCode());
		result = prime * result + ((browserVersion == null) ? 0 : browserVersion.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((platformVersion == null) ? 0 : platformVersion.hashCode());
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		result = prime * result + ((testClass == null) ? 0 : testClass.hashCode());
		result = prime * result + ((testMethod == null) ? 0 : testMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Capability other = (Capability) obj;
		if (browserName == null) {
			if (other.browserName != null)
				return false;
		} else if (!browserName.equals(other.browserName))
			return false;
		if (browserVersion == null) {
			if (other.browserVersion != null)
				return false;
		} else if (!browserVersion.equals(other.browserVersion))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (platformVersion == null) {
			if (other.platformVersion != null)
				return false;
		} else if (!platformVersion.equals(other.platformVersion))
			return false;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		if (testClass == null) {
			if (other.testClass != null)
				return false;
		} else if (!testClass.equals(other.testClass))
			return false;
		if (testMethod == null) {
			if (other.testMethod != null)
				return false;
		} else if (!testMethod.equals(other.testMethod))
			return false;
		return true;
	}
}
