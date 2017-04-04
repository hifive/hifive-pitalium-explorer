/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.htmlhifive.pitalium.explorer.entity.Area;
import com.htmlhifive.pitalium.explorer.entity.AreaRepository;
import com.htmlhifive.pitalium.explorer.entity.Config;
import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImage;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageKey;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageRepository;
import com.htmlhifive.pitalium.explorer.entity.Repositories;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotRepository;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TargetRepository;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionRepository;
import com.htmlhifive.pitalium.explorer.file.FileUtility;
import com.htmlhifive.pitalium.explorer.request.ExecResultInputModel;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.explorer.service.ScreenshotIdService;

public class ExplorerDBPersister extends DBPersister implements ExplorerPersister {

	private TestExecutionRepository testExecutionRepo;
	private ScreenshotRepository screenshotRepo;
	private TargetRepository targetRepo;
	private AreaRepository areaRepo;
	private ProcessedImageRepository processedImageRepo;
	private ConfigRepository configRepo;

	private ScreenshotIdService screenshotIdService;

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
	public void setScreenshotIdService(ScreenshotIdService screenshotIdService) {
		this.screenshotIdService = screenshotIdService;
	}

	@Override
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page,
			int pageSize) {
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			// TODO 検索条件が入っていないからバグかなぁ?
			long count = testExecutionRepo.count();
			pageSize = (int) Math.min(count, Integer.MAX_VALUE);
		}
		PageRequest pageRequest = new PageRequest(page - 1, pageSize, new Sort(Sort.Direction.DESC, "id"));
		return testExecutionRepo.search(searchTestMethod, searchTestScreen, pageRequest);
	}

	@Override
	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen) {
		return screenshotRepo.findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(testExecutionId,
				searchTestMethod, searchTestScreen);
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

		// targetIdはシーケンシャルにふっているため、
		// 引数でわたってきたtargetIdと期待値となる画像のScreenshotクラスから取得したTargetクラスのIDは一致しない。
		// そのために以下の処理を必要とする。
		if (target == null) {
			Area area = areaRepo.getByTargetIdAndExcluded(targetId, Boolean.FALSE);
			for (Target t : screenshot.getTargets()) {
				if (StringUtils.equals(t.getArea().getSelectorType(), area.getSelectorType())
						&& StringUtils.equals(t.getArea().getSelectorValue(), area.getSelectorValue())
						&& t.getArea().getSelectorIndex() == area.getSelectorIndex()) {
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
		String child = screenshot.getTestExecution().getTimeString() + File.separatorChar + screenshot.getTestClass()
				+ File.separatorChar + target.getFileName();
		File image = new File(config.getValue(), child);

		// Send PNG image
		return image;
	}

	@Override
	public Page<Screenshot> findScreenshot(Integer testExecutionId, Integer testEnvironmentId, int page, int pageSize) {
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = Integer.MAX_VALUE;
		}
		PageRequest pageRequest = new PageRequest(page - 1, pageSize,
				new Sort(Sort.Direction.ASC, "testClass", "testMethod", "testScreen"));
		return screenshotRepo.findByTestExecutionIdAndTestEnvironmentId(testExecutionId, testEnvironmentId,
				pageRequest);
	}

	@Override
	public Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(int page, int pageSize) {
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = Integer.MAX_VALUE;
		}

		/*
		 * findTestExecutionAndEnvironmentで実行するSQLの結果は正しく取れるのだが、 totalを取得するHQLが正しくないため
		 * (Pageオブジェクトを作成するために、Springで実行されている。HQLは自動生成されているため、書き換えることは無理そう。)、 Pageオブジェクトを自力で作成することにする。
		 */
		List<TestExecutionAndEnvironment> list = screenshotRepo.findTestExecutionAndEnvironment();
		int size = list.size();

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<TestExecutionAndEnvironment> resultList = new ArrayList<>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			resultList.add(list.get(i));
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<TestExecutionAndEnvironment>(resultList, pageable, size);
	}

	@Override
	public File searchProcessedImageFile(Integer screenshotId, String algorithm) {
		File result = null;
		ProcessedImage p = processedImageRepo.findOne(new ProcessedImageKey(screenshotId, algorithm));
		if (p != null) {
			// FIXME 直したい
			result = new File(
					new FileUtility(new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo))
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

	@Override
	public List<TestExecutionResult> updateExecResult(List<ExecResultInputModel> inputModelList) {
		// TODO 自動生成されたメソッド・スタブ
		throw new UnsupportedOperationException();
	}

}
