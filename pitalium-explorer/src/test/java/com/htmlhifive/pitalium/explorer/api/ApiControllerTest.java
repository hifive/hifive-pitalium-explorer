/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.pitalium.explorer.entity.Config;
import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImage;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageRepository;
import com.htmlhifive.pitalium.explorer.entity.Repositories;
import com.htmlhifive.pitalium.explorer.entity.RepositoryMockCreator;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotRepository;
import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionRepository;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class ApiControllerTest {
	@Autowired
	private ApiController apiController;

	@Autowired
	private TestExecutionRepository testExecutionRepo;

	@Autowired
	private ScreenshotRepository screenshotRepo;

	@Autowired
	private ConfigRepository configRepo;

	@Autowired
	private ProcessedImageRepository processedImageRepo;

	private ArrayList<Config> configs;
	private ArrayList<Screenshot> screenshots;
	private ArrayList<TestExecution> testExecutions;
	private ArrayList<TestEnvironment> testEnvironments;

	/**
	 * Initialize some mock objects for testing. This method is called before each test method.
	 */
	@Before
	public void initializeDefaultMockObjects() {
		RepositoryMockCreator r = new RepositoryMockCreator(new Repositories(configRepo, processedImageRepo,
				screenshotRepo, testExecutionRepo));
		configs = r.getConfigs();
		screenshots = r.getScreenshots();
		new ArrayList<ProcessedImage>();
		testExecutions = r.getTestExecutions();
		testEnvironments = r.getTestEnvironments();
	}

	@SuppressWarnings("unchecked")
	private ArrayList<TestExecution> getTestExecutionMockClone() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		{
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(testExecutions);
			oos.close();
		}
		bos.close();
		byte[] byteData = bos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
		return (ArrayList<TestExecution>) new ObjectInputStream(bais).readObject();
	}

	@Test
	public void testListTestExecution() throws ClassNotFoundException, IOException {
		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			converted.add(new TestExecutionResult(te, 0l, 1l));
		}

		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);
		reset(testExecutionRepo);
		when(testExecutionRepo.search(eq(""), eq(""), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, 0, "", "");
		Assert.assertEquals(200, response.getStatusCode().value());
		List<TestExecutionResult> responseBody = response.getBody().getContent();
		for (int i = 0; i < responseBody.size(); i++) {
			Assert.assertEquals(testExecutions.get(i).getId().intValue(), responseBody.get(i).getTestExecution()
					.getId().intValue());
			Assert.assertEquals(testExecutions.get(i).getTimeString(), responseBody.get(i).getTestExecution()
					.getTimeString());
		}
		verify(testExecutionRepo).search(eq(""), eq(""), any(Pageable.class));
	}

	@Test
	public void testListTestExecutionWithPageSize() throws ClassNotFoundException, IOException {
		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			converted.add(new TestExecutionResult(te, 0l, 1l));
		}

		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);
		reset(testExecutionRepo);
		when(testExecutionRepo.search(eq(""), eq(""), any(Pageable.class))).thenReturn(page);
		when(testExecutionRepo.count()).thenReturn((long) converted.size());

		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, 20, "", "");
		Assert.assertEquals(200, response.getStatusCode().value());
		List<TestExecutionResult> responseBody = response.getBody().getContent();
		for (int i = 0; i < responseBody.size(); i++) {
			Assert.assertEquals(testExecutions.get(i).getId().intValue(), responseBody.get(i).getTestExecution()
					.getId().intValue());
			Assert.assertEquals(testExecutions.get(i).getTimeString(), responseBody.get(i).getTestExecution()
					.getTimeString());
		}
		verify(testExecutionRepo).search(eq(""), eq(""), any(Pageable.class));
	}

	@Test
	public void testListTestExecutionWithPageSizeUnlimited() throws ClassNotFoundException, IOException {
		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			converted.add(new TestExecutionResult(te, 0l, 1l));
		}

		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);
		reset(testExecutionRepo);
		when(testExecutionRepo.search(eq(""), eq(""), any(Pageable.class))).thenReturn(page);
		when(testExecutionRepo.count()).thenReturn((long) converted.size());

		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, -1, "", "");
		Assert.assertEquals(200, response.getStatusCode().value());
		List<TestExecutionResult> responseBody = response.getBody().getContent();
		for (int i = 0; i < responseBody.size(); i++) {
			Assert.assertEquals(testExecutions.get(i).getId().intValue(), responseBody.get(i).getTestExecution()
					.getId().intValue());
			Assert.assertEquals(testExecutions.get(i).getTimeString(), responseBody.get(i).getTestExecution()
					.getTimeString());
		}
		verify(testExecutionRepo).search(eq(""), eq(""), any(Pageable.class));
	}

	@Test
	public void testSearch() throws ClassNotFoundException, IOException {
		HashSet<Integer> expectedIds = new HashSet<Integer>();
		for (int i = 0; i < screenshots.size(); i++) {
			if (screenshots.get(i).getTestMethod().contains("thod1")
					&& screenshots.get(i).getTestScreen().contains("screen1")) {
				expectedIds.add(screenshots.get(i).getTestExecution().getId());
			}
		}

		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			if (expectedIds.contains(te.getId())) {
				converted.add(new TestExecutionResult(te, 0l, 1l));
			}
		}

		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);
		reset(testExecutionRepo);
		when(testExecutionRepo.search(eq("thod1"), eq("screen1"), any(Pageable.class))).thenReturn(page);
		when(testExecutionRepo.count()).thenReturn((long) converted.size());

		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, 20, "thod1",
				"screen1");
		Assert.assertEquals(200, response.getStatusCode().value());
		List<TestExecutionResult> responseBody = response.getBody().getContent();

		HashSet<Integer> realIds = new HashSet<Integer>();
		for (int i = 0; i < responseBody.size(); i++) {
			realIds.add(responseBody.get(i).getTestExecution().getId());
		}

		Integer[] expectedList = expectedIds.toArray(new Integer[expectedIds.size()]);
		Integer[] realList = realIds.toArray(new Integer[realIds.size()]);
		Assert.assertEquals(expectedIds.size(), realIds.size());
		for (int i = 0; i < expectedIds.size(); i++) {
			Assert.assertEquals(expectedList[i].intValue(), realList[i].intValue());
		}

		verify(testExecutionRepo).search(eq("thod1"), eq("screen1"), any(Pageable.class));
	}

	@Test
	public void testListScreenshot() {
		Map<Integer, ArrayList<Screenshot>> mapTE2S = new HashMap<Integer, ArrayList<Screenshot>>();
		for (Screenshot sc : screenshots) {
			Integer tid = sc.getTestExecution().getId();
			if (!mapTE2S.containsKey(tid)) {
				mapTE2S.put(tid, new ArrayList<Screenshot>());
			}
			mapTE2S.get(tid).add(sc);
		}
		for (Map.Entry<Integer, ArrayList<Screenshot>> entry : mapTE2S.entrySet()) {
			when(
					screenshotRepo.findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(eq(entry
							.getKey().intValue()), eq(""), eq(""))).thenReturn(entry.getValue());
		}

		for (Map.Entry<Integer, ArrayList<Screenshot>> entry : mapTE2S.entrySet()) {
			int testExecuteId = entry.getKey().intValue();
			ResponseEntity<List<Screenshot>> response = this.apiController.listScreenshot(testExecuteId, "", "");
			Assert.assertEquals(200, response.getStatusCode().value());
			verify(screenshotRepo).findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(
					eq(testExecuteId), eq(""), eq(""));
		}
	}

	@Test
	public void testGetDetail() {
		for (int i = 0; i < screenshots.size(); i++) {
			int id = screenshots.get(i).getId().intValue();
			when(screenshotRepo.findOne(i)).thenReturn(screenshots.get(i));
			ResponseEntity<Screenshot> response = this.apiController.getDetail(id);
			Assert.assertEquals(200, response.getStatusCode().value());
			Assert.assertEquals(id, response.getBody().getId().intValue());
			verify(screenshotRepo).findOne(id);
		}
	}

	@Test
	public void testGetDetailFail() {
		ResponseEntity<Screenshot> response = this.apiController.getDetail(-1);
		Assert.assertEquals(null, response.getBody());
	}
}
