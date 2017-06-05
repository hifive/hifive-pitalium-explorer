package com.htmlhifive.pitalium.explorer.conf;

import java.util.Map;

import com.htmlhifive.pitalium.core.config.FilePersisterConfig;
import com.htmlhifive.pitalium.core.config.PersisterConfig;
import com.htmlhifive.pitalium.core.config.PtlConfiguration;

@PtlConfiguration
public class ExplorerPersisterConfig extends PersisterConfig {

	private Map<String, FilePersisterConfig> files;

	private String key;

	public Map<String, FilePersisterConfig> getFiles() {
		return files;
	}

	public void setFiles(Map<String, FilePersisterConfig> files) {
		this.files = files;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public FilePersisterConfig getFile() {
		if (files == null || files.isEmpty()) {
			return super.getFile();
		}

		if (key != null) {
			return files.get(key);
		}

		return (FilePersisterConfig) files.values().toArray()[0];
	}


}
