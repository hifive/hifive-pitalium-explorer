package com.htmlhifive.testexplorer.model;

import java.io.Serializable;

public class Target implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = 210338790984032914L;

	private Integer id;
	private String selector;
	private String selectorType;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getSelector() {
		return selector;
	}
	public void setSelector(String selector) {
		this.selector = selector;
	}
	public String getSelectorType() {
		return selectorType;
	}
	public void setSelectorType(String selectorType) {
		this.selectorType = selectorType;
	}
}
