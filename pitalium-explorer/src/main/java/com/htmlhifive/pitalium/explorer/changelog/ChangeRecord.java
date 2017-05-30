package com.htmlhifive.pitalium.explorer.changelog;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 変更記録
 *
 * @author sasaki
 */
public class ChangeRecord {
	/** ID */
	private final Integer id;

	/** リクエストパラメータ */
	private final Map<String, ?> requestParams;

	/** コメント */
	private final String comment;

	/** 結果ID */
	private final String resultId;

	/** 更新日時 */
	private final Date updateTime;

	/** 変更箇所 */
	private ChangePoint changePoints;

	/**
	 * コンストラクタ
	 *
	 * @param id ID
	 * @param requestParams リクエストパラメータ
	 * @param comment コメント
	 * @param resultId 結果ID
	 * @param updateTime 更新日時
	 */
	@JsonCreator
	public ChangeRecord(@JsonProperty("id") int id, @JsonProperty("requestParams") Map<String, ?> requestParams,
			@JsonProperty("comment") String comment, @JsonProperty("resultId") String resultId,
			@JsonProperty("updateTime") Date updateTime) {
		this.id = id;
		this.requestParams = requestParams;
		this.comment = comment;
		this.resultId = resultId;
		this.updateTime  = updateTime;
	}

	/**
	 * IDを取得する。
	 *
	 * @return ID
	 */
	public Integer getId() {
		return id;
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
	 * コメントを取得する。
	 *
	 * @return コメント
	 */
	public String getComment() {
		return comment;
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
	 * 更新日時を取得する。
	 *
	 * @return 更新日時
	 */
	public Date getUpdateTime() {
		return updateTime;
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
