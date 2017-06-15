/*
 * Copyright (C) 2015-2017 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htmlhifive.pitalium.explorer.changelog;

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
