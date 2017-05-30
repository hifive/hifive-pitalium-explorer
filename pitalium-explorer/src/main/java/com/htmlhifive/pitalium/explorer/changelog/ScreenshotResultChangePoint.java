package com.htmlhifive.pitalium.explorer.changelog;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * スクリーンショットの実行結果の変更された箇所
 *
 * @author sasaki
 */
public class ScreenshotResultChangePoint {
	/** スクリーンショットID */
	private final String screenshotId;

	/** テストクラス名 */
	private final String testClass;

	/** テストメソッド名 */
	private final String testMethod;

	/** 機能 */
	private final Map<String, ?> capabilities;

	/**
	 * コンストラクタ
	 *
	 * @param screenshotId スクリーンショットID
	 * @param testClass テストクラス名
	 * @param testMethod テストメソッド名
	 * @param capabilities 機能
	 */
	@JsonCreator
	public ScreenshotResultChangePoint(@JsonProperty("screenshotId") String screenshotId,
			@JsonProperty("testClass") String testClass, @JsonProperty("testMethod") String testMethod,
			@JsonProperty("capabilities") Map<String, ?> capabilities) {
		this.screenshotId = screenshotId;
		this.testClass = testClass;
		this.testMethod = testMethod;
		this.capabilities = capabilities;
	}

	/**
	 * スクリーンショットIDを取得する。
	 *
	 * @return スクリーンショットID
	 */
	public String getScreenshotId() {
		return screenshotId;
	}

	/**
	 * テストクラス名を取得する。
	 *
	 * @return テストクラス名
	 */
	public String getTestClass() {
		return testClass;
	}

	/**
	 * テストメソッド名を取得する。
	 *
	 * @return テストメソッド名
	 */
	public String getTestMethod() {
		return testMethod;
	}

	/**
	 * 機能を取得する。
	 *
	 * @return 機能
	 */
	public Map<String, ?> getCapabilities() {
		return capabilities;
	}
}
