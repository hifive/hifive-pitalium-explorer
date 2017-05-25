package com.htmlhifive.pitalium.explorer.request;

import javax.validation.constraints.NotNull;

public class ChangeRequest {

	@NotNull
	private Integer result;

	private String comment;

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
