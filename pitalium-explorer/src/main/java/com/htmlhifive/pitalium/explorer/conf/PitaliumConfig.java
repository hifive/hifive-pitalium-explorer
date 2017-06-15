/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */

package com.htmlhifive.pitalium.explorer.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.htmlhifive.pitalium.core.result.TestResultManager;
import com.htmlhifive.pitalium.explorer.io.ExplorerPersister;

@Configuration
public class PitaliumConfig {

	@Bean(name = "configExplorerPersister")
	public ExplorerPersister explorerPersister() {
		return (ExplorerPersister) TestResultManager.getInstance().getPersister();
	}

}
