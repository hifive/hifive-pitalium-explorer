package com.htmlhifive.testexplorer.file;

import java.io.File;
import java.io.FileNotFoundException;

import com.htmlhifive.testexplorer.entity.ConfigRepository;
import com.htmlhifive.testexplorer.entity.ProcessedImageKey;
import com.htmlhifive.testexplorer.entity.Repositories;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.TestExecution;

public class ImageFileUtility {
	private final Repositories repositories;
	
	public ImageFileUtility(Repositories repositories)
	{
		this.repositories = repositories;
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
		TestExecution testExecution = repositories.getTestExecutionRepository().findOne(screenshot.getTestExecutionId());
		String relativePath =
				"images" +
				File.separatorChar +
				testExecution.getTimeString() +
				File.separatorChar +
				screenshot.getTestClass() +
				File.separatorChar +
				screenshot.getFileName() + ".png";

		File file = new File(getAbsoluteFilePath(relativePath));
		if (!file.exists() || !file.isFile()) { throw new FileNotFoundException(file.getAbsolutePath() + " Not Found."); }
		return file;
	}


	public String newProcessedFilePath(ProcessedImageKey key) {
		String idDirectory = new File("processed-images", key.getScreenshotId().toString()).getPath();
		return new File(idDirectory, key.getAlgorithm() + ".png").getPath();
	}
}
