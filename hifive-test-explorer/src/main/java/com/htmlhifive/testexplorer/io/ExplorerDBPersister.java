/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.htmlhifive.testexplorer.entity.Area;
import com.htmlhifive.testexplorer.entity.AreaRepository;
import com.htmlhifive.testexplorer.entity.Config;
import com.htmlhifive.testexplorer.entity.ConfigRepository;
import com.htmlhifive.testexplorer.entity.ProcessedImage;
import com.htmlhifive.testexplorer.entity.ProcessedImageKey;
import com.htmlhifive.testexplorer.entity.ProcessedImageRepository;
import com.htmlhifive.testexplorer.entity.Repositories;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.Target;
import com.htmlhifive.testexplorer.entity.TargetRepository;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.file.ImageFileUtility;
import com.htmlhifive.testexplorer.response.TestExecutionResult;

public class ExplorerDBPersister extends DBPersister implements ExplorerPersister {

	private TestExecutionRepository testExecutionRepo;
	private ScreenshotRepository screenshotRepo;
	private TargetRepository targetRepo;
	private AreaRepository areaRepo;
	private ProcessedImageRepository processedImageRepo;
	private ConfigRepository configRepo;

	public void setTestExecutionRepository(TestExecutionRepository repository) {
		testExecutionRepo = repository;
	}
	
	public void setScreenshotRepository(ScreenshotRepository repository) {
		screenshotRepo = repository;
	}

	public void setTargetRepository(TargetRepository repository) {
		targetRepo = repository;
	}

	public void setAreaRepository(AreaRepository repository) {
		areaRepo = repository;
	}

	public void setProcessedImageRepository(ProcessedImageRepository repository) {
		processedImageRepo = repository;
	}

	public void setConfigRepository(ConfigRepository repository) {
		configRepo = repository;
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
		Screenshot screenshot = screenshotRepo.findOne(screenshotId);
		List<Target> targetList = targetRepo.findByScreenshotId(screenshotId);
		screenshot.setTargets(targetList);

		for (Target target : targetList) {
			List<Area> areaList = areaRepo.findByTargetId(target.getTargetId());
			
			List<Area> excludeAreaList = new ArrayList<>();
			for (Area area : areaList) {
				if (!area.isExcluded()) {
					target.setArea(area);
				} else {
					excludeAreaList.add(area);
				}
			}
			target.setExcludeAreas(excludeAreaList);
		}
		
		return screenshot;
	}

	@Override
	public Target getTarget(Integer screenshotId, Integer targetId) {
		Screenshot screenshot = getScreenshot(screenshotId);

		if (screenshot == null) {
			return null;
		}

		Target target = null;
		for (Target t : screenshot.getTargets()) {
			if (t.getTargetId().intValue() == targetId.intValue()) {
				target = t;
				break;
			}
		}
		
		// FIXME データの持ち方を再検討する必要があるかも。
		// targetIdはシーケンシャルにふっているため、
		// 引数でわたってきたtargetIdと期待値となる画像のScreenshotクラスから取得したTargetクラスのIDは一致しない。
		// そのために以下の処理を必要とする。
		if (target == null) {
			Area area = areaRepo.getByTargetIdAndExcluded(targetId, Boolean.FALSE);
			for (Target t : screenshot.getTargets()) {
				if (StringUtils.equals(t.getArea().getSelectorType(), area.getSelectorType()) && 
						StringUtils.equals(t.getArea().getSelectorValue(), area.getSelectorValue())) {
					target = t;
					break;
				}
			}
		}
		return target;
	}

	@Override
	public File getImage(Integer screenshotId, Integer targetId) throws IOException {
		Screenshot screenshot = getScreenshot(screenshotId);

		if (screenshot == null) {
			return null;
		}

		Target target = getTarget(screenshotId, targetId);

		Config config = configRepo.findOne(ConfigRepository.ABSOLUTE_PATH_KEY);
		String child = screenshot.getTestExecution().getTimeString() 
				+ File.separatorChar + screenshot.getTestClass() 
				+ File.separatorChar + target.getFileName();
		File image = new File(config.getValue(), child);

		// Send PNG image
		return image;
	}

	@Override
	public File searchProcessedImageFile(Integer screenshotId, String algorithm) {
		File result = null;
		ProcessedImage p = processedImageRepo.findOne(new ProcessedImageKey(screenshotId, algorithm));
		if (p != null) {
			// FIXME 直したい
			result = new File(new ImageFileUtility(
					new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo))
				.getAbsoluteFilePath(p.getFileName()));
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
