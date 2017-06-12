package com.htmlhifive.pitalium.explorer.request;

/**
 * テスト全体の実行結果変更要求
 *
 * @author sasaki
 */
public class ExecResultChangeRequest extends ChangeRequest {

	/** テスト実行ID */
	private Integer testExecutionId;

	/**
	 * テスト実行IDを取得する。
	 *
	 * @return テスト実行ID
	 */
	public Integer getTestExecutionId() {
		return testExecutionId;
	}

	/**
	 * テスト実行IDを設定する。
	 *
	 * @param testExecutionId テスト実行ID
	 */
	public void setTestExecutionId(Integer testExecutionId) {
		this.testExecutionId = testExecutionId;
	}
}
