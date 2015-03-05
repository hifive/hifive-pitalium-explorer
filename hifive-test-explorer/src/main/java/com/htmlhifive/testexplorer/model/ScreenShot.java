/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.model;


public class ScreenShot {
	private String fileName;
	private String screenshotId;
	private Boolean result;
	private Capability capability;
	private ResultFile resultFile;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(String screenshotId) {
		this.screenshotId = screenshotId;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	public Capability getCapability() {
		return capability;
	}

	public void setCapability(Capability capability) {
		this.capability = capability;
	}

	public ResultFile getResultFile() {
		return resultFile;
	}

	public void setResultFile(ResultFile resultFile) {
		this.resultFile = resultFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capability == null) ? 0 : capability.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((resultFile == null) ? 0 : resultFile.hashCode());
		result = prime * result + ((screenshotId == null) ? 0 : screenshotId.hashCode());
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
		ScreenShot other = (ScreenShot) obj;
		if (capability == null) {
			if (other.capability != null)
				return false;
		} else if (!capability.equals(other.capability))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (resultFile == null) {
			if (other.resultFile != null)
				return false;
		} else if (!resultFile.equals(other.resultFile))
			return false;
		if (screenshotId == null) {
			if (other.screenshotId != null)
				return false;
		} else if (!screenshotId.equals(other.screenshotId))
			return false;
		return true;
	}
}
