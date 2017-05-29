package com.htmlhifive.pitalium.explorer.log;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 変更箇所
 *
 * @author sasaki
 */
public class ChangePoint {
	/** テスト全体の実行結果が変更されたか否か */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean execResult;

	/** スクリーンショットの実行結果の変更された箇所リスト */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<ScreenshotResultChangePoint> screenshotResults;

	/** 対象領域の実行結果の変更された箇所リスト */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<TargetResultChangePoint> targetResults;

	/**
	 * テスト全体の実行結果が変更されたか否かを取得する。
	 *
	 * @return テスト全体の実行結果が変更されたか否か
	 */
	public Boolean getExecResult() {
		return execResult;
	}

	/**
	 * テスト全体の実行結果が変更されたか否かを設定する。
	 *
	 * @param execResult テスト全体の実行結果が変更されたか否か
	 */
	public void setExecResult(Boolean execResult) {
		this.execResult = execResult;
	}

	/**
	 * スクリーンショットの実行結果の変更された箇所リストを取得する。
	 *
	 * @return スクリーンショットの実行結果の変更された箇所リスト
	 */
	public List<ScreenshotResultChangePoint> getScreenshotResults() {
		return screenshotResults;
	}

	/**
	 * スクリーンショットの実行結果の変更された箇所リストを設定する。
	 *
	 * @param screenshotResults スクリーンショットの実行結果の変更された箇所リスト
	 */
	public void setScreenshotResults(List<ScreenshotResultChangePoint> screenshotResults) {
		this.screenshotResults = screenshotResults;
	}

	/**
	 * 対象領域の実行結果の変更された箇所リストを取得する。
	 *
	 * @return 対象領域の実行結果の変更された箇所リスト
	 */
	public List<TargetResultChangePoint> getTargetResults() {
		return targetResults;
	}

	/**
	 * 対象領域の実行結果の変更された箇所リストを設定する。
	 *
	 * @param targetResults 対象領域の実行結果の変更された箇所リスト
	 */
	public void setTargetResults(List<TargetResultChangePoint> targetResults) {
		this.targetResults = targetResults;
	}

}
