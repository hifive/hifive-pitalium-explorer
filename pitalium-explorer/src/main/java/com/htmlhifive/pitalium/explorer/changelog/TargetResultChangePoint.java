package com.htmlhifive.pitalium.explorer.changelog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.htmlhifive.pitalium.core.model.IndexDomSelector;

/**
 * 対象領域の実行結果の変更された箇所
 *
 * @author sasaki
 */
public class TargetResultChangePoint {
	/** スクリーンショットの実行結果の変更された箇所 */
	private final ScreenshotResultChangePoint screenshot;

	/** 対象領域となるDOM要素 */
	private final IndexDomSelector target;

	/**
	 * コンストラクタ
	 *
	 * @param screenshot スクリーンショットの実行結果の変更された箇所
	 * @param target 対象領域となるDOM要素
	 */
	@JsonCreator
	public TargetResultChangePoint(@JsonProperty("screenshot") ScreenshotResultChangePoint screenshot,
			@JsonProperty("target") IndexDomSelector target) {
		this.screenshot = screenshot;
		this.target = target;
	}

	/**
	 * スクリーンショットの実行結果の変更された箇所を取得する。
	 *
	 * @return スクリーンショットの実行結果の変更された箇所
	 */
	public ScreenshotResultChangePoint getScreenshot() {
		return screenshot;
	}

	/**
	 * 対象領域となるDOM要素を取得する。
	 *
	 * @return 対象領域となるDOM要素
	 */
	public IndexDomSelector getTarget() {
		return target;
	}
}
