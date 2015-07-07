package com.htmlhifive.testexplorer.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ScreenshotDifference implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@ManyToOne
	@JoinColumn(name="screenshotId", nullable=false, updatable=false)
	private Screenshot screenshot;

	@Id
	private String type;

	private String data;

	public Screenshot getScreenshot() {
		return screenshot;
	}

	public void setScreenshot(Screenshot screenshot) {
		this.screenshot = screenshot;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
