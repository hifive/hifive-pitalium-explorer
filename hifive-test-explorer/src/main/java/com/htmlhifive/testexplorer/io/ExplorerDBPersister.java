/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.htmlhifive.testexplorer.entity.ProcessedImage;
import com.htmlhifive.testexplorer.entity.ProcessedImageKey;
import com.htmlhifive.testexplorer.entity.ProcessedImageRepository;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.file.ImageFileUtility;
import com.htmlhifive.testexplorer.response.TestExecutionResult;

public class ExplorerDBPersister extends DBPersister implements ExplorerPersister {

	private TestExecutionRepository testExecutionRepo;
	private ScreenshotRepository screenshotRepo;
	private ProcessedImageRepository processedImageRepo;

	// FIXME 使わないように何とかしたい。
	private ImageFileUtility imageFileUtil;

	public void setImageFileUtility(ImageFileUtility utility) {
		imageFileUtil = utility;
	}

	public void setTestExecutionRepository(TestExecutionRepository repository) {
		testExecutionRepo = repository;
	}
	
	public void setScreenshotRepository(ScreenshotRepository repository) {
		screenshotRepo = repository;
	}

	public void setProcessedImageRepository(ProcessedImageRepository repository) {
		processedImageRepo = repository;
	}
	
	@Override
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, 
			String searchTestScreen, int page, int pageSize) {
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			// TODO 検索条件が入っていないからバグかなぁ?
			long count = testExecutionRepo.count();
			pageSize = (int)Math.min(count, Integer.MAX_VALUE);
		}
		PageRequest pageRequest = new PageRequest(
				page - 1, pageSize, new Sort(Sort.Direction.DESC, "id"));
		return testExecutionRepo.search(searchTestMethod, searchTestScreen, pageRequest);
	}

	@Override
	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, 
			String searchTestScreen) {
		return screenshotRepo.findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(
				testExecutionId, searchTestMethod, searchTestScreen);
	}

	@Override
	public Screenshot getScreenshot(Integer screenshotId) {
		return screenshotRepo.findOne(screenshotId);
	}

	@Override
	public File getImage(Integer id) throws IOException {
		Screenshot screenshot = getScreenshot(id);

		if (screenshot == null) {
			return null;
		}

		// Send PNG image
		return imageFileUtil.getFile(screenshot);
	}

	@Override
	public File searchProcessedImageFile(Integer screenshotId, String algorithm) {
		File result = null;
		ProcessedImage p = processedImageRepo.findOne(new ProcessedImageKey(screenshotId, algorithm));
		if (p != null)
		{
			result = new File(imageFileUtil.getAbsoluteFilePath(p.getFileName()));
		}
		return result;
	}

	@Override
	public List<Screenshot> findNotProcessedEdge() {
		Integer lastIndex = -1;
		// TODO SQLを修正する必要がある。
		return screenshotRepo.findNotProcessedEdge(lastIndex);
	}

	@Override
	public boolean exsitsProcessedImage(Integer screenshotId, String algorithm) {
		ProcessedImageKey key = new ProcessedImageKey(screenshotId, algorithm);
		return processedImageRepo.exists(key);
	}

	@Override
	public String getEdgeFileName(Integer screenshotId, String algorithm) {
		return new File(String.valueOf(screenshotId), algorithm + ".png").getPath();
	}

	@Override
	public void saveProcessedImage(Integer screenshotId, String algorithm, String edgeFileName) {
		ProcessedImage newEntry = new ProcessedImage();
		newEntry.setScreenshotId(screenshotId);
		newEntry.setAlgorithm(algorithm);
		newEntry.setFileName(edgeFileName);
		processedImageRepo.saveAndFlush(newEntry);
	}

}
