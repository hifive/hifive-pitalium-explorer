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
import com.htmlhifive.testexplorer.entity.ProcessedImageRepository;
import com.htmlhifive.testexplorer.entity.Repositories;
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
	private void initializeDefaultTestExecution()
	{
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

	private void initializeDefaultTestEnvironment()
	{
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

	private void initializeDefaultScreenshot()
	{
		for (int i = 0; i < 40; i++)
		{
			Screenshot sc = new Screenshot();
			if (i%3 != 0)
			{
				sc.setExpectedScreenshot(screenshots.get(1000000007%i));
				sc.setComparisonResult(i%2 == 0);
			}
			sc.setFileName("screenshot" + Integer.toString(i));
			sc.setId(i);
			sc.setTestClass("class" + Integer.toString(i%5));
			sc.setTestEnvironment(testEnvironments.get(1023%(testEnvironments.size())));
			sc.setTestExecutionId(testExecutions.get(511%(testExecutions.size())).getId());
			sc.setTestMethod("method" + Integer.toString(i/2%2));
			sc.setTestScreen("screen" + Integer.toString(i/3%2));
			screenshots.add(sc);
			
			when(screenshotRepo.findOne(sc.getId())).thenReturn(sc);
		}
	}

	/**
	 * Initialize some mock objects for testing. This method is called before each test method.
	 */
	@Before
	public void initializeDefaultMockObjects()
	{
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
			pathconfig.setValue("/test1234/");
			when(configRepo.findOne(pathconfig.getKey())).thenReturn(pathconfig);
			configs.add(pathconfig);
		}
		
	}


	@Test
	public void testGetAbsoluteFilePath()
	{
		ImageFileUtility util = new ImageFileUtility(new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo));
		String path = util.getAbsoluteFilePath("test.png");
		Assert.assertTrue(path.endsWith("test.png"));
		Assert.assertTrue(path.contains("test1234"));
	}

}
