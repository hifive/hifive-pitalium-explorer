package com.htmlhifive.testexplorer.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Result {
	@SequenceGenerator(name="Result_generator", sequenceName="Seq_Result", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Result_generator")
	@Id
	private Integer resultId;
	private String executeTime;
	private String expectedId;
	public Integer getResultId() {
		return resultId;
	}
	public void setResultId(Integer resultId) {
		this.resultId = resultId;
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
