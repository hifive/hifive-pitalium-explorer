/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import java.util.List;

import javax.annotation.PostConstruct;

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
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.explorer.service.ExplorerService;

@Controller
@RequestMapping("/api")
public class ApiController {

	@Autowired
	private ExplorerService service;

	@PostConstruct
	public void init() {
		service.init();
	}

	/**
	 * Gets list of the test execution. If pageSize equals to zero, the default page size is used. If pageSize equals to
	 * -1, the entire list is returned.
	 * 
	 * @param page Which page to show.
	 * @param pageSize Page size.
	 * @return Page of test execution
	 */
	@RequestMapping(value = "/listTestExecution", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Page<TestExecutionResult>> listTestExecution(@RequestParam(defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "0") int pageSize,
			@RequestParam(defaultValue = "") String searchTestMethod,
			@RequestParam(defaultValue = "") String searchTestScreen) {
		Page<TestExecutionResult> list = service.findTestExecution(searchTestMethod, searchTestScreen, page, pageSize);
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
	public ResponseEntity<List<Screenshot>> listScreenshot(@RequestParam Integer testExecutionId,
			@RequestParam(defaultValue = "") String searchTestMethod,
			@RequestParam(defaultValue = "") String searchTestScreen) {
		List<Screenshot> list = service.findScreenshot(testExecutionId, searchTestMethod, searchTestScreen);
		return new ResponseEntity<List<Screenshot>>(list, HttpStatus.OK);
	}

	/**
	 * Gets the screenshot from screenshot id.
	 *
	 * @param screenshotId screenshot id
	 * @return screenshot
	 */
	@RequestMapping(value = "/getScreenshot", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<Screenshot> getDetail(@RequestParam Integer screenshotId) {
		Screenshot item = service.getScreenshot(screenshotId);
		return new ResponseEntity<Screenshot>(item, HttpStatus.OK);
	}

}
