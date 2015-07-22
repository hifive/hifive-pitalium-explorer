package com.htmlhifive.testexplorer.io;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.TestEnvironment;
import com.htmlhifive.testexplorer.entity.TestExecution;
import com.htmlhifive.testexplorer.response.TestExecutionResult;
import com.htmlhifive.testlib.core.config.FilePersisterConfig;
import com.htmlhifive.testlib.core.io.FilePersister;
import com.htmlhifive.testlib.core.io.PersistMetadata;
import com.htmlhifive.testlib.core.model.ExecResult;
import com.htmlhifive.testlib.core.model.ScreenshotResult;
import com.htmlhifive.testlib.core.model.TestResult;
import com.htmlhifive.testlib.core.selenium.MrtCapabilities;

public class ExplorerFilePersister extends FilePersister implements ExplorerPersister {

	private static Logger log = LoggerFactory.getLogger(ExplorerFilePersister.class);

	private Map<Integer, TestExecution> testExecutiontMap;
	private Map<Integer, Screenshot> screenshotMap;
	private Map<Integer, List<Screenshot>> screenshotListMap;
	
	public ExplorerFilePersister() {
		// FIXME 独自のConfigに差し替える必要があるかも
		super();
	}

	public ExplorerFilePersister(FilePersisterConfig config) {
		// FIXME 独自のConfigに差し替える必要があるかも
		super(config);
	}

	@Override
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, 
			String searchTestScreen, int page, int pageSize) {
		// TODO:検索には未対応
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new PageImpl<>(new ArrayList<TestExecutionResult>());
		}

		IOFileFilter filter = FileFilterUtils.nameFileFilter(getTestResultFileName());
		Collection<File> collection = 
				FileUtils.listFiles(root, filter, TrueFileFilter.INSTANCE);

		int size = collection.size();
		
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = (int) Math.min(size, Integer.MAX_VALUE);
		}

		// TODO このタイミングで反転しない方が良いかも。
		// 最新から並ぶようにソートする。
		File[] tempFiles = collection.toArray(new File[size]);
		File[] files = new File[size];
		for (int i = 0; i <size; i++) {
			files[size - i - 1] = tempFiles[i];
		}

		testExecutiontMap = new HashMap<>();
		screenshotMap = new HashMap<>();
		screenshotListMap = new HashMap<>();
		// 後続のページング処理用
		List<TestExecution> testExecutionList = new ArrayList<>();
		// Screenshotの関連を貼るための処理用
		Map<String, List<Screenshot>> workScreenshotListMap = new HashMap<>();
		Map<ScreenshotResult, Screenshot> workScreenshotMap = new HashMap<>();
		
		int screenshotId = 0;
		for (int i = 0, len = files.length; i < len; i++) {
			PersistMetadata metadata = new PersistMetadata(
					files[i].getParentFile().getParentFile().getName(), 
					files[i].getParentFile().getName());
			TestResult testResult = super.loadTestResult(metadata);

			TestExecution testExecution = new TestExecution();
			testExecution.setId(Integer.valueOf(i));
			DateTime dateTime = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss")
					.parseDateTime(testResult.getResultId());
			testExecution.setTime(new Timestamp(dateTime.getMillis()));
			testExecutiontMap.put(Integer.valueOf(i), testExecution);
			testExecutionList.add(testExecution);

			List<Screenshot> screenshotList = new ArrayList<>();
			for (ScreenshotResult screenshotResult : testResult.getScreenshotResults()) {
				Screenshot screenshot = new Screenshot();
				screenshot.setId(screenshotId);
				Boolean comparisonResult = null;
				if (screenshotResult.getResult() != null) {
					comparisonResult = 
							screenshotResult.getResult() == ExecResult.SUCCESS ? Boolean.TRUE : Boolean.FALSE;
				}
				screenshot.setComparisonResult(comparisonResult);

				metadata = new PersistMetadata(screenshotResult.getExpectedId(), 
						screenshotResult.getTestClass(), screenshotResult.getTestMethod(), 
						screenshotResult.getScreenshotId(), new MrtCapabilities(screenshotResult.getCapabilities()));
				
				screenshot.setFileName(getScreenshotImageFileName(metadata));
				screenshot.setTestClass(screenshotResult.getTestClass());
				screenshot.setTestExecutionId(Integer.valueOf(i));
				screenshot.setTestMethod(screenshotResult.getTestMethod());
				screenshot.setTestScreen(screenshotResult.getScreenshotId());
				
				Map<String, ?> capabilities = screenshotResult.getCapabilities();
				TestEnvironment testEnvironment = new TestEnvironment();
				testEnvironment.setBrowserName((String)capabilities.get("browserName"));
				testEnvironment.setBrowserVersion((String)capabilities.get("version"));
				testEnvironment.setDeviceName((String)capabilities.get("deviceName"));
				testEnvironment.setId(null);
				testEnvironment.setLabel(null);
				testEnvironment.setPlatform((String)capabilities.get("platform"));
				testEnvironment.setPlatformVersion((String)capabilities.get("platformVersion"));
				screenshot.setTestEnvironment(testEnvironment);

				screenshotMap.put(screenshotId, screenshot);
				screenshotList.add(screenshot);
				screenshotId++;

				workScreenshotMap.put(screenshotResult, screenshot);
			}
			// TestExecution.id をキーとして格納する。
			screenshotListMap.put(Integer.valueOf(i), screenshotList);
			// 実行日時をキーとして格納する。
			workScreenshotListMap.put(files[i].getParentFile().getParentFile().getName(), screenshotList);
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
				if (StringUtils.equals(expectedScreenshot.getTestClass(), screenshot.getTestClass()) &&
						StringUtils.equals(expectedScreenshot.getFileName(), screenshot.getFileName())) {
					screenshot.setExpectedScreenshot(expectedScreenshot);
					break;
				}
			}
		}

		List<TestExecutionResult> resultList = new ArrayList<TestExecutionResult>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			TestExecution execution = testExecutionList.get(i);
			List<Screenshot> list = screenshotListMap.get(execution.getId());

			int passedCount = 0;
			int totalCount = 0;
			for (Screenshot s : list) {
				totalCount++;
				if (s.getComparisonResult() != null && s.getComparisonResult().booleanValue()) {
					passedCount++;
				}
			}
			TestExecutionResult testExecutionResult = new TestExecutionResult(
					execution, Long.valueOf(passedCount), Long.valueOf(totalCount));
			resultList.add(testExecutionResult);

		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<TestExecutionResult>(resultList, pageable, collection.size());
	}

	@Override
	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, 
			String searchTestScreen) {
		return screenshotListMap != null ? 
				screenshotListMap.get(testExecutionId) : new ArrayList<Screenshot>(); 
	}

	@Override
	public Screenshot getScreenshot(Integer screenshotId) {
		return screenshotMap != null ? screenshotMap.get(screenshotId) : null;
	}

	@Override
	public File getImage(Integer id) throws IOException {
		Screenshot screenshot = getScreenshot(id);

		if (screenshot == null) {
			return null;
		}

		TestExecution execution = testExecutiontMap.get(screenshot.getTestExecutionId());
		TestEnvironment env = screenshot.getTestEnvironment();
		Map<String, String> capabilities = new HashMap<>();
		capabilities.put("browserName", env.getBrowserName());
		capabilities.put("version", env.getBrowserVersion());
		capabilities.put("deviceName", env.getDeviceName());
		capabilities.put("platform", env.getPlatform());
		capabilities.put("platformVersion", env.getPlatformVersion());

		PersistMetadata metadata = new PersistMetadata(execution.getTimeString(), 
				screenshot.getTestClass(), screenshot.getTestMethod(), 
				screenshot.getTestScreen(), new MrtCapabilities(capabilities));
		// Send PNG image
		return super.getScreenshotImageFile(metadata);
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

}
