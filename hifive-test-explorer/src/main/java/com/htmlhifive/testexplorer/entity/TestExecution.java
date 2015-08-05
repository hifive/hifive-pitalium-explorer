/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import com.htmlhifive.pitalium.core.model.ExecResult;

@Entity
public class TestExecution implements Serializable {

	private static final long serialVersionUID = 1L;

	@SequenceGenerator(name = "TestExecution_generator", sequenceName = "Seq_TestExecution", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TestExecution_generator")
	@Id
	private Integer id;

	private String label;

	private Timestamp time;

	private ExecResult execResult;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public String getTimeString() {
		SimpleDateFormat directoryFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return directoryFormat.format(time);
	}

	public ExecResult getExecResult() {
		return execResult;
	}

	public void setExecResult(ExecResult execResult) {
		this.execResult = execResult;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TestExecution))
			return false;
		if (obj == this)
			return true;
		TestExecution other = (TestExecution) obj;
		return this.id == other.id;
	}
}
