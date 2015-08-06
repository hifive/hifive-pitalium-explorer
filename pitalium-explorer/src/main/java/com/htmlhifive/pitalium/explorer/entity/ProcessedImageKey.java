/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.Serializable;

import javax.persistence.Id;

public class ProcessedImageKey implements Serializable {
	/**
	 * serial id
	 */
	private static final long serialVersionUID = -5282087196673413726L;

	@Id
	private Integer screenshotId;
	@Id
	private String algorithm;

	/**
	 * Constructors
	 */
	public ProcessedImageKey() {
	}

	public ProcessedImageKey(Integer screenshotId, String algorithm) {
		this.screenshotId = screenshotId;
		this.algorithm = algorithm;
	}

	/**
	 * @return the screenshotId
	 */
	public Integer getScreenshotId() {
		return screenshotId;
	}

	/**
	 * @param screenshotId the screenshotId to set
	 */
	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
	}

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
}
