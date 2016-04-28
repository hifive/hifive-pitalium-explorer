/*
 *  Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

package com.htmlhifive.pitalium.explorer.service;

/**
 * スクリーンショットに割り当てるシーケンシャルIDを管理するサービス
 */
public interface ScreenshotIdService {

	/**
	 * スクリーンショット種別
	 */
	enum ScreenshotType {
		/**
		 * Pitaliumが保存したスクリーンショットファイル
		 */
		PITALIUM_FILE, /**
		 * ファイルアップロード等から保存した一時ファイル
		 */
		TEMPORARY_FILE
	}

	/**
	 * 次のスクリーンショット識別IDを採番します。
	 *
	 * @param type スクリーンショット種別
	 * @return スクリーンショット識別ID
	 */
	int nextId(ScreenshotType type);

	/**
	 * スクリーンショット識別IDに対応する種別を取得します。
	 * @param id スクリーンショット識別ID
	 * @return IDに対応するスクリーンショット種別
	 * @throws IllegalArgumentException IDに対応する種別が存在しない場合
	 */
	ScreenshotType getScreenshotType(int id);

}
