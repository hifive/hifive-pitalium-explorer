/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(ProcessedImageKey.class)
public class ProcessedImage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private Integer screenshotId;

	@Id
	private String algorithm;

	private String fileName;

	public Integer getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
