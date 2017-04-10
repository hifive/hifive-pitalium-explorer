/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.io;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.htmlhifive.pitalium.core.config.FilePersisterConfig;
import com.htmlhifive.pitalium.core.io.FilePersister;
import com.htmlhifive.pitalium.core.io.PersistMetadata;
import com.htmlhifive.pitalium.core.model.ExecResult;
import com.htmlhifive.pitalium.core.model.IndexDomSelector;
import com.htmlhifive.pitalium.core.model.ScreenAreaResult;
import com.htmlhifive.pitalium.core.model.ScreenshotResult;
import com.htmlhifive.pitalium.core.model.SelectorType;
import com.htmlhifive.pitalium.core.model.TargetResult;
import com.htmlhifive.pitalium.core.model.TestResult;
import com.htmlhifive.pitalium.core.selenium.PtlCapabilities;
import com.htmlhifive.pitalium.explorer.entity.Area;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.request.ExecResultInputModel;
import com.htmlhifive.pitalium.explorer.request.ScreenshotResultInputModel;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.explorer.service.ScreenshotIdService;
import com.htmlhifive.pitalium.image.model.RectangleArea;

public class ExplorerFilePersister extends FilePersister implements ExplorerPersister {

	private static Logger log = LoggerFactory.getLogger(ExplorerFilePersister.class);

	private ScreenshotIdService screenshotIdService;

	private Map<Integer, Screenshot> screenshotMap;
	private Map<Integer, List<Screenshot>> screenshotListMap;
	private Map<Integer, Target> targetMap;

	// 更新処理のために保持
	private Map<Integer, List<TestResult>> testResultMap;
	private Map<ScreenshotResult, Integer> screenshotIdMap;

	public ExplorerFilePersister() {
		// FIXME 独自のConfigに差し替える必要があるかも
		super();
	}

	public ExplorerFilePersister(FilePersisterConfig config) {
		// FIXME 独自のConfigに差し替える必要があるかも
		super(config);
	}

	@Override
	public void setScreenshotIdService(ScreenshotIdService screenshotIdService) {
		this.screenshotIdService = screenshotIdService;
	}

	@Override
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page,
			int pageSize) {
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new PageImpl<>(new ArrayList<TestExecutionResult>());
		}

		IOFileFilter filter = FileFilterUtils.nameFileFilter(getTestResultFileName());
		Collection<File> collection = FileUtils.listFiles(root, filter, TrueFileFilter.INSTANCE);

		screenshotMap = new HashMap<>();
		screenshotListMap = new HashMap<>();
		targetMap = new HashMap<>();

		testResultMap = new HashMap<>();
		screenshotIdMap = new HashMap<>();

		// 後続のページング処理用
		List<TestExecution> testExecutionList = new ArrayList<>();
		// Screenshotの関連を貼るための処理用
		Map<String, List<Screenshot>> workScreenshotListMap = new HashMap<>();
		Map<ScreenshotResult, Screenshot> workScreenshotMap = new HashMap<>();

		List<TestEnvironment> workEnvList = new ArrayList<>();

		int executionId = 0;
		int targetId = 0;
		int areaId = 0;
		int environmentId = 0;

		File[] files = collection.toArray(new File[collection.size()]);
		for (int i = 0, len = files.length; i < len; i++) {
			String executionDate = files[i].getParentFile().getParentFile().getName();
			PersistMetadata metadata = new PersistMetadata(executionDate, files[i].getParentFile().getName());
			TestResult testResult = super.loadTestResult(metadata);

			TestExecution testExecution = new TestExecution();
			DateTime dateTime = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss").parseDateTime(executionDate);
			testExecution.setTime(new Timestamp(dateTime.getMillis()));

			// 重複チェック
			boolean exists = false;
			for (TestExecution exec : testExecutionList) {
				if (StringUtils.equals(exec.getTimeString(), testExecution.getTimeString())) {
					exists = true;
					testExecution = exec;
					break;
				}
			}

			List<TestResult> testResultList;
			if (!exists) {
				testExecutionList.add(testExecution);
				testExecution.setId(executionId);
				// 元データを追加する。
				testResultList = new ArrayList<>();
				testResultList.add(testResult);
				testResultMap.put(executionId, testResultList);
				// IDをインクリメント
				executionId++;
			} else {
				// 元データを追加する。
				testResultList = testResultMap.get(testExecution.getId());
				testResultList.add(testResult);
			}

			ExecResult result = testResult.getResult();
			if (result != null) {
				if (result == ExecResult.FAILURE || ExecResult.FAILURE.name().equals(testExecution.getExecResult())) {
					testExecution.setExecResult(ExecResult.FAILURE.name());
				} else {
					testExecution.setExecResult(ExecResult.SUCCESS.name());
				}
			}

			List<Screenshot> screenshotList = new ArrayList<>();
			for (ScreenshotResult screenshotResult : testResult.getScreenshotResults()) {
				int screenshotId = screenshotIdService.nextId(ScreenshotIdService.ScreenshotType.PITALIUM_FILE);
				Screenshot screenshot = createScreenshot(screenshotId, screenshotResult);
				screenshot.setTestExecution(testExecution);

				// Capability
				Map<String, ?> capabilities = screenshotResult.getCapabilities();
				TestEnvironment testEnvironment = createTestEnvironment(capabilities);
				// リストの何番目に一致するデータがあるか探す。
				int index = indexOf(workEnvList, testEnvironment);
				if (index == -1) {
					workEnvList.add(testEnvironment);
					testEnvironment.setId(environmentId);
					environmentId++;
				} else {
					testEnvironment = workEnvList.get(index);
				}
				screenshot.setTestEnvironment(testEnvironment);

				// Target
				List<TargetResult> targetResultList = screenshotResult.getTargetResults();
				List<Target> targetList = new ArrayList<>();
				for (TargetResult targetResult : targetResultList) {
					Target target = createTarget(targetId, screenshotId, targetResult, screenshotResult);
					targetList.add(target);

					// 比較対象の情報
					ScreenAreaResult screenAreaResult = targetResult.getTarget();
					Area area = createArea(areaId, targetId, screenAreaResult, false);
					areaId++;
					target.setArea(area);

					// 比較除外対象の情報
					List<ScreenAreaResult> screenAreaResultList = targetResult.getExcludes();
					List<Area> exculdeAreaList = new ArrayList<>();
					for (ScreenAreaResult excludeScreenAreaResult : screenAreaResultList) {
						Area excludeArea = createArea(areaId, targetId, excludeScreenAreaResult, true);
						exculdeAreaList.add(excludeArea);
						areaId++;
					}
					target.setExcludeAreas(exculdeAreaList);
					targetMap.put(targetId, target);
					targetId++;
				}
				screenshot.setTargets(targetList);

				screenshotMap.put(screenshotId, screenshot);
				screenshotList.add(screenshot);

				workScreenshotMap.put(screenshotResult, screenshot);
			}
			// TestExecution.id をキーとして格納する。
			List<Screenshot> list = screenshotListMap.get(testExecution.getId());
			if (list == null) {
				list = new ArrayList<>();
				screenshotListMap.put(testExecution.getId(), list);
			}
			list.addAll(screenshotList);
			// 実行日時をキーとして格納する。
			workScreenshotListMap.put(testExecution.getTimeString(), list);
		}

		// Screenshot同士の関連付けを行う。
		for (ScreenshotResult result : workScreenshotMap.keySet()) {
			String expectedId = result.getExpectedId();
			if (expectedId == null) {
				continue;
			}
			Screenshot screenshot = workScreenshotMap.get(result);

			// 期待値の実行日時と一致するScreenshotのリストを取得し、関連付けを行う。
			List<Screenshot> screenshotList = workScreenshotListMap.get(expectedId);
			for (Screenshot expectedScreenshot : screenshotList) {
				if (StringUtils.equals(expectedScreenshot.getTestClass(), screenshot.getTestClass())
						&& StringUtils.equals(expectedScreenshot.getFileName(), screenshot.getFileName())) {
					screenshot.setExpectedScreenshotId(expectedScreenshot.getId());
					break;
				}
			}
		}

		// 検索条件に一致するtestExecution.Idを取得する。
		List<Integer> extractExecutionIdList = extractTestExecutionId(searchTestMethod, searchTestScreen);

		// 検索条件に一致したTestExecutionのみに絞り込み、かつ最新から並ぶようにソートする。
		List<TestExecution> tempTestExecutionList = new ArrayList<>();
		for (int i = testExecutionList.size() - 1; i >= 0; i--) {
			TestExecution testExecution = testExecutionList.get(i);
			if (extractExecutionIdList.contains(testExecution.getId())) {
				tempTestExecutionList.add(testExecution);
			}
		}
		testExecutionList = tempTestExecutionList;

		int size = testExecutionList.size();

		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = (int) Math.min(size, Integer.MAX_VALUE);
		}

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<TestExecutionResult> resultList = new ArrayList<TestExecutionResult>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			TestExecution execution = testExecutionList.get(i);
			List<Screenshot> list = findScreenshot(execution.getId(), searchTestMethod, searchTestScreen);

			int passedCount = 0;
			int totalCount = 0;
			for (Screenshot s : list) {
				totalCount++;
				if (s.getComparisonResult() == null || s.getComparisonResult().booleanValue()) {
					// 成功時
					passedCount++;
				}
			}
			TestExecutionResult testExecutionResult = new TestExecutionResult(execution, Long.valueOf(passedCount),
					Long.valueOf(totalCount));
			resultList.add(testExecutionResult);
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<TestExecutionResult>(resultList, pageable, size);
	}

	private Screenshot createScreenshot(Integer screenshotId, ScreenshotResult screenshotResult) {
		// 更新処理用にscreenshotResultとscreenshotResultをマッピングする。
		screenshotIdMap.put(screenshotResult, screenshotId);
		Screenshot screenshot = new Screenshot();
		screenshot.setId(screenshotId);
		screenshot.setScreenshotName(screenshotResult.getScreenshotId());
		screenshot.setComparisonResult(
				screenshotResult.getResult() != null ? screenshotResult.getResult().isSuccess() : null);
		screenshot.setTestClass(screenshotResult.getTestClass());
		screenshot.setTestMethod(screenshotResult.getTestMethod());
		screenshot.setTestScreen(screenshotResult.getScreenshotId());

		PersistMetadata screenshotMetadata = new PersistMetadata(screenshotResult.getExpectedId(),
				screenshotResult.getTestClass(), screenshotResult.getTestMethod(), screenshotResult.getScreenshotId(),
				new PtlCapabilities(screenshotResult.getCapabilities()));
		screenshot.setFileName(getScreenshotImageFileName(screenshotMetadata));
		return screenshot;
	}

	private TestEnvironment createTestEnvironment(Map<String, ?> capabilities) {
		TestEnvironment testEnvironment = new TestEnvironment();
		testEnvironment.setBrowserName((String) capabilities.get("browserName"));
		testEnvironment.setBrowserVersion((String) capabilities.get("version"));
		testEnvironment.setDeviceName((String) capabilities.get("deviceName"));
		testEnvironment.setId(null);
		testEnvironment.setLabel(null);
		testEnvironment.setPlatform((String) capabilities.get("platform"));
		testEnvironment.setPlatformVersion((String) capabilities.get("platformVersion"));
		return testEnvironment;
	}

	private int indexOf(List<TestEnvironment> environmentList, TestEnvironment environment) {
		// idの値を除いて一致するデータを探す。
		for (int i = 0, size = environmentList.size(); i < size; i++) {
			TestEnvironment env = environmentList.get(i);
			if (StringUtils.equals(env.getPlatform(), environment.getPlatform())
					&& StringUtils.equals(env.getPlatformVersion(), environment.getPlatformVersion())
					&& StringUtils.equals(env.getDeviceName(), environment.getDeviceName())
					&& StringUtils.equals(env.getBrowserName(), environment.getBrowserName())
					&& StringUtils.equals(env.getBrowserVersion(), environment.getBrowserVersion())) {
				return i;
			}
		}
		return -1;
	}

	private Target createTarget(Integer targetId, Integer screenshotId, TargetResult targetResult,
			ScreenshotResult screenshotResult) {
		Target target = new Target();
		target.setTargetId(targetId);
		target.setScreenshotId(screenshotId);
		target.setComparisonResult(targetResult.getResult() != null ? targetResult.getResult().isSuccess() : null);

		// 比較対象の情報
		ScreenAreaResult screenAreaResult = targetResult.getTarget();

		PersistMetadata targetMetadata = new PersistMetadata(screenshotResult.getExpectedId(),
				screenshotResult.getTestClass(), screenshotResult.getTestMethod(), screenshotResult.getScreenshotId(),
				screenAreaResult.getSelector(), screenAreaResult.getRectangle(),
				new PtlCapabilities(screenshotResult.getCapabilities()));
		target.setFileName(getScreenshotImageFileName(targetMetadata));
		return target;
	}

	private Area createArea(Integer areaId, Integer targetId, ScreenAreaResult screenAreaResult, boolean excluded) {
		Area area = new Area();
		area.setAreaId(areaId);
		area.setTargetId(targetId);
		area.setSelectorType(screenAreaResult.getSelector().getType().name());
		area.setSelectorValue(screenAreaResult.getSelector().getValue());
		area.setSelectorIndex(screenAreaResult.getSelector().getIndex());
		area.setX(screenAreaResult.getRectangle().getX());
		area.setY(screenAreaResult.getRectangle().getY());
		area.setWidth(screenAreaResult.getRectangle().getWidth());
		area.setHeight(screenAreaResult.getRectangle().getHeight());
		area.setExcluded(excluded);
		return area;
	}

	private List<Integer> extractTestExecutionId(String searchTestMethod, String searchTestScreen) {
		boolean existsSearchTestMethod = searchTestMethod != null && !searchTestMethod.trim().isEmpty();
		boolean existsSearchTestScreen = searchTestScreen != null && !searchTestScreen.trim().isEmpty();

		List<Integer> extractExecutionIdList = new ArrayList<>();
		for (Entry<Integer, List<Screenshot>> entry : screenshotListMap.entrySet()) {
			List<Screenshot> screenshotList = entry.getValue();
			for (Screenshot screenshot : screenshotList) {
				if (existsSearchTestScreen) {
					if (!screenshot.getTestScreen().contains(searchTestScreen)) {
						continue;
					}
				}
				if (existsSearchTestMethod) {
					if (!screenshot.getTestMethod().contains(searchTestMethod)) {
						continue;
					}
				}
				extractExecutionIdList.add(entry.getKey());
				break;
			}
		}
		return extractExecutionIdList;
	}

	@Override
	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen) {
		if (screenshotListMap == null) {
			return new ArrayList<Screenshot>();
		}

		List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);

		// 検索条件に一致するScreenshotを抽出する
		boolean existsSearchTestMethod = searchTestMethod != null && !searchTestMethod.trim().isEmpty();
		boolean existsSearchTestScreen = searchTestScreen != null && !searchTestScreen.trim().isEmpty();

		List<Screenshot> extractScreenshotList = new ArrayList<>();
		for (Screenshot screenshot : screenshotList) {
			if (existsSearchTestScreen) {
				if (!screenshot.getTestScreen().contains(searchTestScreen)) {
					continue;
				}
			}
			if (existsSearchTestMethod) {
				if (!screenshot.getTestMethod().contains(searchTestMethod)) {
					continue;
				}
			}
			extractScreenshotList.add(screenshot);
		}
		return extractScreenshotList;
	}

	@Override
	public Screenshot getScreenshot(Integer screenshotId) {
		return screenshotMap != null ? screenshotMap.get(screenshotId) : null;
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
			Area area = targetMap.get(targetId).getArea();
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
		Area area = target.getArea();
		IndexDomSelector selector = new IndexDomSelector(SelectorType.valueOf(area.getSelectorType()),
				area.getSelectorValue(), area.getSelectorIndex());
		RectangleArea rectangleArea = new RectangleArea(area.getX(), area.getY(), area.getWidth(), area.getHeight());
		Map<String, String> map = new HashMap<>();
		TestEnvironment env = screenshot.getTestEnvironment();
		map.put("browserName", env.getBrowserName());
		map.put("version", env.getBrowserVersion());
		map.put("deviceName", env.getDeviceName());
		map.put("platform", env.getPlatform());
		map.put("platformVersion", env.getPlatformVersion());

		PersistMetadata metadata = new PersistMetadata(screenshot.getTestExecution().getTimeString(),
				screenshot.getTestClass(), screenshot.getTestMethod(), screenshot.getTestScreen(), selector,
				rectangleArea, new PtlCapabilities(map));
		// Send PNG image
		return super.getScreenshotImageFile(metadata);
	}

	@Override
	public Page<Screenshot> findScreenshot(Integer testExecutionId, Integer testEnvironmentId, int page, int pageSize) {
		if (screenshotListMap == null) {
			return new PageImpl<Screenshot>(new ArrayList<Screenshot>());
		}

		List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);

		List<Screenshot> extractScreenshotList = new ArrayList<>();
		for (Screenshot screenshot : screenshotList) {
			if (screenshot.getTestEnvironment().getId() == testEnvironmentId) {
				extractScreenshotList.add(screenshot);
			}
		}

		int size = extractScreenshotList.size();

		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = Integer.MAX_VALUE;
		}

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<Screenshot> resultList = new ArrayList<>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			resultList.add(extractScreenshotList.get(i));
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<Screenshot>(resultList, pageable, size);
	}

	@Override
	public Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(int page, int pageSize) {
		if (screenshotListMap == null) {
			return new PageImpl<TestExecutionAndEnvironment>(new ArrayList<TestExecutionAndEnvironment>());
		}

		List<TestExecutionAndEnvironment> extractList = new ArrayList<>();
		for (Entry<Integer, List<Screenshot>> entry : screenshotListMap.entrySet()) {
			for (Screenshot screenshot : entry.getValue()) {
				TestExecutionAndEnvironment testEE = new TestExecutionAndEnvironment();

				TestExecution testExec = screenshot.getTestExecution();
				testEE.setExecutionId(testExec.getId());
				testEE.setExecutionTime(testExec.getTimeString());

				TestEnvironment testEnv = screenshot.getTestEnvironment();
				testEE.setEnvironmentId(testEnv.getId());
				testEE.setBrowserName(testEnv.getBrowserName());
				testEE.setBrowserVersion(testEnv.getBrowserVersion());
				testEE.setPlatform(testEnv.getPlatform());
				testEE.setPlatformVersion(testEnv.getPlatformVersion());
				testEE.setDeviceName(testEnv.getDeviceName());

				if (!extractList.contains(testEE)) {
					extractList.add(testEE);
				}
			}
		}

		// 実行日時の降順にソート
		List<TestExecutionAndEnvironment> tempTestExecutionAndEnvironmentList = new ArrayList<>();
		for (int i = extractList.size() - 1; i >= 0; i--) {
			TestExecutionAndEnvironment testEE = extractList.get(i);
			tempTestExecutionAndEnvironmentList.add(testEE);
		}
		extractList = tempTestExecutionAndEnvironmentList;

		int size = extractList.size();

		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = Integer.MAX_VALUE;
		}

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<TestExecutionAndEnvironment> resultList = new ArrayList<>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			resultList.add(extractList.get(i));
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<TestExecutionAndEnvironment>(resultList, pageable, size);
	}

	@Override
	public File searchProcessedImageFile(Integer screenshotId, String algorithm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Screenshot> findNotProcessedEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exsitsProcessedImage(Integer screenshotId, String algorithm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEdgeFileName(Integer screenshotId, String algorithm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveProcessedImage(Integer screenshotId, String algorithm, String edgeFileName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<TestExecutionResult> updateExecResult(List<ExecResultInputModel> inputModelList) {
		List<TestExecutionResult> testExecutionResultList = new ArrayList<>();
		for (ExecResultInputModel im : inputModelList) {
			Integer testExecutionId = im.getTestExecutionId();

			// ファイルの更新
			List<TestResult> testResultList = testResultMap.get(testExecutionId);

			List<TestResult> newTestResultList = new ArrayList<>();
			Integer resultCd = im.getResult();
			ExecResult execResult = null;
			for (TestResult orgTestResult : testResultList) {
				// FIXME 外出ししたい箇所
				switch (resultCd) {
					case 0:
						execResult = ExecResult.SUCCESS;
						break;
					case 1:
						execResult = ExecResult.FAILURE;
						break;
					default:
						// FIXME 入力チェックを行い、入らないようにする。
						break;
				}

				// スクリーンショットの結果を変更
				List<ScreenshotResult> newScreenshotResultList = new ArrayList<>();
				for (ScreenshotResult orgScreenshotResult : orgTestResult.getScreenshotResults()) {

					// 対象領域の結果を変更
					List<TargetResult> newTargetResultList = new ArrayList<>();
					for (TargetResult orgTargetResult : orgScreenshotResult.getTargetResults()) {
						TargetResult newTargetResult =
								new TargetResult(execResult, orgTargetResult.getTarget(), orgTargetResult.getExcludes(),
										orgTargetResult.isMoveTarget(), orgTargetResult.getHiddenElementSelectors(),
										orgTargetResult.getImage(), orgTargetResult.getOptions());
						newTargetResultList.add(newTargetResult);
					}

					ScreenshotResult newScreenshotResult =
							new ScreenshotResult(orgScreenshotResult.getScreenshotId(), execResult,
									orgScreenshotResult.getExpectedId(), newTargetResultList,
									orgScreenshotResult.getTestClass(), orgScreenshotResult.getTestMethod(),
									orgScreenshotResult.getCapabilities(), orgScreenshotResult.getEntireScreenshotImage());
					newScreenshotResultList.add(newScreenshotResult);
				}

				// テストクラス全体の結果を変更
				TestResult newTestResult = new TestResult(orgTestResult.getResultId(), execResult, newScreenshotResultList);
				newTestResultList.add(newTestResult);
				PersistMetadata metadata =
						new PersistMetadata(orgTestResult.getResultId(), orgTestResult.getScreenshotResults().get(0).getTestClass());
				saveTestResult(metadata, newTestResult);
			}

			// キャッシュしているデータの更新
			// キャッシュしているテストクラス全体の結果を置換
			testResultMap.put(testExecutionId, newTestResultList);

			TestExecution testExecution = null;
			int totalCount = 0;
			int passedCount = 0;

			List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);
			for (Screenshot s : screenshotList) {
				Boolean comparisonResult = execResult == ExecResult.SUCCESS;
				// screenshotのキャッシュ更新。
				s.setComparisonResult(comparisonResult);

				if (testExecution == null) {
					testExecution = s.getTestExecution();
					testExecution.setExecResult(execResult.name());
				}

				totalCount++;
				if (s.getComparisonResult() == null || s.getComparisonResult().booleanValue()) {
					// 成功時
					passedCount++;
				}

				List<Target> targets = s.getTargets();
				for (Target t : targets) {
					// targetのキャッシュ更新。
					t.setComparisonResult(comparisonResult);
				}
			}

			TestExecutionResult testExecutionResult =
					new TestExecutionResult(testExecution, Long.valueOf(passedCount), Long.valueOf(totalCount));
			testExecutionResultList.add(testExecutionResult);
		}

		return testExecutionResultList;
	}

	@Override
	public List<TestExecutionResult> updateScreenshotComparisonResult(List<ScreenshotResultInputModel> inputModelList) {
		Map<Integer, String> resultMap = new HashMap<>();
		for (ScreenshotResultInputModel im : inputModelList) {
			// キャッシュしているデータの更新
			Integer screenshotId = im.getScreenshotId();
			Screenshot s = screenshotMap.get(screenshotId);

			ExecResult execResult = null;
			// FIXME 外出ししたい箇所
			switch (im.getResult()) {
				case 0:
					execResult = ExecResult.SUCCESS;
					break;
				case 1:
					execResult = ExecResult.FAILURE;
					break;
				default:
					// FIXME 入力チェックを行い、入らないようにする。
					break;
			}

			Boolean comparisonResult = execResult == ExecResult.SUCCESS;
			// screenshotのキャッシュ更新。
			s.setComparisonResult(comparisonResult);

			List<Target> targets = s.getTargets();
			for (Target t : targets) {
				// targetのキャッシュ更新。
				t.setComparisonResult(comparisonResult);
			}

			// 全体結果のキャッシュを更新するか否か判定を行う。
			Integer testExecutionId = s.getTestExecution().getId();
			boolean whetherToUpdate = true;
			List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);
			for (Screenshot screenshot : screenshotList) {
				// 1つでも一致しないものがあった場合は、更新を行わない。
				if (comparisonResult != screenshot.getComparisonResult()) {
					whetherToUpdate = false;
					break;
				}
			}
			if (whetherToUpdate) {
				// 全体結果のキャッシュ更新。
				s.getTestExecution().setExecResult(execResult.name());
			}

			resultMap.put(testExecutionId, s.getTestExecution().getExecResult());
		}

		List<TestExecutionResult> testExecutionResultList = new ArrayList<>();

		for (Entry<Integer, String> entry : resultMap.entrySet()) {
			Integer testExecutionId = entry.getKey();
			// ファイルの更新
			List<TestResult> testResultList = testResultMap.get(testExecutionId);

			List<TestResult> newTestResultList = new ArrayList<>();
			for (TestResult orgTestResult : testResultList) {
				// スクリーンショットの結果を変更
				List<ScreenshotResult> newScreenshotResultList = new ArrayList<>();
				for (ScreenshotResult orgScreenshotResult : orgTestResult.getScreenshotResults()) {
					// スクリーンショットの結果の値を取得
					ExecResult ssExecResult = null;
					Integer screenshotId = screenshotIdMap.get(orgScreenshotResult);
					if (screenshotId != null) {
						Boolean comparisonResult = screenshotMap.get(screenshotId).getComparisonResult();
						if (comparisonResult != null) {
							if (comparisonResult) {
								ssExecResult = ExecResult.SUCCESS;
							} else if (!comparisonResult) {
								ssExecResult = ExecResult.FAILURE;
							}
						}
					}

					// 対象領域の結果を変更
					List<TargetResult> newTargetResultList = new ArrayList<>();
					for (TargetResult orgTargetResult : orgScreenshotResult.getTargetResults()) {
						// スクリーンショットの結果の値がnullの場合は既存の結果の値を使用する。
						ExecResult targetExecResult = ssExecResult != null ? ssExecResult : orgTargetResult.getResult();

						TargetResult newTargetResult =
								new TargetResult(targetExecResult, orgTargetResult.getTarget(), orgTargetResult.getExcludes(),
										orgTargetResult.isMoveTarget(), orgTargetResult.getHiddenElementSelectors(),
										orgTargetResult.getImage(), orgTargetResult.getOptions());
						newTargetResultList.add(newTargetResult);
					}

					// nullの場合は既存の結果の値を使用する。
					ssExecResult = ssExecResult != null ? ssExecResult : orgScreenshotResult.getResult();
					ScreenshotResult newScreenshotResult =
							new ScreenshotResult(orgScreenshotResult.getScreenshotId(), ssExecResult,
									orgScreenshotResult.getExpectedId(), newTargetResultList,
									orgScreenshotResult.getTestClass(), orgScreenshotResult.getTestMethod(),
									orgScreenshotResult.getCapabilities(), orgScreenshotResult.getEntireScreenshotImage());
					newScreenshotResultList.add(newScreenshotResult);
				}

				// テストクラス全体の結果を変更
				ExecResult testExecResult = entry.getValue() != null ? ExecResult.valueOf(entry.getValue()) : null;
				TestResult newTestResult =
						new TestResult(orgTestResult.getResultId(), testExecResult, newScreenshotResultList);
				newTestResultList.add(newTestResult);
				PersistMetadata metadata =
						new PersistMetadata(orgTestResult.getResultId(), orgTestResult.getScreenshotResults().get(0).getTestClass());
				saveTestResult(metadata, newTestResult);
			}

			// 戻りのクラス作成
			TestExecution testExecution = null;
			int totalCount = 0;
			int passedCount = 0;

			List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);
			for (Screenshot s : screenshotList) {
				totalCount++;
				if (s.getComparisonResult() == null || s.getComparisonResult().booleanValue()) {
					// 成功時
					passedCount++;
				}

				if (testExecution == null) {
					testExecution = s.getTestExecution();
				}
			}

			TestExecutionResult testExecutionResult =
					new TestExecutionResult(testExecution, Long.valueOf(passedCount), Long.valueOf(totalCount));
			testExecutionResultList.add(testExecutionResult);
		}
		return testExecutionResultList;
	}

}
