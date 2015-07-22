/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.conf;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

	@Value("#{apiConf.diffImageCache}")
	private String diffImageCache;

	@Value("#{apiConf.resultsDir}")
	private String resultsDir;
	
	@Value("#{apiConf.edgeCacheDir}")
	private String edgeCacheDir;
	
	/**
	 * Diff画像のキャッシュ機能を有効にするか判定する。
	 * 
	 * @return 有効にする場合は、true。
	 */
	public Boolean isDiffImageCacheOn() {
		return StringUtils.equals(diffImageCache, "on");
	}

	public File getResultDir() {
		return new File(resultsDir);
	}

	public File getEdgeCacheDir() {
		return new File(edgeCacheDir);
	}
}
