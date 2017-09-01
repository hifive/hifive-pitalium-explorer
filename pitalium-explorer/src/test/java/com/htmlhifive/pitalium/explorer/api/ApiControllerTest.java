/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

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
import com.htmlhifive.pitalium.explorer.service.ExplorerService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
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

	// ApiControllerのserviceのバックアップ
	private ExplorerService explorerService;

	/**
	 * Initialize some mock objects for testing. This method is called before each test method.
	 */
	@Before
	public void initializeDefaultMockObjects() {
		RepositoryMockCreator r = new RepositoryMockCreator(
				new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo));
		configs = r.getConfigs();
		screenshots = r.getScreenshots();
		new ArrayList<ProcessedImage>();
		testExecutions = r.getTestExecutions();
		testEnvironments = r.getTestEnvironments();
	}

	@Before
	public void testInit() {
		// ApiControllerのserviceを各テストで置き換えるのでバックアップを取っておく
		explorerService = (ExplorerService) Whitebox.getInternalState(this.apiController, "service");
	}

	@After
	public void testCleanup() {
		// ApiControllerのserviceのバックアップを戻す
		Whitebox.setInternalState(this.apiController, "service", explorerService);
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
		// 戻り値を置き換えるためのdummyのPage
		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			converted.add(new TestExecutionResult(te, 0l, 1l));
		}
		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);

		// findTestExecution()の戻り値を置き換えるためmockにする
		ExplorerService service = mock(ExplorerService.class);
		when(service.findTestExecution("", "", 1, 0, "")).thenReturn(page);

		// ApiControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.apiController, "service", service);

		// 実行
		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, 0, "", "", "");

		// responseのステータスコードが200か確認
		assertThat(response.getStatusCode().value(), is(200));

		// 戻り値が意図した値になっているか確認
		List<TestExecutionResult> responseBody = response.getBody().getContent();
		for (int i = 0; i < responseBody.size(); i++) {
			TestExecution expected = testExecutions.get(i);
			TestExecution actual = responseBody.get(i).getTestExecution();
			assertThat(actual.getId(), is(expected.getId()));
			assertThat(actual.getTimeString(), is(expected.getTimeString()));
		}

		// findTestExecution()が引数"", "", 1, 0, ""で呼び出されたか確認
		verify(service).findTestExecution("", "", 1, 0, "");
	}

	@Test
	public void testListTestExecutionWithPageSize() throws ClassNotFoundException, IOException {
		// 戻り値を置き換えるためのdummyのPage
		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			converted.add(new TestExecutionResult(te, 0l, 1l));
		}
		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);

		// findTestExecution()の戻り値を置き換えるためmockにする
		ExplorerService service = mock(ExplorerService.class);
		when(service.findTestExecution("", "", 1, 20, "")).thenReturn(page);

		// ApiControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.apiController, "service", service);

		// 実行
		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, 20, "", "", "");

		// responseのステータスコードが200か確認
		assertThat(response.getStatusCode().value(), is(200));

		// 戻り値が意図した値になっているか確認
		List<TestExecutionResult> responseBody = response.getBody().getContent();
		for (int i = 0; i < responseBody.size(); i++) {
			TestExecution expected = testExecutions.get(i);
			TestExecution actual = responseBody.get(i).getTestExecution();
			assertThat(actual.getId(), is(expected.getId()));
			assertThat(actual.getTimeString(), is(expected.getTimeString()));
		}

		// findTestExecution()が引数"", "", 1, 20, ""で呼び出されたか確認
		verify(service).findTestExecution("", "", 1, 20, "");
	}

	@Test
	public void testListTestExecutionWithPageSizeUnlimited() throws ClassNotFoundException, IOException {
		// 戻り値を置き換えるためのdummyのPage
		List<TestExecution> cloned = getTestExecutionMockClone();
		List<TestExecutionResult> converted = new ArrayList<TestExecutionResult>();

		for (TestExecution te : cloned) {
			converted.add(new TestExecutionResult(te, 0l, 1l));
		}
		Page<TestExecutionResult> page = new PageImpl<TestExecutionResult>(converted);

		// findTestExecution()の戻り値を置き換えるためmockにする
		ExplorerService service = mock(ExplorerService.class);
		when(service.findTestExecution("", "", 1, -1, "")).thenReturn(page);

		// ApiControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.apiController, "service", service);

		// 実行
		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, -1, "", "", "");

		// responseのステータスコードが200か確認
		assertThat(response.getStatusCode().value(), is(200));

		// 戻り値が意図した値になっているか確認
		List<TestExecutionResult> responseBody = response.getBody().getContent();
		for (int i = 0; i < responseBody.size(); i++) {
			TestExecution expected = testExecutions.get(i);
			TestExecution actual = responseBody.get(i).getTestExecution();
			assertThat(actual.getId(), is(expected.getId()));
			assertThat(actual.getTimeString(), is(expected.getTimeString()));
		}

		// findTestExecution()が引数"", "", 1, -1, ""で呼び出されたか確認
		verify(service).findTestExecution("", "", 1, -1, "");
	}

	@Test
	public void testSearch() throws ClassNotFoundException, IOException {
		// 戻り値を置き換えるためのdummyのPage
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

		// findTestExecution()の戻り値を置き換えるためmockにする
		ExplorerService service = mock(ExplorerService.class);
		when(service.findTestExecution("thod1", "screen1", 1, 20, "")).thenReturn(page);

		// ApiControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.apiController, "service", service);

		// 実行
		ResponseEntity<Page<TestExecutionResult>> response = this.apiController.listTestExecution(1, 20, "", "thod1",
				"screen1");

		// responseのステータスコードが200か確認
		assertThat(response.getStatusCode().value(), is(200));

		// 戻り値が意図した値になっているか確認
		List<TestExecutionResult> responseBody = response.getBody().getContent();

		HashSet<Integer> actualIds = new HashSet<Integer>();
		for (int i = 0; i < responseBody.size(); i++) {
			actualIds.add(responseBody.get(i).getTestExecution().getId());
		}

		Integer[] expectedList = expectedIds.toArray(new Integer[expectedIds.size()]);
		Integer[] actualList = actualIds.toArray(new Integer[actualIds.size()]);
		assertThat(actualIds.size(), is(expectedIds.size()));
		for (int i = 0; i < expectedIds.size(); i++) {
			assertThat(actualList[i].intValue(), is(expectedList[i].intValue()));
		}

		// findTestExecution()が引数"thod1", "screen1", 1, 20, ""で呼び出されたか確認
		verify(service).findTestExecution("thod1", "screen1", 1, 20, "");
	}

	@Test
	public void testListScreenshot() {
		// 戻り値を置き換えるためのdummyの値
		Map<Integer, ArrayList<Screenshot>> mapTE2S = new HashMap<Integer, ArrayList<Screenshot>>();
		for (Screenshot sc : screenshots) {
			Integer tid = sc.getTestExecution().getId();
			if (!mapTE2S.containsKey(tid)) {
				mapTE2S.put(tid, new ArrayList<Screenshot>());
			}
			mapTE2S.get(tid).add(sc);
		}

		// findScreenshot()の戻り値を置き換えるためmockにする
		ExplorerService service = mock(ExplorerService.class);
		for (Map.Entry<Integer, ArrayList<Screenshot>> entry : mapTE2S.entrySet()) {
			int testExecuteId = entry.getKey().intValue();
			when(service.findScreenshot(testExecuteId, "", "")).thenReturn(entry.getValue());
		}

		// ApiControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.apiController, "service", service);

		for (Map.Entry<Integer, ArrayList<Screenshot>> entry : mapTE2S.entrySet()) {
			int testExecuteId = entry.getKey().intValue();
			// 実行
			ResponseEntity<List<Screenshot>> response = this.apiController.searchScreenshots(testExecuteId, "", "");

			// responseのステータスコードが200か確認
			assertThat(response.getStatusCode().value(), is(200));

			// findScreenshot()が引数testExecuteId, "", ""で呼び出されたか確認
			verify(service).findScreenshot(testExecuteId, "", "");
		}
	}

	@Test
	public void testGetDetail() {
		// getScreenshot()の戻り値を置き換えるためmockにする
		ExplorerService service = mock(ExplorerService.class);
		for (int i = 0; i < screenshots.size(); i++) {
			int id = screenshots.get(i).getId().intValue();
			// 戻り値を置き換えるためのdummyのscreenshot
			when(service.getScreenshot(id)).thenReturn(screenshots.get(id));
		}

		// ApiControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.apiController, "service", service);

		for (int i = 0; i < screenshots.size(); i++) {
			int id = screenshots.get(i).getId().intValue();
			// 実行
			ResponseEntity<Screenshot> response = this.apiController.getScreenshot(id);

			// responseのステータスコードが200か確認
			assertThat(response.getStatusCode().value(), is(200));

			// 戻り値が意図した値になっているか確認
			assertThat(response.getBody().getId().intValue(), is(id));

			// getScreenshot()が引数idで呼び出されたか確認
			verify(service).getScreenshot(id);
		}
	}

	@Test
	public void testGetDetailFail() {
		ResponseEntity<Screenshot> response = this.apiController.getScreenshot(-1);
		assertThat(response.getBody(), is(nullValue()));
	}
}
