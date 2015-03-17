package com.htmlhifive.testexplorer.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Screenshot {
	@SequenceGenerator(name="Screenshot_generator", sequenceName="Seq_Screenshot", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Screenshot_generator")
	@Id
	private Integer sid;
	private String screenshotId;
	private Boolean comparisonResult;
	private String fileName;
	@ManyToOne
	@JoinColumn(name="resultId", nullable=false, updatable=false)
	private Result result;
	public Integer getSid() {
		return sid;
	}
	public void setSid(Integer sid) {
		this.sid = sid;
	}
	public String getScreenshotId() {
		return screenshotId;
	}
	public void setScreenshotId(String screenshotId) {
		this.screenshotId = screenshotId;
	}
	public Boolean getComparisonResult() {
		return comparisonResult;
	}
	public void setComparisonResult(Boolean comparisonResult) {
		this.comparisonResult = comparisonResult;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
	}
}
