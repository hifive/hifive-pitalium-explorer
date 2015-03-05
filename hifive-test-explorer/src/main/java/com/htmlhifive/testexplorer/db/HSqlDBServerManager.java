/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.db;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class HSqlDBServerManager implements InitializingBean, DisposableBean {
	private Logger logger = LoggerFactory.getLogger(HSqlDBServerManager.class);
	private Properties serverProperties;
	private Server server;

	@Autowired
	private ServletContext context;

	public Properties getServerProperties() {
		return serverProperties;
	}

	public void setServerProperties(Properties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (server != null) {
			logger.info("HSQLDBサーバは起動済みです。");
			return;
		}
		String webAppPath = context.getRealPath("");

		// server.database.x に相対パスが指定された場合は、webappを基点とした絶対パスに変換する
		for (Object key : serverProperties.keySet()) {
			String keyStr = (String) key;

			if (keyStr.indexOf("server.database.") == -1) {
				continue;
			}

			String val = serverProperties.getProperty(keyStr);

			// 絶対パスか判定
			if (val == null || "".equals(val) || val.indexOf(":/") != -1) {
				continue;
			}

			// 相対パスの先頭に/が含まれていない場合は/を付与する
			if (!val.startsWith("/")) {
				val = "/" + val;
			}

			Path path = Paths.get(webAppPath + "/../../.." + val).normalize();
			String absolutePath = path.toString().replace("\\\\", "/");
			serverProperties.put(keyStr, absolutePath);
		}

		try {
			logger.info("HSQLDBサーバを起動します...");
			server = new Server();
			server.setRestartOnShutdown(false);
			server.setNoSystemExit(true);
			server.setProperties(new HsqlProperties(serverProperties));
			server.setLogWriter(null);
			server.setErrWriter(null);
			server.start();
			logger.info("HSQLDBサーバを起動しました。");
		} catch (IOException | AclFormatException e) {
			logger.error("HSQLDBサーバの起動に失敗しました。", e);
		}
	}

	@Override
	public void destroy() throws Exception {
		if (server == null) {
			return;
		}

		server.stop();
		logger.info("HSQLDBサーバを停止しました。");
		server = null;
	}
}
