/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.htmlhifive.testexplorer.model.Capability;
import com.htmlhifive.testexplorer.model.ResultFile;
import com.htmlhifive.testexplorer.model.ScreenShot;
import com.htmlhifive.testexplorer.response.ExpectedId;
import com.htmlhifive.testexplorer.response.TestExecutionTime;
import com.htmlhifive.testexplorer.response.TestResultDetail;

@Controller
@RequestMapping("/api")
public class ApiController {
	// PropertyKey
	private static final String RESULTS_DIR = "resultsDir";
	private static final String RESULT_FILE_NAME = "resultFileName";

	// SessionKey
	private static final String KEY_RESULT_MAP = "RESULT_MAP";
	private static final String KEY_INDEX_MAP = "INDEX_MAP";

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private Properties apiConfig;

	private static Log log = LogFactory.getLog(ApiController.class);

	/**
	 * Gets list of the test execution date and time. Parses the Result file and create the object list. Also, store the
	 * object list in the session. If you do not call this method, the subsequent processing does not work.
	 *
	 * @return list of test execution date and time
	 */
	@RequestMapping(value = "/listTestExectionTime", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestExecutionTime>> listTestExectionTime() {
		File root = new File(apiConfig.getProperty(RESULTS_DIR));
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new ResponseEntity<List<TestExecutionTime>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		ResultFile[] resultFiles;
		try {
			resultFiles = parseResultFile(root);
		} catch (IOException e) {
			return new ResponseEntity<List<TestExecutionTime>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		List<TestExecutionTime> list = new ArrayList<TestExecutionTime>();
		for (ResultFile resultFile : resultFiles) {
			TestExecutionTime execution = new TestExecutionTime(resultFile);
			if (!list.contains(execution)) {
				list.add(execution);
			}
		}

		HttpSession session = request.getSession(true);
		Map<String, List<ResultFile>> resultFileMap = new HashMap<String, List<ResultFile>>();
		session.setAttribute(KEY_RESULT_MAP, resultFileMap);

		for (ResultFile resultFile : resultFiles) {
			String executionTime = resultFile.getExecuteTime();
			List<ResultFile> tempList = resultFileMap.get(executionTime);
			if (tempList == null) {
				tempList = new ArrayList<ResultFile>();
				resultFileMap.put(executionTime, tempList);
			}
			tempList.add(resultFile);
		}

		Map<String, ScreenShot> screenShotMap = new HashMap<String, ScreenShot>();
		session.setAttribute(KEY_INDEX_MAP, screenShotMap);

		int index = 0;
		for (ResultFile resultFile : resultFiles) {
			for (ScreenShot screenShot : resultFile.getScreenShots()) {
				screenShotMap.put(String.valueOf(index++), screenShot);
			}
		}

		return new ResponseEntity<List<TestExecutionTime>>(list, HttpStatus.OK);
	}

	/**
	 * Gets list of the test execution date and time which is narrowed down by parameters.
	 *
	 * @param criteria parameter to narrow
	 * @return list of test execution date and time
	 */
	@RequestMapping(value = "/listTestExectionTime/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestExecutionTime>> search(@RequestParam String criteria) {
		// Not implemented.
		return listTestExectionTime();
	}

	/**
	 * Gets list of the test execution result which is narrowed down by test execution time.
	 *
	 * @param executionTime test execution time
	 * @return list of test execution result
	 */
	@RequestMapping(value = "/listTestResult", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestResultDetail>> listTestResult(@RequestParam String executionTime) {
		@SuppressWarnings("unchecked")
		Map<String, List<ResultFile>> resultFileMap = (Map<String, List<ResultFile>>) request.getSession(false)
				.getAttribute(KEY_RESULT_MAP);
		List<ResultFile> list = resultFileMap.get(executionTime);
		if (list == null) {
			log.error("executionTime(" + executionTime + ") is invalid parameter.");
			return new ResponseEntity<List<TestResultDetail>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		@SuppressWarnings("unchecked")
		Map<String, ScreenShot> screenShotMap = (Map<String, ScreenShot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		List<TestResultDetail> resultList = makeTestResultList(list, screenShotMap);
		return new ResponseEntity<List<TestResultDetail>>(resultList, HttpStatus.OK);
	}

	/**
	 * Gets the test execution detail from test execution result id.
	 *
	 * @param id id test execution result id
	 * @return list of test execution result
	 */
	@RequestMapping(value = "/getDetail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<TestResultDetail> getDetail(@RequestParam String id) {
		@SuppressWarnings("unchecked")
		Map<String, ScreenShot> screenShotMap = (Map<String, ScreenShot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		ScreenShot screenShot = screenShotMap.get(id);
		if (screenShot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			return new ResponseEntity<TestResultDetail>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		TestResultDetail detail = new TestResultDetail(screenShot);
		detail.setId(id);
		return new ResponseEntity<TestResultDetail>(detail, HttpStatus.OK);
	}

	/**
	 * Gets the right image id from test execution result id.
	 *
	 * @param id test execution result id
	 * @return right image id
	 */
	@RequestMapping(value = "/getExpectedId", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<ExpectedId> getExpectedId(@RequestParam String id) {
		@SuppressWarnings("unchecked")
		Map<String, ScreenShot> screenShotMap = (Map<String, ScreenShot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		ScreenShot screenShot = screenShotMap.get(id);
		if (screenShot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			return new ResponseEntity<ExpectedId>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Entry<String, ScreenShot> entry = findExpectedScreenshot(screenShot, screenShotMap);
		if (entry == null) {
			if (screenShot.getResult() != null) {
				log.error("find Expected ScreenShot.");
			}
			return new ResponseEntity<ExpectedId>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		ExpectedId obj = new ExpectedId();
		obj.setId(entry.getKey());
		return new ResponseEntity<ExpectedId>(obj, HttpStatus.OK);
	}

	private ResultFile[] parseResultFile(File root) throws IOException {
		String resultFileName = apiConfig.getProperty(RESULT_FILE_NAME);
		IOFileFilter filter = FileFilterUtils.nameFileFilter(resultFileName);
		Collection<File> collection = FileUtils.listFiles(root, filter, TrueFileFilter.INSTANCE);

		int size = collection.size();
		File[] files = collection.toArray(new File[size]);
		ResultFile[] resultFiles = new ResultFile[size];
		for (int i = 0; i < size; i++) {
			try {
				resultFiles[i] = new ObjectMapper().readValue(files[i], ResultFile.class);
				// Unsorted
				ScreenShot[] screenShots = resultFiles[i].getScreenShots();
				for (int j = 0, len = screenShots.length; j < len; j++) {
					Capability capability = screenShots[j].getCapability();
					capability.setScreenShot(screenShots[j]);
					screenShots[j].setResultFile(resultFiles[i]);
				}
			} catch (IOException e) {
				log.error("Invalid file(" + resultFileName + ").", e);
				throw e;
			}
		}
		return resultFiles;
	}

	private List<TestResultDetail> makeTestResultList(final List<ResultFile> resultFileList, final Map<String, ScreenShot> map) {
		List<TestResultDetail> resultList = new ArrayList<TestResultDetail>();
		for (ResultFile resultFile : resultFileList) {
			String executionTime = resultFile.getExecuteTime();
			for (ScreenShot screenShot : resultFile.getScreenShots()) {
				for (Entry<String, ScreenShot> entry : map.entrySet()) {
					ScreenShot other = entry.getValue();
					if (screenShot.equals(other) && executionTime.equals(other.getResultFile().getExecuteTime())) {
						TestResultDetail result = new TestResultDetail(screenShot);
						result.setId(entry.getKey());
						resultList.add(result);
						break;
					}
				}
			}
		}
		return resultList;
	}

	private Entry<String, ScreenShot> findExpectedScreenshot(ScreenShot screenShot, final Map<String, ScreenShot> map) {
		if (screenShot.getResultFile().getExpectedId() == null) {
			return null;
		}
		for (Entry<String, ScreenShot> entrySet : map.entrySet()) {
			ScreenShot other = entrySet.getValue();
			if (!screenShot.getScreenshotId().equals(other.getScreenshotId())) {
				continue;
			}
			if (!screenShot.getResultFile().getExpectedId().equals(other.getResultFile().getExecuteTime())) {
				continue;
			}
			if (screenShot.getCapability().equals(other.getCapability())) {
				return entrySet;
			}
		}
		return null;
	}
}
