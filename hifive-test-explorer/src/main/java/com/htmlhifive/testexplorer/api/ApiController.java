/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.response.TestExecutionResult;

@Controller
@RequestMapping("/api")
public class ApiController {

	@Autowired
	private TestExecutionRepository testExecutionRepo;
	@Autowired
	private ScreenshotRepository screenshotRepo;

	@Autowired
	private HttpServletRequest request;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ApiController.class);
	
	private static final int defaultPageSize = 20;

	/**
	 * Gets list of the test execution.
	 * 
	 * If pageSize equals to zero, the default page size is used.
	 * If pageSize equals to -1, the entire list is returned.
	 * 
	 * @param page Which page to show.
	 * @param pageSize Page size.
	 * @return Page of test execution
	 */
	@RequestMapping(value = "/listTestExecution", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Page<TestExecutionResult>> listTestExecution(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "0") int pageSize,
			@RequestParam(defaultValue = "") String searchTestMethod,
			@RequestParam(defaultValue = "") String searchTestScreen) {
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		}
		else if (pageSize == -1) {
			pageSize = (int)Math.min(testExecutionRepo.count(), Integer.MAX_VALUE);
		}
		PageRequest pageRequest = new PageRequest(page - 1, pageSize, new Sort("id"));
		Page<TestExecutionResult> list = testExecutionRepo.search(searchTestMethod, searchTestScreen, pageRequest);
		return new ResponseEntity<Page<TestExecutionResult>>(list, HttpStatus.OK);
	}

	/**
	 * Gets list of the screenshots which is narrowed down by a test execution.
	 *
	 * @param executionId test execution id
	 * @return list of screenshots
	 */
	@RequestMapping(value = "/listScreenshot", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<Screenshot>> listScreenshot(
			@RequestParam Integer testExecutionId,
			@RequestParam(defaultValue = "") String searchTestMethod,
			@RequestParam(defaultValue = "") String searchTestScreen) {
		List<Screenshot> list = screenshotRepo.findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(
				testExecutionId, searchTestMethod, searchTestScreen);
		return new ResponseEntity<List<Screenshot>>(list, HttpStatus.OK);
	}

	/**
	 * Gets the screenshot from screenshot id.
	 *
	 * @param id screenshot id
	 * @return screenshot
	 */
	@RequestMapping(value = "/getScreenshot", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Screenshot> getDetail(@RequestParam Integer id) {
		Screenshot item = screenshotRepo.findOne(id);
		return new ResponseEntity<Screenshot>(item, HttpStatus.OK);
	}

}
