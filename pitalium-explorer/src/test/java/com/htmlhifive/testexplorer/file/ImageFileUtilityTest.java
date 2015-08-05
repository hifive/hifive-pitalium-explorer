/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.file;

import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.testexplorer.entity.Config;
import com.htmlhifive.testexplorer.entity.ConfigRepository;
import com.htmlhifive.testexplorer.entity.ProcessedImage;
import com.htmlhifive.testexplorer.entity.ProcessedImageKey;
import com.htmlhifive.testexplorer.entity.ProcessedImageRepository;
import com.htmlhifive.testexplorer.entity.Repositories;
import com.htmlhifive.testexplorer.entity.RepositoryMockCreator;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestEnvironment;
import com.htmlhifive.testexplorer.entity.TestExecution;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class ImageFileUtilityTest {
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
	public void initializeDefaultMockObjects()
	{
		RepositoryMockCreator r = new RepositoryMockCreator(new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo)); 
		configs = r.getConfigs();
		screenshots = r.getScreenshots();
		new ArrayList<ProcessedImage>();
		testExecutions = r.getTestExecutions();
		testEnvironments = r.getTestEnvironments();
	}


	@Test
	public void testGetAbsoluteFilePath()
	{
		ImageFileUtility util = new ImageFileUtility(new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo));
		String path = util.getAbsoluteFilePath("test.png");
		Assert.assertTrue(path.endsWith("test.png"));
	}

	@Test
	public void testProcessedFilePath()
	{
		ImageFileUtility util = new ImageFileUtility(new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo));
		String path = util.newProcessedFilePath(new ProcessedImageKey(123, "best"));
		Assert.assertTrue(path.startsWith("processed-image"));
	}
}
