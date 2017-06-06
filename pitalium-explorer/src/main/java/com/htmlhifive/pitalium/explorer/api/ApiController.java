/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import com.htmlhifive.pitalium.explorer.changelog.ChangeRecord;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.request.ChangeRequest;
import com.htmlhifive.pitalium.explorer.request.ExecResultChangeRequest;
import com.htmlhifive.pitalium.explorer.request.ScreenshotResultChangeRequest;
import com.htmlhifive.pitalium.explorer.request.TargetResultChangeRequest;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.explorer.service.ExplorerService;

@Scope(scopeName=WebApplicationContext.SCOPE_SESSION)
@Controller
public class ApiController {

	@Autowired
	private ExplorerService service;

	/**
	 * Gets list of the test execution. If pageSize equals to zero, the default page size is used. If pageSize equals to
	 * -1, the entire list is returned.
	 *
	 * @param page Which page to show.
	 * @param pageSize Page size.
	 * @return Page of test execution
	 */
	@RequestMapping(value = "executions/list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Page<TestExecutionResult>> listTestExecution(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "0") int pageSize,
			@RequestParam(value = "resultDirectoryKey", defaultValue = "") String resultDirectoryKey,
			@RequestParam(value = "searchTestMethod", defaultValue = "") String searchTestMethod,
			@RequestParam(value = "searchTestScreen", defaultValue = "") String searchTestScreen) {
		Page<TestExecutionResult> list = service.findTestExecution(searchTestMethod, searchTestScreen, page, pageSize, resultDirectoryKey);
		return new ResponseEntity<Page<TestExecutionResult>>(list, HttpStatus.OK);
	}

	/**
	 * Gets list of the screenshots which is narrowed down by a test execution.
	 *
	 * @param executionId test execution id
	 * @return list of screenshots
	 */
	@RequestMapping(value = "screenshots/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<Screenshot>> searchScreenshots(
			@RequestParam(value = "testExecutionId") Integer testExecutionId,
			@RequestParam(value = "searchTestMethod", defaultValue = "") String searchTestMethod,
			@RequestParam(value = "searchTestScreen", defaultValue = "") String searchTestScreen) {
		List<Screenshot> list = service.findScreenshot(testExecutionId, searchTestMethod, searchTestScreen);
		return new ResponseEntity<List<Screenshot>>(list, HttpStatus.OK);
	}

	/**
	 * Gets the screenshot from screenshot id.
	 *
	 * @param screenshotId screenshot id
	 * @return screenshot
	 */
	@RequestMapping(value = "screenshot", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Screenshot> getScreenshot(@RequestParam(value = "screenshotId") Integer screenshotId) {
		Screenshot item = service.getScreenshot(screenshotId);
		return new ResponseEntity<Screenshot>(item, HttpStatus.OK);
	}

	/**
	 * Gets list of the screenshots which is narrowed down by a test execution id and test environment id. If pageSize
	 * equals to zero, the default page size is used. If pageSize equals to -1, the entire list is returned.
	 *
	 * @param testExecutionId test execution id
	 * @param testEnviromentId test environment id
	 * @param page Which page to show.
	 * @param pageSize Page size.
	 * @return Page of test execution
	 */
	@RequestMapping(value = "screenshots/list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Page<Screenshot>> listScreenshots(
			@RequestParam(value = "testExecutionId") Integer testExecutionId,
			@RequestParam(value = "testEnvironmentId") Integer testEnvironmentId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "-1") int pageSize) {
		Page<Screenshot> screenshotPage = service.findScreenshot(testExecutionId, testEnvironmentId, page, pageSize);
		return new ResponseEntity<Page<Screenshot>>(screenshotPage, HttpStatus.OK);
	}

	/**
	 * Gets listGets list of the TestExecutionAndEnvironment. If pageSize equals to zero, the default page size is used.
	 * If pageSize equals to -1, the entire list is returned. param page Which page to show.
	 *
	 * @param pageSize Page size.
	 * @return Page of test execution and environment
	 */
	@RequestMapping(value = "executions/environments/list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Page<TestExecutionAndEnvironment>> listTestExecutionsWithEnvironment(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "-1") int pageSize) {
		Page<TestExecutionAndEnvironment> testPage = service.findTestExecutionAndEnvironment(page, pageSize);
		return new ResponseEntity<Page<TestExecutionAndEnvironment>>(testPage, HttpStatus.OK);
	}

	/**
	 * テスト全体の実行結果を更新する。
	 *
	 * @param inputModelList 変更内容のリスト
	 * @return 変更記録のリスト
	 */
	@RequestMapping(value = "executions/update", method = RequestMethod.POST, consumes = "application/json;charset=utf-8",
			produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<ChangeRecord>> updateExecResult(@RequestBody List<ExecResultChangeRequest> inputModelList) {
		for (ExecResultChangeRequest request : inputModelList) {
			if (!validateExecResultChangeRequest(request)) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}

		List<ChangeRecord> testExecutionResultList = service.updateExecResult(inputModelList);
		return new ResponseEntity<List<ChangeRecord>>(testExecutionResultList, HttpStatus.OK);
	}

	/**
	 * スクリーンショットの実行結果を更新する。
	 *
	 * @param inputModelList 変更内容のリスト
	 * @return 変更記録のリスト
	 */
	@RequestMapping(value = "screenshots/update", method = RequestMethod.POST, consumes = "application/json;charset=utf-8",
			produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<ChangeRecord>> updateScreenshotComparisonResult(@RequestBody List<ScreenshotResultChangeRequest> inputModelList) {
		for (ScreenshotResultChangeRequest request : inputModelList) {
			if (!validateScreenshotResultChangeRequest(request)) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}

		List<ChangeRecord> testExecutionResultList = service.updateScreenshotComparisonResult(inputModelList);
		return new ResponseEntity<List<ChangeRecord>>(testExecutionResultList, HttpStatus.OK);
	}

	/**
	 * 対象領域の実行結果を更新する。
	 *
	 * @param inputModelList 変更内容のリスト
	 * @return 変更記録のリスト
	 */
	@RequestMapping(value = "targets/update", method = RequestMethod.POST, consumes = "application/json;charset=utf-8",
			produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<ChangeRecord>> updateTargetComparisonResult(@RequestBody List<TargetResultChangeRequest> inputModelList) {
		for (TargetResultChangeRequest request : inputModelList) {
			if (!validateTargetResultChangeRequest(request)) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}

		List<ChangeRecord> screenshotList = service.updateTargetComparisonResult(inputModelList);
		return new ResponseEntity<List<ChangeRecord>>(screenshotList, HttpStatus.OK);
	}

	private boolean validateExecResultChangeRequest(ExecResultChangeRequest request) {
		if (request.getTestExecutionId() == null) {
			return false;
		}
		return validateChangeRequest(request);
	}

	private boolean validateScreenshotResultChangeRequest(ScreenshotResultChangeRequest request) {
		if (request.getScreenshotId() == null) {
			return false;
		}
		return validateChangeRequest(request);
	}

	private boolean validateTargetResultChangeRequest(TargetResultChangeRequest request) {
		if (request.getScreenshotId() == null || request.getTargetId() == null) {
			return false;
		}
		return validateChangeRequest(request);
	}

	private boolean validateChangeRequest(ChangeRequest request) {
		Integer result = request.getResult();
		if (result == null || (result != 0 && result != 1)) {
			return false;
		}
		return true;
	}
}
