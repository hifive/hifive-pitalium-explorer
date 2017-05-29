package com.htmlhifive.pitalium.explorer.log;

import java.util.Date;
import java.util.Map;

/**
 * 変更記録
 *
 * @author sasaki
 */
public class ChangeRecord {
	/** ID */
	private Integer id;

	/** リクエストパラメータ */
	private Map<String, ?> requestParams;

	/** コメント */
	private String comment;

	/** 結果ID */
	private String resultId;

	/** 更新日時 */
	private Date updateTime;

	/** 変更箇所 */
	private ChangePoint changePoints;

	/**
	 * IDを取得する。
	 *
	 * @return ID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * IDを設定する。
	 *
	 * @param id ID
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * リクエストパラメータを取得する。
	 *
	 * @return リクエストパラメータ
	 */
	public Map<String, ?> getRequestParams() {
		return requestParams;
	}

	/**
	 * リクエストパラメータを設定する。
	 *
	 * @param requestParams リクエストパラメータ
	 */
	public void setRequestParams(Map<String, ?> requestParams) {
		this.requestParams = requestParams;
	}

	/**
	 * コメントを取得する。
	 *
	 * @return コメント
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * コメントを設定する。
	 *
	 * @param comment コメント
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * 結果IDを取得する。
	 *
	 * @return 結果ID
	 */
	public String getResultId() {
		return resultId;
	}

	/**
	 * 結果IDを設定する。
	 *
	 * @param resultId 結果ID
	 */
	public void setResultId(String resultId) {
		this.resultId = resultId;
	}

	/**
	 * 更新日時を取得する。
	 *
	 * @return 更新日時
	 */
	public Date getUpdateTime() {
		return updateTime;
	}

	/**
	 * 更新日時を設定する。
	 *
	 * @param updateTime 更新日時
	 */
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 変更箇所を取得する。
	 *
	 * @return 変更箇所
	 */
	public ChangePoint getChangePoints() {
		return changePoints;
	}

	/**
	 * 変更箇所を設定する。
	 *
	 * @param changePoints 変更箇所
	 */
	public void setChangePoints(ChangePoint changePoints) {
		this.changePoints = changePoints;
	}
}
