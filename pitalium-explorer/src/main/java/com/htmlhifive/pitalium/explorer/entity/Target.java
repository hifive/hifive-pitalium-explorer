/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

@Entity
public class Target implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SequenceGenerator(name="Target_generator", sequenceName="Seq_Target", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Target_generator")
	@Id
	private Integer targetId;

	private Integer screenshotId;

	private String fileName;
	
	private Boolean comparisonResult;

	@Transient
	private Area area;
	
	@Transient
	private List<Area> excludeAreas;

	public Target() {
	}
	
	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public Integer getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Boolean getComparisonResult() {
		return comparisonResult;
	}

	public void setComparisonResult(Boolean comparisonResult) {
		this.comparisonResult = comparisonResult;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public List<Area> getExcludeAreas() {
		return excludeAreas;
	}

	public void setExcludeAreas(List<Area> excludeAreas) {
		this.excludeAreas = excludeAreas;
	}

}
