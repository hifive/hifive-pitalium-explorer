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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htmlhifive.testexplorer.model.Capability;
import com.htmlhifive.testexplorer.model.TestCaseResult;
import com.htmlhifive.testexplorer.model.Screenshot;
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

	private static Logger log = LoggerFactory.getLogger(ApiController.class);

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

		TestCaseResult[] testCaseResults;
		try {
			testCaseResults = parseResultFile(root);
		} catch (IOException e) {
			return new ResponseEntity<List<TestExecutionTime>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		List<TestExecutionTime> list = new ArrayList<TestExecutionTime>();
		for (TestCaseResult testCaseResult : testCaseResults) {
			TestExecutionTime execution = new TestExecutionTime(testCaseResult);
			if (!list.contains(execution)) {
				list.add(execution);
			}
		}

		HttpSession session = request.getSession(true);
		Map<String, List<TestCaseResult>> testCaseResutlMap = new HashMap<String, List<TestCaseResult>>();
		session.setAttribute(KEY_RESULT_MAP, testCaseResutlMap);

		for (TestCaseResult testCaseResult : testCaseResults) {
			String executionTime = testCaseResult.getExecuteTime();
			List<TestCaseResult> tempList = testCaseResutlMap.get(executionTime);
			if (tempList == null) {
				tempList = new ArrayList<TestCaseResult>();
				testCaseResutlMap.put(executionTime, tempList);
			}
			tempList.add(testCaseResult);
		}

		Map<String, Screenshot> screenshotMap = new HashMap<String, Screenshot>();
		session.setAttribute(KEY_INDEX_MAP, screenshotMap);

		int index = 0;
		for (TestCaseResult testCaseResult : testCaseResults) {
			for (Screenshot screenshot : testCaseResult.getScreenShots()) {
				screenshotMap.put(String.valueOf(index++), screenshot);
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
		Map<String, List<TestCaseResult>> testCaseResultMap = (Map<String, List<TestCaseResult>>) request.getSession(false)
				.getAttribute(KEY_RESULT_MAP);
		List<TestCaseResult> list = testCaseResultMap.get(executionTime);
		if (list == null) {
			log.error("executionTime(" + executionTime + ") is invalid parameter.");
			return new ResponseEntity<List<TestResultDetail>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		@SuppressWarnings("unchecked")
		Map<String, Screenshot> screenshotMap = (Map<String, Screenshot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		List<TestResultDetail> resultList = makeTestResultList(list, screenshotMap);
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
		Map<String, Screenshot> screenshotMap = (Map<String, Screenshot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		Screenshot screenshot = screenshotMap.get(id);
		if (screenshot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			return new ResponseEntity<TestResultDetail>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		TestResultDetail detail = new TestResultDetail(screenshot);
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
		Map<String, Screenshot> screenshotMap = (Map<String, Screenshot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		Screenshot screenshot = screenshotMap.get(id);
		if (screenshot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			return new ResponseEntity<ExpectedId>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Entry<String, Screenshot> entry = findExpectedScreenshot(screenshot, screenshotMap);
		if (entry == null) {
			if (screenshot.getResult() != null) {
				log.error("find Expected Screenshot.");
			}
			return new ResponseEntity<ExpectedId>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		ExpectedId obj = new ExpectedId();
		obj.setId(entry.getKey());
		return new ResponseEntity<ExpectedId>(obj, HttpStatus.OK);
	}

	private TestCaseResult[] parseResultFile(File root) throws IOException {
		String resultFileName = apiConfig.getProperty(RESULT_FILE_NAME);
		IOFileFilter filter = FileFilterUtils.nameFileFilter(resultFileName);
		Collection<File> collection = FileUtils.listFiles(root, filter, TrueFileFilter.INSTANCE);

		int size = collection.size();
		File[] files = collection.toArray(new File[size]);
		TestCaseResult[] testCaseResults = new TestCaseResult[size];
		for (int i = 0; i < size; i++) {
			try {
				testCaseResults[i] = new ObjectMapper().readValue(files[i], TestCaseResult.class);
				// Unsorted
				Screenshot[] screenshots = testCaseResults[i].getScreenShots();
				for (int j = 0, len = screenshots.length; j < len; j++) {
					Capability capability = screenshots[j].getCapability();
					capability.setScreenshot(screenshots[j]);
					screenshots[j].setTestCaseResult(testCaseResults[i]);
				}
			} catch (IOException e) {
				log.error("Invalid file(" + resultFileName + ").", e);
				throw e;
			}
		}
		return testCaseResults;
	}

	private List<TestResultDetail> makeTestResultList(final List<TestCaseResult> resultFileList, final Map<String, Screenshot> map) {
		List<TestResultDetail> resultList = new ArrayList<TestResultDetail>();
		for (TestCaseResult testCaseResult : resultFileList) {
			String executionTime = testCaseResult.getExecuteTime();
			for (Screenshot screenshot : testCaseResult.getScreenShots()) {
				for (Entry<String, Screenshot> entry : map.entrySet()) {
					Screenshot other = entry.getValue();
					if (screenshot.equals(other) && executionTime.equals(other.getTestCaseResult().getExecuteTime())) {
						TestResultDetail result = new TestResultDetail(screenshot);
						result.setId(entry.getKey());
						resultList.add(result);
						break;
					}
				}
			}
		}
		return resultList;
	}

	private Entry<String, Screenshot> findExpectedScreenshot(Screenshot screenshot, final Map<String, Screenshot> map) {
		if (screenshot.getTestCaseResult().getExpectedId() == null) {
			return null;
		}
		for (Entry<String, Screenshot> entrySet : map.entrySet()) {
			Screenshot other = entrySet.getValue();
			if (!screenshot.getScreenshotId().equals(other.getScreenshotId())) {
				continue;
			}
			if (!screenshot.getTestCaseResult().getExpectedId().equals(other.getTestCaseResult().getExecuteTime())) {
				continue;
			}
			if (screenshot.getCapability().equals(other.getCapability())) {
				return entrySet;
			}
		}
		return null;
	}
}
