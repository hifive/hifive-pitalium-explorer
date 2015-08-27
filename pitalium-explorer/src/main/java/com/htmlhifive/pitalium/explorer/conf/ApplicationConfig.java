/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.conf;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration()
public class ApplicationConfig {

	@Value("${diffImageCache:}")
	private String diffImageCache;

	@Value("${uploadPath:C:/temp}")
	private String uploadPath;
	
	/**
	 * Diff画像のキャッシュ機能を有効にするか判定する。
	 * 
	 * @return 有効にする場合は、true。
	 */
	public Boolean isDiffImageCacheOn() {
		return StringUtils.equals(diffImageCache, "on");
	}

	/**
	 * アップロードされたファイルの格納先を取得する。
	 * 
	 * @return ファイルの格納先
	 */
	public String getUploadPath() {
		return uploadPath;
	}
}
