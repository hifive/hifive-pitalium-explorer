package com.htmlhifive.testexplorer.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Screenshot implements Serializable {

	private static final long serialVersionUID = 1L;

	@SequenceGenerator(name="Screenshot_generator", sequenceName="Seq_Screenshot", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Screenshot_generator")
	@Id
	private Integer id;

	@ManyToOne
	@JoinColumn(name="expectedId", nullable=true, updatable=false)
	private Screenshot expectedScreenshot;

	private String fileName;

	private Boolean comparisonResult;

	private String testClass;

	private String testMethod;

	private String testScreen;

	@ManyToOne
	@JoinColumn(name="testExecutionId", nullable=false, updatable=false)
	private TestExecution testExecution;

	@ManyToOne
	@JoinColumn(name="testEnvironmentId", nullable=false, updatable=false)
	private TestEnvironment testEnvironment;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Screenshot getExpectedScreenshot() {
		return expectedScreenshot;
	}

	public void setExpectedScreenshot(Screenshot expectedScreenshot) {
		this.expectedScreenshot = expectedScreenshot;
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

}
