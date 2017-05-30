/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.io;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.htmlhifive.pitalium.common.exception.JSONException;
import com.htmlhifive.pitalium.common.util.JSONUtils;
import com.htmlhifive.pitalium.core.config.FilePersisterConfig;
import com.htmlhifive.pitalium.core.config.PtlTestConfig;
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
import com.htmlhifive.pitalium.explorer.log.ChangePoint;
import com.htmlhifive.pitalium.explorer.log.ChangeRecord;
import com.htmlhifive.pitalium.explorer.log.ScreenshotResultChangePoint;
import com.htmlhifive.pitalium.explorer.log.TargetResultChangePoint;
import com.htmlhifive.pitalium.explorer.request.ExecResultChangeRequest;
import com.htmlhifive.pitalium.explorer.request.ScreenshotResultChangeRequest;
import com.htmlhifive.pitalium.explorer.request.TargetResultChangeRequest;
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
	private Map<Integer, List<TestResult>> testResultMap;		// IDと結果オブジェクト（リスト）を紐付けするためのもの
	private Map<ScreenshotResult, Integer> screenshotIdMap;		// 結果オブジェクトとIDを紐付けするためのもの
	private Map<TargetResult, Integer> targetIdMap;				// 結果オブジェクトとIDを紐付けするためのもの

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
		targetIdMap = new HashMap<>();

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
		// 更新処理用にscreenshotResultとscreenshotIdをマッピングする。
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
		// 更新処理用にtargetResultとtargetIdをマッピングする。
		targetIdMap.put(targetResult, targetId);
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
	public List<ChangeRecord> updateExecResult(List<ExecResultChangeRequest> inputModelList) {

		// result.jsの更新、およびexplorer-change-log.jsonの更新
		Date updateTime = new Date();
		// 返却用の変更記録リスト
		List<ChangeRecord> changeRecordList = new ArrayList<>();

		for (ExecResultChangeRequest im : inputModelList) {
			ExecResult execResult = convertToExecResult(im.getResult());

			// ファイルの更新
			Integer testExecutionId = im.getTestExecutionId();
			List<TestResult> testResultList = testResultMap.get(testExecutionId);
			// 変更記録ファイルの格納先に使用する。
			String resultId = testResultList.get(0).getResultId();

			// 変更履歴ファイルの読み込み。
			List<ChangeRecord> fileOutputList = loadChangeLog(resultId);

			// 変更記録の作成。
			ChangeRecord changeRecord = createChangeRecord(fileOutputList.size() + 1, im, resultId, updateTime);
			fileOutputList.add(changeRecord);
			// 返却用の変更記録リストにも追加。
			changeRecordList.add(changeRecord);

			// 変更箇所
			ChangePoint point = new ChangePoint();
			// スクリーンショット変更箇所格納用
			List<ScreenshotResultChangePoint> screenshotResults = new ArrayList<>();
			point.setScreenshotResults(screenshotResults);
			// ターゲット変更箇所格納用
			List<TargetResultChangePoint> targetResults = new ArrayList<>();
			point.setTargetResults(targetResults);
			changeRecord.setChangePoints(point);

			// テストクラス全体の実行結果格納用
			List<TestResult> newTestResultList = new ArrayList<>();
			for (TestResult orgTestResult : testResultList) {
				// 結果が一致しないものについては、変更個所として格納
				if (orgTestResult.getResult() != execResult) {
					point.setExecResult(Boolean.TRUE);
				}

				// スクリーンショットの実行結果格納用
				List<ScreenshotResult> newScreenshotResultList = new ArrayList<>();
				for (ScreenshotResult orgScreenshotResult : orgTestResult.getScreenshotResults()) {
					ScreenshotResultChangePoint screenshotResultChangePoint =
							createScreenshotResultChangePoint(orgScreenshotResult);
					// 結果が一致しないものについては、変更個所として格納
					if (orgScreenshotResult.getResult() != execResult) {
						screenshotResults.add(screenshotResultChangePoint);
					}

					// 対象領域の実行結果格納用
					List<TargetResult> newTargetResultList = new ArrayList<>();
					for (TargetResult orgTargetResult : orgScreenshotResult.getTargetResults()) {
						// 結果が一致しないものについては、変更個所として格納
						if (orgTargetResult.getResult() != execResult) {
							targetResults.add(creatTargetResultChangePoint(orgTargetResult, screenshotResultChangePoint));
						}

						// メモリにキャッシュしている対象領域の結果を置換
						TargetResult newTargetResult = createTargetResult(orgTargetResult, execResult);
						Integer targetId = targetIdMap.get(orgTargetResult);
						targetIdMap.remove(orgTargetResult);
						targetIdMap.put(newTargetResult, targetId);

						newTargetResultList.add(newTargetResult);
					}

					// メモリにキャッシュしているスクリーンショットの結果を置換
					ScreenshotResult newScreenshotResult =
							createScreenshotResult(orgScreenshotResult, execResult, newTargetResultList);
					Integer screenshotId = screenshotIdMap.get(orgScreenshotResult);
					screenshotIdMap.remove(orgScreenshotResult);
					screenshotIdMap.put(newScreenshotResult, screenshotId);

					newScreenshotResultList.add(newScreenshotResult);
				}

				// result.jsの更新
				TestResult newTestResult = createTestResult(orgTestResult, execResult, newScreenshotResultList);
				newTestResultList.add(newTestResult);
				PersistMetadata metadata = createPersistMetadata(orgTestResult);
				saveTestResult(metadata, newTestResult);
			}
			// 変更履歴をファイルに出力。
			saveChangelog(resultId, fileOutputList);

			// メモリにキャッシュしているテストクラス全体の結果を置換
			testResultMap.put(testExecutionId, newTestResultList);

			// メモリにキャッシュしているテスト実行、スクリーンショット、対象領域を置換
			List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);
			for (Screenshot s : screenshotList) {
				Boolean comparisonResult = execResult == ExecResult.SUCCESS;
				// screenshotのキャッシュ更新。
				s.setComparisonResult(comparisonResult);

				// targetのキャッシュ更新。
				List<Target> targets = s.getTargets();
				for (Target t : targets) {
					t.setComparisonResult(comparisonResult);
				}

				// testExecutionのキャッシュ更新。
				s.getTestExecution().setExecResult(execResult.name());
			}
		}

		return changeRecordList;
	}

	@Override
	public List<ChangeRecord> updateScreenshotComparisonResult(List<ScreenshotResultChangeRequest> inputModelList) {
		// TestExecutionIdとExecResultを紐付けするためのもの
		Map<Integer, ExecResult> execResultMap = new HashMap<>();
		// キャッシュしている情報の更新
		for (ScreenshotResultChangeRequest im : inputModelList) {
			ExecResult execResult = convertToExecResult(im.getResult());
			Boolean comparisonResult = execResult == ExecResult.SUCCESS;

			// screenshotのキャッシュ更新。
			Integer screenshotId = im.getScreenshotId();
			Screenshot s = screenshotMap.get(screenshotId);
			s.setComparisonResult(comparisonResult);

			// targetのキャッシュ更新。
			List<Target> targets = s.getTargets();
			for (Target t : targets) {
				t.setComparisonResult(comparisonResult);
			}

			// testExecutionのキャッシュ更新。
			Integer testExecutionId = s.getTestExecution().getId();
			boolean matchAll = true;
			List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);
			for (Screenshot screenshot : screenshotList) {
				// 1つでも一致しないものがあった場合は、FAILUREにする。
				if (comparisonResult != screenshot.getComparisonResult()) {
					matchAll = false;
					break;
				}
			}
			if (matchAll) {
				s.getTestExecution().setExecResult(execResult.name());
			} else {
				s.getTestExecution().setExecResult(ExecResult.FAILURE.name());
			}

			// 履歴ファイルに書き込む際に使用する情報を確保。
			ExecResult execAllResult =
					s.getTestExecution().getExecResult() != null ? ExecResult.valueOf(s.getTestExecution().getExecResult()) : null;
			execResultMap.put(testExecutionId, execAllResult);
		}

		// explorer-change-log.jsonの更新
		Date updateTime = new Date();
		// 返却用の変更記録リスト
		List<ChangeRecord> changeRecordList = new ArrayList<>();

		for (ScreenshotResultChangeRequest im : inputModelList) {
			ExecResult execResult = convertToExecResult(im.getResult());

			Screenshot screenshot = screenshotMap.get(im.getScreenshotId());
			TestExecution testExecution = screenshot.getTestExecution();
			Integer testExecutionId = testExecution.getId();
			String resultId = testExecution.getTimeString();

			// 変更履歴ファイルの読み込み。
			List<ChangeRecord> fileOutputList = loadChangeLog(resultId);

			// 変更記録の作成。
			ChangeRecord changeRecord = createChangeRecord(fileOutputList.size() + 1, im, resultId, updateTime);
			fileOutputList.add(changeRecord);
			// 返却用の変更記録リストにも追加。
			changeRecordList.add(changeRecord);

			// 変更箇所
			ChangePoint point = new ChangePoint();
			// スクリーンショット変更箇所格納用
			List<ScreenshotResultChangePoint> screenshotResults = new ArrayList<>();
			point.setScreenshotResults(screenshotResults);
			// ターゲット変更箇所格納用
			List<TargetResultChangePoint> targetResults = new ArrayList<>();
			point.setTargetResults(targetResults);
			changeRecord.setChangePoints(point);

			List<TestResult> testResultList = testResultMap.get(testExecutionId);

			for (TestResult testResult : testResultList) {
				// テストクラス名が一致しないものは変更記録の対象外とする。
				if (!screenshot.getTestClass().equals(testResult.getScreenshotResults().get(0).getTestClass())) {
					continue;
				}

				// 結果が一致しないものについては、変更個所として格納
				if (testResult.getResult() != execResultMap.get(testExecutionId)) {
					point.setExecResult(Boolean.TRUE);
				}

				for (ScreenshotResult screenshotResult : testResult.getScreenshotResults()) {
					// 各パラメータが一致しないものは変更記録の対象外とする。
					if (!isMatchedScreenshot(screenshot, screenshotResult)) {
						continue;
					}

					ScreenshotResultChangePoint screenshotResultChangePoint =
							createScreenshotResultChangePoint(screenshotResult);
					// 結果が一致しないものについては、変更個所として格納
					if (screenshotResult.getResult() != execResult) {
						screenshotResults.add(screenshotResultChangePoint);
					}

					// 対象領域の結果を変更
					for (TargetResult targetResult : screenshotResult.getTargetResults()) {
						// 結果が一致しないものについては、変更個所として格納
						if (targetResult.getResult() != execResult) {
							targetResults.add(creatTargetResultChangePoint(targetResult, screenshotResultChangePoint));
						}
					}
				}
			}
			// 変更履歴をファイルに出力。
			saveChangelog(resultId, fileOutputList);
		}

		// result.jsの更新の更新
		for (Entry<Integer, ExecResult> entry : execResultMap.entrySet()) {
			// ファイルの更新
			Integer testExecutionId = entry.getKey();
			List<TestResult> testResultList = testResultMap.get(testExecutionId);

			// テストクラス全体の実行結果格納用
			List<TestResult> newTestResultList = new ArrayList<>();
			for (TestResult orgTestResult : testResultList) {
				// スクリーンショットの実行結果格納用
				List<ScreenshotResult> newScreenshotResultList = new ArrayList<>();
				for (ScreenshotResult orgScreenshotResult : orgTestResult.getScreenshotResults()) {
					Integer screenshotId = screenshotIdMap.get(orgScreenshotResult);

					// スクリーンショットの結果の値を取得
					Boolean comparisonResult = screenshotMap.get(screenshotId).getComparisonResult();
					ExecResult ssExecResult  = convertToExecResult(comparisonResult);

					// 対象領域の実行結果格納用
					List<TargetResult> newTargetResultList = new ArrayList<>();
					for (TargetResult orgTargetResult : orgScreenshotResult.getTargetResults()) {
						// スクリーンショットの結果の値がnullの場合は既存の結果の値を使用する。
						ExecResult targetExecResult = ssExecResult != null ? ssExecResult : orgTargetResult.getResult();

						// メモリにキャッシュしている対象領域の結果を置換
						TargetResult newTargetResult = createTargetResult(orgTargetResult, targetExecResult);
						Integer targetId = targetIdMap.get(orgTargetResult);
						targetIdMap.remove(orgTargetResult);
						targetIdMap.put(newTargetResult, targetId);

						newTargetResultList.add(newTargetResult);
					}

					// nullの場合は既存の結果の値を使用する。
					ssExecResult = ssExecResult != null ? ssExecResult : orgScreenshotResult.getResult();
					// メモリにキャッシュしているスクリーンショットの結果を置換
					ScreenshotResult newScreenshotResult =
							createScreenshotResult(orgScreenshotResult, ssExecResult, newTargetResultList);
					screenshotIdMap.remove(orgScreenshotResult);
					screenshotIdMap.put(newScreenshotResult, screenshotId);

					newScreenshotResultList.add(newScreenshotResult);
				}

				// result.jsの更新
				TestResult newTestResult = createTestResult(orgTestResult, entry.getValue(), newScreenshotResultList);
				newTestResultList.add(newTestResult);
				PersistMetadata metadata = createPersistMetadata(orgTestResult);
				saveTestResult(metadata, newTestResult);
			}

			// メモリにキャッシュしているテストクラス全体の結果を置換
			testResultMap.put(testExecutionId, newTestResultList);
		}
		return changeRecordList;
	}

	@Override
	public List<ChangeRecord> updateTargetComparisonResult(List<TargetResultChangeRequest> inputModelList) {
		// TestExecutionIdとExecResultを紐付けするためのもの
		Map<Integer, ExecResult> execResultMap = new HashMap<>();
		// キャッシュしている情報の更新
		for (TargetResultChangeRequest im : inputModelList) {
			// キャッシュしているデータの更新
			ExecResult execResult = convertToExecResult(im.getResult());
			Boolean comparisonResult = execResult == ExecResult.SUCCESS;

			// targetのキャッシュ更新。
			Integer targetId = im.getTargetId();
			Target t = targetMap.get(targetId);
			t.setComparisonResult(comparisonResult);

			Integer screenshotId = im.getScreenshotId();
			boolean matchScreenshot = true;
			Screenshot s = screenshotMap.get(screenshotId);

			// スクリーンショットのキャッシュを更新する。
			List<Target> targetList = s.getTargets();
			for (Target target : targetList) {
				// 1つでも一致しないものがあった場合は、falseにする。
				if (comparisonResult != target.getComparisonResult()) {
					matchScreenshot = false;
					break;
				}
			}
			if (matchScreenshot) {
				s.setComparisonResult(comparisonResult);
			} else {
				s.setComparisonResult(false);
			}

			// 全体結果のキャッシュを更新する。
			Integer testExecutionId = s.getTestExecution().getId();
			boolean matchAll = true;
			List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);
			for (Screenshot screenshot : screenshotList) {
				// 1つでも一致しないものがあった場合は、FAILUREにする。
				if (comparisonResult != screenshot.getComparisonResult()) {
					matchAll = false;
					break;
				}
			}
			if (matchAll) {
				s.getTestExecution().setExecResult(execResult.name());
			} else {
				s.getTestExecution().setExecResult(ExecResult.FAILURE.name());
			}

			// ファイルに書き込む際に使用する情報を確保。
			ExecResult execAllResult =
					s.getTestExecution().getExecResult() != null ? ExecResult.valueOf(s.getTestExecution().getExecResult()) : null;
			execResultMap.put(testExecutionId, execAllResult);
		}

		// explorer-change-log.jsonの更新
		Date updateTime = new Date();
		// 返却用の変更記録リスト
		List<ChangeRecord> changeRecordList = new ArrayList<>();

		for (TargetResultChangeRequest im : inputModelList) {
			ExecResult execResult = convertToExecResult(im.getResult());

			Target target = targetMap.get(im.getTargetId());
			Screenshot screenshot = screenshotMap.get(im.getScreenshotId());
			TestExecution testExecution = screenshot.getTestExecution();
			Integer testExecutionId = testExecution.getId();
			String resultId = testExecution.getTimeString();

			// 変更履歴ファイルの読み込み。
			List<ChangeRecord> fileOutputList = loadChangeLog(resultId);

			// 変更記録の作成。
			ChangeRecord changeRecord = createChangeRecord(fileOutputList.size() + 1, im, resultId, updateTime);
			fileOutputList.add(changeRecord);
			// 返却用の変更記録リストにも追加。
			changeRecordList.add(changeRecord);

			// 変更箇所
			ChangePoint point = new ChangePoint();
			// スクリーンショット変更箇所格納用
			List<ScreenshotResultChangePoint> screenshotResults = new ArrayList<>();
			point.setScreenshotResults(screenshotResults);
			// ターゲット変更箇所格納用
			List<TargetResultChangePoint> targetResults = new ArrayList<>();
			point.setTargetResults(targetResults);
			changeRecord.setChangePoints(point);

			List<TestResult> testResultList = testResultMap.get(testExecutionId);

			for (TestResult testResult : testResultList) {
				// テストクラス名が一致しないものは変更記録の対象外とする。
				if (!screenshot.getTestClass().equals(testResult.getScreenshotResults().get(0).getTestClass())) {
					continue;
				}

				// 結果が一致しないものについては、変更個所として格納
				if (testResult.getResult() != execResultMap.get(testExecutionId)) {
					point.setExecResult(Boolean.TRUE);
				}

				for (ScreenshotResult screenshotResult : testResult.getScreenshotResults()) {
					// 各パラメータが一致しないものは変更記録の対象外とする。
					if (!isMatchedScreenshot(screenshot, screenshotResult)) {
						continue;
					}

					ScreenshotResultChangePoint screenshotResultChangePoint =
							createScreenshotResultChangePoint(screenshotResult);
					// 結果が一致しないものについては、変更個所として格納
					if (screenshotResult.getResult() != convertToExecResult(screenshot.getComparisonResult())) {
						screenshotResults.add(screenshotResultChangePoint);
					}

					// 対象領域の結果を変更
					for (TargetResult targetResult : screenshotResult.getTargetResults()) {
						// 各パラメータが一致しないものは変更記録の対象外とする。
						if (!isMatchedTarget(target, targetResult)) {
							continue;
						}

						// 結果が一致しないものについては、変更個所として格納
						if (targetResult.getResult() != execResult) {
							targetResults.add(creatTargetResultChangePoint(targetResult, screenshotResultChangePoint));
						}
					}
				}
			}
			// 変更履歴をファイルに出力。
			saveChangelog(resultId, fileOutputList);
		}

		// result.jsの更新の更新
		for (Entry<Integer, ExecResult> entry : execResultMap.entrySet()) {
			Integer testExecutionId = entry.getKey();
			List<TestResult> testResultList = testResultMap.get(testExecutionId);

			// テストクラス全体の実行結果格納用
			List<TestResult> newTestResultList = new ArrayList<>();
			for (TestResult orgTestResult : testResultList) {
				// スクリーンショットの実行結果格納用
				List<ScreenshotResult> newScreenshotResultList = new ArrayList<>();
				for (ScreenshotResult orgScreenshotResult : orgTestResult.getScreenshotResults()) {
					// 対象領域の実行結果格納用
					List<TargetResult> newTargetResultList = new ArrayList<>();
					for (TargetResult orgTargetResult : orgScreenshotResult.getTargetResults()) {
						// 対象領域の結果の値を取得
						Integer targetId = targetIdMap.get(orgTargetResult);
						Boolean comparisonResult = targetMap.get(targetId).getComparisonResult();
						ExecResult targetExecResult = convertToExecResult(comparisonResult);

						// メモリにキャッシュしている対象領域の結果を置換
						TargetResult newTargetResult = createTargetResult(orgTargetResult, targetExecResult);
						targetIdMap.remove(orgTargetResult);
						targetIdMap.put(newTargetResult, targetId);

						newTargetResultList.add(newTargetResult);
					}

					// スクリーンショットの結果の値を取得
					Integer screenshotId = screenshotIdMap.get(orgScreenshotResult);
					Boolean comparisonResult = screenshotMap.get(screenshotId).getComparisonResult();
					ExecResult ssExecResult  = convertToExecResult(comparisonResult);

					// nullの場合は既存の結果の値を使用する。
					ssExecResult = ssExecResult != null ? ssExecResult : orgScreenshotResult.getResult();
					// メモリにキャッシュしているスクリーンショットの結果を置換
					ScreenshotResult newScreenshotResult =
							createScreenshotResult(orgScreenshotResult, ssExecResult, newTargetResultList);
					screenshotIdMap.remove(orgScreenshotResult);
					screenshotIdMap.put(newScreenshotResult, screenshotId);

					newScreenshotResultList.add(newScreenshotResult);
				}

				// result.jsの更新
				TestResult newTestResult = createTestResult(orgTestResult, entry.getValue(), newScreenshotResultList);
				newTestResultList.add(newTestResult);
				PersistMetadata metadata = createPersistMetadata(orgTestResult);
				saveTestResult(metadata, newTestResult);
			}

			// メモリにキャッシュしているテストクラス全体の結果を置換
			testResultMap.put(testExecutionId, newTestResultList);
		}

		return changeRecordList;
	}

	private ExecResult convertToExecResult(Integer resultCd) {
		switch (resultCd) {
			case 0:
				return ExecResult.SUCCESS;
			case 1:
				return ExecResult.FAILURE;
			default:
				return null;
		}
	}

	private ExecResult convertToExecResult(Boolean result) {
		if (result == null) {
			return null;
		}
		if (result) {
			return ExecResult.SUCCESS;
		} else {
			return ExecResult.FAILURE;
		}
	}

	private TargetResult createTargetResult(TargetResult original, ExecResult result) {
		return new TargetResult( result, original.getTarget(), original.getExcludes(), original.isMoveTarget(),
				original.getHiddenElementSelectors(), original.getImage(), original.getOptions());
	}

	private ScreenshotResult createScreenshotResult(ScreenshotResult original, ExecResult result, List<TargetResult> targetResultList) {
		return new ScreenshotResult(original.getScreenshotId(), result, original.getExpectedId(), targetResultList,
				original.getTestClass(), original.getTestMethod(), original.getCapabilities(), original.getEntireScreenshotImage());
	}

	private TestResult createTestResult(TestResult original, ExecResult result, List<ScreenshotResult> screenshotResultList) {
		return new TestResult(original.getResultId(), result, screenshotResultList);
	}

	private PersistMetadata createPersistMetadata(TestResult result) {
		return new PersistMetadata(result.getResultId(), result.getScreenshotResults().get(0).getTestClass());
	}

	private ChangeRecord createChangeRecord(int index, ExecResultChangeRequest request, String resultId, Date updateTime) {
		Map<String, Object> requestParams = new TreeMap<>();	// キーを昇順の並びで出力するためにTreeMapを使用している。
		requestParams.put("testExecutionId", request.getTestExecutionId());
		requestParams.put("execResult", convertToExecResult(request.getResult()));
		return createChangeRecord(index, requestParams, request.getComment(), resultId, updateTime);
	}

	private ChangeRecord createChangeRecord(int index, ScreenshotResultChangeRequest request, String resultId, Date updateTime) {
		Map<String, Object> requestParams = new TreeMap<>();	// キーを昇順の並びで出力するためにTreeMapを使用している。
		requestParams.put("screenshotId", request.getScreenshotId());
		requestParams.put("execResult", convertToExecResult(request.getResult()));
		return createChangeRecord(index, requestParams, request.getComment(), resultId, updateTime);
	}

	private ChangeRecord createChangeRecord(int index, TargetResultChangeRequest request, String resultId, Date updateTime) {
		Map<String, Object> requestParams = new TreeMap<>();	// キーを昇順の並びで出力するためにTreeMapを使用している。
		requestParams.put("screenshotId", request.getScreenshotId());
		requestParams.put("targetId", request.getTargetId());
		requestParams.put("execResult", convertToExecResult(request.getResult()));
		return createChangeRecord(index, requestParams, request.getComment(), resultId, updateTime);
	}

	private ChangeRecord createChangeRecord(int index, Map<String, Object> requestParams, String comment, String resultId,
			Date updateTime) {
		return new ChangeRecord(index, requestParams, comment, resultId, updateTime);
	}

	private TargetResultChangePoint creatTargetResultChangePoint(TargetResult targetResult,
			ScreenshotResultChangePoint screenshotResultChangePoint) {
		return new TargetResultChangePoint(screenshotResultChangePoint, targetResult.getTarget().getSelector());
	}

	private ScreenshotResultChangePoint createScreenshotResultChangePoint(ScreenshotResult screenshotResult) {
		return new ScreenshotResultChangePoint(screenshotResult.getScreenshotId(),
				screenshotResult.getTestClass(), screenshotResult.getTestMethod(), screenshotResult.getCapabilities());
	}

	private boolean isMatchedScreenshot(Screenshot screenshot, ScreenshotResult screenshotResult) {
		TestEnvironment testEnvironment = screenshot.getTestEnvironment();
		Map<String, ?> capabilities = screenshotResult.getCapabilities();
		// 各パラメータが一致しないものは変更記録の対象外とする。
		if (!screenshot.getTestClass().equals(screenshotResult.getTestClass())
				|| !screenshot.getTestMethod().equals(screenshotResult.getTestMethod())
				|| !screenshot.getTestScreen().equals(screenshotResult.getScreenshotId())
				|| !testEnvironment.getPlatform().equals(capabilities.get("platform"))
				|| !testEnvironment.getBrowserName().equals(capabilities.get("browserName"))){
			return false;
		}
		// バージョンについては、nullが入ることがあるため判定処理を別にしている。
		String version = (String)capabilities.get("version");
		if (testEnvironment.getBrowserVersion() == null && version == null) {
			return true;
		} else {
			if (testEnvironment.getBrowserVersion() != null && version != null
					&& testEnvironment.getBrowserVersion().equals(version)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean isMatchedTarget(Target target, TargetResult targetResult) {
		Area area = target.getArea();
		IndexDomSelector selector = targetResult.getTarget().getSelector();
		// 各パラメータが一致しないものは変更記録の対象外とする。
		if (!area.getSelectorType().equals(selector.getType().name())
				|| !area.getSelectorValue().equals(selector.getValue())
				|| area.getSelectorIndex() != selector.getIndex()) {
			return false;
		}
		return true;
	}

	private List<ChangeRecord> loadChangeLog(String resultId) {
		List<ChangeRecord> changeRecordList = null;
		File dir = new File(PtlTestConfig.getInstance().getPersisterConfig().getFile().getResultDirectory(), resultId);
		File file = new File(dir, "explorer-change-log.json");
		if (!file.exists()) {
			changeRecordList = new ArrayList<>();
			return changeRecordList;
		}

		if (log.isDebugEnabled()) {
			log.debug("[Load Changelog] ({})", file);
		}
		try {
			changeRecordList = JSONUtils.readValue(file, new TypeReference<List<ChangeRecord>>() {
			});
		} catch (JSONException e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to load Changelog.", e);
			}
			throw e;
		}
		return changeRecordList;
	}

	private void saveChangelog(String resultId, List<ChangeRecord> changeRecordList) {
		File dir = new File(PtlTestConfig.getInstance().getPersisterConfig().getFile().getResultDirectory(), resultId);
		File file = new File(dir, "explorer-change-log.json");
		if (log.isDebugEnabled()) {
			log.debug("[Save Changelog] ({})", file);
		}
		try {
			JSONUtils.writeValueWithIndent(file, changeRecordList);
		} catch (JSONException e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to save Changelog.", e);
			}
			throw e;
		}
	}
}
