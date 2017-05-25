package com.htmlhifive.pitalium.explorer.log;

import java.util.Date;
import java.util.Map;

/**
 * 変更記録
 *
 * @author sasaki
 */
public class ChangeRecord {
	private Integer id;

	private Map<String, ?> requestParams;

	private String comment;

	private String resultId;

	private Date updateTime;

	private ChangePoint changePoints;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Map<String, ?> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, ?> requestParams) {
		this.requestParams = requestParams;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getResultId() {
		return resultId;
	}

	public void setResultId(String resultId) {
		this.resultId = resultId;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public ChangePoint getChangePoints() {
		return changePoints;
	}

	public void setChangePoints(ChangePoint changePoints) {
		this.changePoints = changePoints;
	}
}
