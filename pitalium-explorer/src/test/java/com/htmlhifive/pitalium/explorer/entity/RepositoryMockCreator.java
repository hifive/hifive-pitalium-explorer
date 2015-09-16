/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;

public class RepositoryMockCreator {
	private TestExecutionRepository testExecutionRepo;
	private ScreenshotRepository screenshotRepo;
	private ConfigRepository configRepo;
	private ProcessedImageRepository processedImageRepo;
	private ArrayList<Config> configs;
	private ArrayList<Screenshot> screenshots;
	private ArrayList<TestExecution> testExecutions;
	private ArrayList<TestEnvironment> testEnvironments;

	public RepositoryMockCreator(Repositories repositoriesMock) {
		this.testExecutionRepo = repositoriesMock.getTestExecutionRepository();
		this.screenshotRepo = repositoriesMock.getScreenshotRepository();
		this.configRepo = repositoriesMock.getConfigRepository();
		this.processedImageRepo = repositoriesMock.getProcessedImageRepository();

		initializeDefaultMockObjects();
	}

	private void initializeDefaultTestExecution() {
		{
			TestExecution testExecution = new TestExecution();
			testExecution.setId(17);
			testExecution.setLabel("API TEST LABEL");
			testExecution.setTime(new Timestamp(111111));
			testExecutions.add(testExecution);

			when(testExecutionRepo.findOne(testExecution.getId())).thenReturn(testExecution);
		}
		{
			TestExecution testExecution = new TestExecution();
			testExecution.setId(42);
			testExecution.setLabel("API TEST LABEL 2");
			testExecution.setTime(new Timestamp(1111111));
			testExecutions.add(testExecution);

			when(testExecutionRepo.findOne(testExecution.getId())).thenReturn(testExecution);
		}
	}

	private void initializeDefaultTestEnvironment() {
		{
			TestEnvironment env = new TestEnvironment();
			env.setBrowserName("Chrome");
			env.setBrowserVersion("22.22.22");
			env.setDeviceName("Galaxy Gear");
			env.setId(1);
			env.setLabel("heh");
			env.setPlatform("Tizen");
			env.setPlatformVersion("2.2.5");
			testEnvironments.add(env);
		}
		{
			TestEnvironment env = new TestEnvironment();
			env.setBrowserName("Netscape Navigator");
			env.setBrowserVersion("1.0.3");
			env.setDeviceName("486");
			env.setId(2);
			env.setLabel("such old");
			env.setPlatform("Windows 95");
			env.setPlatformVersion("4.0");
			testEnvironments.add(env);
		}
		{
			TestEnvironment env = new TestEnvironment();
			env.setBrowserName("w3m");
			env.setBrowserVersion("3.0.1");
			env.setDeviceName("Arduino");
			env.setId(3);
			env.setLabel("C++lover");
			env.setPlatform("Haiku");
			env.setPlatformVersion("4.2");
			testEnvironments.add(env);
		}
	}

	private void initializeDefaultScreenshot() {
		for (int i = 0; i < 40; i++) {
			Screenshot sc = new Screenshot();
			if (i % 3 != 0) {
				sc.setExpectedScreenshotId(getScreenshots().get(1000000007 % i).getId());
				sc.setComparisonResult(i % 2 == 0);
			}
			sc.setFileName("screenshot" + Integer.toString(i));
			sc.setId(i);
			sc.setTestClass("class" + Integer.toString(i % 5));
			sc.setTestEnvironment(testEnvironments.get(1023 % (testEnvironments.size())));
			sc.setTestExecution(testExecutions.get(511 % (testExecutions.size())));
			sc.setTestMethod("method" + Integer.toString(i / 2 % 2));
			sc.setTestScreen("screen" + Integer.toString(i / 3 % 2));
			getScreenshots().add(sc);

			when(screenshotRepo.findOne(sc.getId())).thenReturn(sc);
		}

		ArrayList<Screenshot> notProcessed = new ArrayList<Screenshot>();
		notProcessed.add(getScreenshots().get(0));
		when(screenshotRepo.findNotProcessedEdge(any(Integer.class))).thenReturn(notProcessed);
	}

	/**
	 * Initialize some mock objects for testing. This method is called before each test method.
	 */
	private void initializeDefaultMockObjects() {
		configs = new ArrayList<Config>();
		screenshots = new ArrayList<Screenshot>();
		new ArrayList<ProcessedImage>();
		testExecutions = new ArrayList<TestExecution>();
		testEnvironments = new ArrayList<TestEnvironment>();

		initializeDefaultTestExecution();
		initializeDefaultTestEnvironment();
		initializeDefaultScreenshot();

		{
			Config pathconfig = new Config();
			pathconfig.setKey(ConfigRepository.ABSOLUTE_PATH_KEY);
			pathconfig.setValue("src/test/resources/");
			when(configRepo.findOne(pathconfig.getKey())).thenReturn(pathconfig);
			getConfigs().add(pathconfig);
		}
	}

	/**
	 * @return the configs
	 */
	public ArrayList<Config> getConfigs() {
		return configs;
	}

	/**
	 * @return the screenshots
	 */
	public ArrayList<Screenshot> getScreenshots() {
		return screenshots;
	}

	/**
	 * @return the testExecutions
	 */
	public ArrayList<TestExecution> getTestExecutions() {
		return testExecutions;
	}

	/**
	 * @return the testEnvironments
	 */
	public ArrayList<TestEnvironment> getTestEnvironments() {
		return testEnvironments;
	}
}
