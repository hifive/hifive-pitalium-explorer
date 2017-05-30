package com.htmlhifive.pitalium.explorer.request;

/**
 * スクリーンショットの実行結果変更要求
 *
 * @author sasaki
 */
public class ScreenshotResultChangeRequest extends ChangeRequest {

	/** スクリーンショットID */
	private Integer screenshotId;

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
}
