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
	private Integer id;
	private String screenshotId;
	private Boolean result;
	private String fileName;
	@ManyToOne
	@JoinColumn(name="testCaseResultId", nullable=false, updatable=false)
	private TestCaseResult testCaseResult;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public TestCaseResult getTestCaseResult() {
		return testCaseResult;
	}
	public void setTestCaseResult(TestCaseResult testCaseResult) {
		this.testCaseResult = testCaseResult;
	}
}
