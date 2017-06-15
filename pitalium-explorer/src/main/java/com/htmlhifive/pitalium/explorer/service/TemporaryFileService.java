/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */

package com.htmlhifive.pitalium.explorer.service;

import java.io.File;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.Target;

/**
 * ブラウザからアップロードされた一時ファイルを扱うサービス
 */
public interface TemporaryFileService {

	/**
	 * ブラウザからアップロードされたファイルを受け取って一時領域へ保存し、そのスクリーンショット識別IDを返します。
	 *
	 * @param files ブラウザからアップロードされたファイル一覧
	 * @return アップロードされたファイルに対応するスクリーンショット識別ID一覧
	 */
	List<Integer> upload(List<MultipartFile> files);

	Screenshot getScreenshot(Integer screenshotId);

	Target getTarget(Integer screenshotId, Integer targetId);

	File getImage(Integer screenshotId, Integer targetId);

}
