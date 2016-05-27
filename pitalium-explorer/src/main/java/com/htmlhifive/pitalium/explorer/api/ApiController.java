/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.image.ComparedRectangle;
import com.htmlhifive.pitalium.explorer.io.ExplorerFilePersister;
import com.htmlhifive.pitalium.explorer.io.ExplorerPersister;
import com.htmlhifive.pitalium.explorer.response.Result;
import com.htmlhifive.pitalium.explorer.response.ResultDirectory;
import com.htmlhifive.pitalium.explorer.response.ResultListOfExpected;
import com.htmlhifive.pitalium.explorer.response.ScreenshotFile;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.explorer.service.ExplorerService;

@Controller
public class ApiController {

	@Autowired
	private ExplorerService service;

	/**
	 * Gets list of sub-directories under 'results' directory with the comparison results information.
	 * Parameters usage is same with listTestExecution API.
	 *
	 * @param page
	 * @param pageSize
	 * @param searchTestMethod
	 * @param searchTestScreen
	 * @return
	 */
	@RequestMapping(value = "_results/list", method = RequestMethod.GET, produces="application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Page<ResultDirectory>> getList(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "0") int pageSize,
			@RequestParam(value = "searchTestMethod", defaultValue = "") String searchTestMethod,
			@RequestParam(value = "searchTestScreen", defaultValue = "") String searchTestScreen,
			@RequestParam(value = "refresh", defaultValue = "true") boolean refresh) {
		Page<ResultDirectory> list = service.findResultDirectory(searchTestMethod, searchTestScreen, page, pageSize, refresh);
		return new ResponseEntity<Page<ResultDirectory>>(list, HttpStatus.OK);
	}
	/**
	 * 
	 * @param name
	 * @param refresh
	 * @return
	 */
	@RequestMapping(value = "_screenshots/list", method = RequestMethod.GET, produces="application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Map<String, List>> getScreenshotFiles(
			@RequestParam(value = "path", defaultValue = "0") String path,
			@RequestParam(value = "refresh", defaultValue = "true") boolean refresh){
		Map<String, List> map = service.findScreenshotFiles(path, refresh);
		return new ResponseEntity<Map<String, List>>(map, HttpStatus.OK);
	}
	/**
	 * 
	 * @param directoryName
	 * @param expectedFilename
	 * @param targetFilenames
	 * @return
	 */
	@RequestMapping(value = "_screenshots/compare", method = RequestMethod.POST, produces="application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<ResultListOfExpected> executeComparing(
			@RequestParam(value = "expected", defaultValue = "") String expectedFilePath,
			@RequestParam(value = "targets", defaultValue = "") String[] targetFilePaths
			){
		ResultListOfExpected resultListOfExpected = service.executeComparing(expectedFilePath, targetFilePaths);
		return new ResponseEntity<ResultListOfExpected>(resultListOfExpected, HttpStatus.OK);
	}

	@RequestMapping(value = "_screenshots/images", method = RequestMethod.GET, produces="application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Map<String, byte[]>> getImages(
			@RequestParam(value = "expected", defaultValue = "") String expectedFilePath,
			@RequestParam(value = "target", defaultValue = "") String targetFilePath
			){
		Map<String, byte[]> map = service.getImages(expectedFilePath, targetFilePath);
		return new ResponseEntity<Map<String, byte[]>>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "_screenshots/result", method = RequestMethod.GET, produces="application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<ComparedRectangle>> getComparedResult(
			@RequestParam(value = "path", defaultValue = "") String path,
			@RequestParam(value = "resultListId", defaultValue = "") int resultListId,
			@RequestParam(value = "targetResultId", defaultValue = "") int targetResultId
			){
		List<ComparedRectangle> list = service.getComparedResult(path, resultListId, targetResultId);
		return new ResponseEntity<List<ComparedRectangle>>(list, HttpStatus.OK);
	}

	@RequestMapping(value = "_screenshots/results", method = RequestMethod.GET, produces="application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<String> deleteResults(
			@RequestParam(value = "path", defaultValue = "") String path,
			@RequestParam(value = "resultListId", defaultValue = "") int resultListId
			){
		String result = service.deleteResults(path, resultListId);
		return new ResponseEntity<String>(result, HttpStatus.OK);
	}

	
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
			@RequestParam(value = "searchTestMethod", defaultValue = "") String searchTestMethod,
			@RequestParam(value = "searchTestScreen", defaultValue = "") String searchTestScreen) {
		Page<TestExecutionResult> list = service.findTestExecution(searchTestMethod, searchTestScreen, page, pageSize);
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

}
