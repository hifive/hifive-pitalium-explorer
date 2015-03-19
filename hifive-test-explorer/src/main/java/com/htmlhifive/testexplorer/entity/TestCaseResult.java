package com.htmlhifive.testexplorer.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class TestCaseResult {
	@SequenceGenerator(name="Result_generator", sequenceName="Seq_TestCaseResult", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Result_generator")
	@Id
	private Integer id;
	private String executeTime;
	private String expectedId;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getExecuteTime() {
		return executeTime;
	}
	public void setExecuteTime(String executeTime) {
		this.executeTime = executeTime;
	}
	public String getExpectedId() {
		return expectedId;
	}
	public void setExpectedId(String expectedId) {
		this.expectedId = expectedId;
	}

}
