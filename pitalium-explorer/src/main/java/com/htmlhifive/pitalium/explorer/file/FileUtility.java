/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.file;

import java.io.File;
import java.io.FileNotFoundException;

import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageKey;
import com.htmlhifive.pitalium.explorer.entity.Repositories;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;

public class FileUtility {
	private final Repositories repositories;

	public FileUtility(Repositories repositories) {
		this.repositories = repositories;
	}
	
	public static String getPairResultFilename(String expectedFilePath, String targetFilePath, int id){
		return expectedFilePath.replace("/", "--") + "__" +
			   targetFilePath.replace("/",  "--") + "__" +
			   Integer.toString(id) + ".json";
	}

	/**
	 * Convert relativePath from DB into absolute path
	 * 
	 * @param relativePath
	 * @return converted path
	 */
	public String getAbsoluteFilePath(String relativePath) {
		File file = new File(repositories.getConfigRepository().findOne(ConfigRepository.ABSOLUTE_PATH_KEY).getValue(),
				relativePath);
		return file.getPath();
	}

	/**
	 * Get the file of a screenshot
	 * 
	 * @param screenshot the input screenshot
	 * @return a file related with the input screenshot
	 * @throws FileNotFoundException
	 */
	public File getFile(Screenshot screenshot) throws FileNotFoundException {
		TestExecution testExecution = screenshot.getTestExecution();
		String relativePath = testExecution.getTimeString() + File.separatorChar + screenshot.getTestClass()
				+ File.separatorChar + screenshot.getFileName() + ".png";

		File file = new File(getAbsoluteFilePath(relativePath));
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException(file.getAbsolutePath() + " Not Found.");
		}
		return file;
	}

	public String newProcessedFilePath(ProcessedImageKey key) {
		String idDirectory = new File("processed-images", key.getScreenshotId().toString()).getPath();
		return new File(idDirectory, key.getAlgorithm() + ".png").getPath();
	}
}
