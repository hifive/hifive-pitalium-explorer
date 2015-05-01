/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestExecution;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;

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

	/**
	 * Gets list of the test execution.
	 *
	 * @return list of test execution
	 */
	@RequestMapping(value = "/listTestExecution", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestExecution>> listTestExecution() {
		List<TestExecution> list = testExecutionRepo.findAll();
		return new ResponseEntity<List<TestExecution>>(list, HttpStatus.OK);
	}

	/**
	 * Gets list of the test execution which is narrowed down by parameters.
	 *
	 * @param criteria parameter to narrow
	 * @return list of test execution
	 */
	@RequestMapping(value = "/listTestExecution/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestExecution>> search(@RequestParam String criteria) {
		// Not implemented.
		return listTestExecution();
	}

	/**
	 * Gets list of the screenshots which is narrowed down by a test execution.
	 *
	 * @param executionId test execution id
	 * @return list of screenshots
	 */
	@RequestMapping(value = "/listScreenshot", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<Screenshot>> listScreenshot(@RequestParam Integer testExecutionId) {
		List<Screenshot> list = screenshotRepo.findByTestExecutionId(testExecutionId);
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
