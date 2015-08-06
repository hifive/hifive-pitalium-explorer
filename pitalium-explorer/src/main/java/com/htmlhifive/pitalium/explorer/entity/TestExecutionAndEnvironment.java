/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestExecutionAndEnvironment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer executionId;

	private Integer enviromentId; 

	private String executionTime;

	private String platform;

	private String platformVersion;

	private String deviceName;

	private String browserName;

	private String browserVersion;

	public TestExecutionAndEnvironment() {
	}

	public TestExecutionAndEnvironment(Integer executionId, Date executionTime, 
			Integer enviromentId, String platform, String platformVersion, String deviceName, 
			String browserName, String browserVersion) {
		this.executionId = executionId;
		this.executionTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(executionTime);
		this.enviromentId = enviromentId;
		this.platform = platform;
		this.platformVersion = platformVersion;
		this.deviceName = deviceName;
		this.browserName = browserName;
		this.browserVersion = browserVersion;
	}
	
	public Integer getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Integer executionId) {
		this.executionId = executionId;
	}

	public Integer getEnviromentId() {
		return enviromentId;
	}

	public void setEnviromentId(Integer enviromentId) {
		this.enviromentId = enviromentId;
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
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

	public void setPlatformVersion(String platformVersion) {
		this.platformVersion = platformVersion;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((browserName == null) ? 0 : browserName.hashCode());
		result = prime * result + ((browserVersion == null) ? 0 : browserVersion.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((enviromentId == null) ? 0 : enviromentId.hashCode());
		result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
		result = prime * result + ((executionTime == null) ? 0 : executionTime.hashCode());
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		result = prime * result + ((platformVersion == null) ? 0 : platformVersion.hashCode());
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
		TestExecutionAndEnvironment other = (TestExecutionAndEnvironment) obj;
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
		if (enviromentId == null) {
			if (other.enviromentId != null)
				return false;
		} else if (!enviromentId.equals(other.enviromentId))
			return false;
		if (executionId == null) {
			if (other.executionId != null)
				return false;
		} else if (!executionId.equals(other.executionId))
			return false;
		if (executionTime == null) {
			if (other.executionTime != null)
				return false;
		} else if (!executionTime.equals(other.executionTime))
			return false;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		if (platformVersion == null) {
			if (other.platformVersion != null)
				return false;
		} else if (!platformVersion.equals(other.platformVersion))
			return false;
		return true;
	}

}
