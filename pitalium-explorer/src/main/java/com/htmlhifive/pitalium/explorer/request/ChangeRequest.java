package com.htmlhifive.pitalium.explorer.request;

/**
 * 変更要求
 *
 * @author sasaki
 */
public abstract class ChangeRequest {

	/** 結果 */
	private Integer result;

	/** コメント */
	private String comment;

	/**
	 * 結果を取得する。
	 *
	 * @return 結果
	 */
	public Integer getResult() {
		return result;
	}

	/**
	 * 結果を設定する。
	 *
	 * @param result 結果
	 */
	public void setResult(Integer result) {
		this.result = result;
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

}
