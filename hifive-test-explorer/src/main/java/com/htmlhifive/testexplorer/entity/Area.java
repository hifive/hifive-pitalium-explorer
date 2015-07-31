/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Area implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SequenceGenerator(name="Area_generator", sequenceName="Seq_Area", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Area_generator")
	@Id
	private Integer areaId;

	private Integer targetId;

	private String selectorType;

	private String selectorValue;

	private Integer selectorIndex;
	
	private double x;

	private double y;

	private double width;

	private double height;

	private boolean excluded;

	public Integer getAreaId() {
		return areaId;
	}

	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public String getSelectorType() {
		return selectorType;
	}

	public void setSelectorType(String selectorType) {
		this.selectorType = selectorType;
	}

	public String getSelectorValue() {
		return selectorValue;
	}

	public void setSelectorValue(String selectorValue) {
		this.selectorValue = selectorValue;
	}

	public Integer getSelectorIndex() {
		return selectorIndex;
	}

	public void setSelectorIndex(Integer selectorIndex) {
		this.selectorIndex = selectorIndex;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public boolean isExcluded() {
		return excluded;
	}

	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

}
