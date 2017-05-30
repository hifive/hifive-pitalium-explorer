package com.htmlhifive.pitalium.explorer.request;

/**
 * 対象領域の実行結果変更要求
 *
 * @author sasaki
 */
public class TargetResultChangeRequest extends ChangeRequest {

	/** スクリーンショットID */
	private Integer screenshotId;

	/** ターゲットID */
	private Integer targetId;

	/**
	 * スクリーンショットIDを取得する。
	 *
	 * @return スクリーンショットID
	 */
	public Integer getScreenshotId() {
		return screenshotId;
	}

	/**
	 * スクリーンショットIDを設定する。
	 *
	 * @param screenshotId スクリーンショットID
	 */
	public void setScreenshotId(Integer screenshotId) {
		this.screenshotId = screenshotId;
	}

	/**
	 * ターゲットIDを取得する。
	 *
	 * @return ターゲットID
	 */
	public Integer getTargetId() {
		return targetId;
	}

	/**
	 * ターゲットIDを設定する。
	 *
	 * @param targetId ターゲットID
	 */
	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}
}
