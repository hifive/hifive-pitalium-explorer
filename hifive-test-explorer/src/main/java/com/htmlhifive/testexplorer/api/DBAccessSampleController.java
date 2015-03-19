package com.htmlhifive.testexplorer.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.htmlhifive.testexplorer.entity.TestCaseResult;
import com.htmlhifive.testexplorer.entity.TestCaseResultRepository;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;

@Controller
@RequestMapping("/dbaccess")
public class DBAccessSampleController {

	@Autowired
	private TestCaseResultRepository resultRepo;
	@Autowired
	private ScreenshotRepository screenshotRepo;

	@RequestMapping(value = "/findAll", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestCaseResult>> findAll() {
		List<TestCaseResult> resultList = resultRepo.findAll(new Sort(Direction.ASC, "executeTime"));
		return new ResponseEntity<>(resultList, HttpStatus.OK);
	}

	@RequestMapping(value = "/find", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestCaseResult>> find(@RequestParam String expectedId) {
		List<TestCaseResult> resultList = resultRepo.find(expectedId);
		return new ResponseEntity<>(resultList, HttpStatus.OK);
	}

	@RequestMapping(value = "/findKeyword", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestCaseResult>> findKeyword(@RequestParam String expectedId) {
		List<TestCaseResult> resultList = resultRepo.findKeyword(expectedId);
		return new ResponseEntity<>(resultList, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/findRage", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<TestCaseResult>> findRage(@RequestParam String start, @RequestParam String end) {
		List<TestCaseResult> resultList = resultRepo.findRange(start, end);
		return new ResponseEntity<>(resultList, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<TestCaseResult> save(@RequestParam String executeTime, @RequestParam String expectedId) {
		TestCaseResult result = new TestCaseResult();
		result.setExecuteTime(executeTime);
		result.setExpectedId(expectedId);
		result = resultRepo.save(result);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/findScreenshotAll", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<Screenshot>> findScreenshotAll() {
		List<Screenshot> resultList = screenshotRepo.findAll();
		return new ResponseEntity<>(resultList, HttpStatus.OK);
	}

	@RequestMapping(value = "/findByExecuteTime", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<Screenshot>> findByExecuteTime(@RequestParam String executeTime) {
		List<Screenshot> resultList = screenshotRepo.find(executeTime);
		return new ResponseEntity<>(resultList, HttpStatus.OK);
	}

}
