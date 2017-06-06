/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

@Entity
public class Screenshot implements Serializable {

	private static final long serialVersionUID = 1L;

	@SequenceGenerator(name = "Screenshot_generator", sequenceName = "Seq_Screenshot", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Screenshot_generator")
	@Id
	private Integer id;

	@Column(name = "testScreen", insertable = false, updatable = false)
	private String screenshotName;

	@Column(name = "expectedId")
	private Integer expectedScreenshotId;

	private String fileName;

	private Boolean comparisonResult;

	private Boolean isUpdated;

	private String testClass;

	private String testMethod;

	private String testScreen;

	@ManyToOne
	@JoinColumn(name = "testExecutionId", nullable = false, updatable = false)
	private TestExecution testExecution;

	@ManyToOne
	@JoinColumn(name = "testEnvironmentId", nullable = false, updatable = false)
	private TestEnvironment testEnvironment;

	@Transient
	private List<Target> targets;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getScreenshotName() {
		return screenshotName;
	}

	public void setScreenshotName(String screenshotName) {
		this.screenshotName = screenshotName;
	}

	public Integer getExpectedScreenshotId() {
		return expectedScreenshotId;
	}

	public void setExpectedScreenshotId(Integer expectedScreenshotId) {
		this.expectedScreenshotId = expectedScreenshotId;
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

	public Boolean isUpdated() {
		return isUpdated;
	}

	public void setIsUpdated(Boolean isUpdate) {
		this.isUpdated = isUpdate;
	}

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

	public String getTestScreen() {
		return testScreen;
	}

	public void setTestScreen(String testScreen) {
		this.testScreen = testScreen;
	}

	public TestExecution getTestExecution() {
		return testExecution;
	}

	public void setTestExecution(TestExecution testExecution) {
		this.testExecution = testExecution;
	}

	public TestEnvironment getTestEnvironment() {
		return testEnvironment;
	}

	public void setTestEnvironment(TestEnvironment testEnvironment) {
		this.testEnvironment = testEnvironment;
	}

	public List<Target> getTargets() {
		return targets;
	}

	public void setTargets(List<Target> targets) {
		this.targets = targets;
	}

}
