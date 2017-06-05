package com.htmlhifive.pitalium.explorer.conf;

import java.util.Map;

import com.htmlhifive.pitalium.core.config.FilePersisterConfig;
import com.htmlhifive.pitalium.core.config.PersisterConfig;
import com.htmlhifive.pitalium.core.config.PtlConfiguration;

@PtlConfiguration
public class ExplorerPersisterConfig extends PersisterConfig {

	private Map<String, FilePersisterConfig> files;

	private String defaultResultKey;

	public Map<String, FilePersisterConfig> getFiles() {
		return files;
	}

	public void setFiles(Map<String, FilePersisterConfig> files) {
		this.files = files;
	}

	public String getDefaultResultKey() {
		return defaultResultKey;
	}

	public void setDefaultResultKey(String key) {
		this.defaultResultKey = key;
	}

	@Override
	public FilePersisterConfig getFile() {
		if (files == null || files.isEmpty()) {
			return super.getFile();
		}

		if (defaultResultKey != null) {
			return files.get(defaultResultKey);
		}

		return (FilePersisterConfig) files.values().toArray()[0];
	}


}
