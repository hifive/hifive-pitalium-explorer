package com.htmlhifive.testexplorer.api;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

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
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestEnvironment;
import com.htmlhifive.testexplorer.entity.TestExecution;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.file.ImageFileUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class ImageControllerTest {
	@Autowired
	private ImageController imageController;

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
	private ArrayList<ProcessedImage> processedImages;
	
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
		processedImages = new ArrayList<ProcessedImage>();
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
	public void testGetImageNotFound()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getImage(-1, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetImageFileError()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getImage(0, response);
		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetImageOk() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getImage(0, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetDiffImageNotFoundSource()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getDiffImage(-1, 0, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetDiffImageNotFoundTarget()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getDiffImage(0, -1, response);;
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetDiffImageFileError()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getDiffImage(0, 1, response);
		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetDiffImageOk() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getDiffImage(0, 0, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetDiffImageOkDifferent() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc0 = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc0);
		Screenshot sc1 = screenshotRepo.findOne(1);
		doReturn(new File("src/test/resources/images/edge_detector_0_edge.png")).
			when(spy.imageFileUtil).getFile(sc1);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getDiffImage(0, 1, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetProcessedNotFound()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "-1");
		params.put("algorithm", "edge");
		params.put("colorIndex", "-1");
		this.imageController.getProcessed(-1, "edge", params, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetProcessedUnknownMethod()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "-1");
		params.put("algorithm", "aaaaaa");
		params.put("colorIndex", "-1");
		this.imageController.getProcessed(-1, "aaaaaa", params, response);
		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void testGetProcessedFileError()
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "0");
		params.put("algorithm", "edge");
		params.put("colorIndex", "-1");
		this.imageController.getProcessed(0, "edge", params, response);
		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetProcessedEdgeNoColorIndex() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "0");
		params.put("algorithm", "edge");

		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getProcessed(0, "edge", params, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetProcessedEdgeColorIndex0() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "0");
		params.put("algorithm", "edge");
		params.put("colorIndex", "0");

		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getProcessed(0, "edge", params, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetProcessedEdgeColorIndex1() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "0");
		params.put("algorithm", "edge");
		params.put("colorIndex", "1");

		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getProcessed(0, "edge", params, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetProcessedEdgeColorIndexInvalid() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("id", "0");
		params.put("algorithm", "edge");
		params.put("colorIndex", "invalid");

		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		Screenshot sc = screenshotRepo.findOne(0);
		doReturn(new File("src/test/resources/images/edge_detector_0.png")).
			when(spy.imageFileUtil).getFile(sc);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getProcessed(0, "edge", params, response);

		verify(response).setContentType("image/png");
	}
	

	@Test
	public void testGetDiffImageFileExists() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		doReturn("src/test/resources/images/edge_detector_0.png").
			when(spy.imageFileUtil).getAbsoluteFilePath(any(String.class));
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getDiffImage(0, 0, response);

		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetDiffImageFileDirectory() throws IOException
	{
		HttpServletResponse response = mock(HttpServletResponse.class);
		ImageController spy = spy(this.imageController);
		spy.imageFileUtil = spy(spy.imageFileUtil);
		doReturn("src/test/resources/images/").
			when(spy.imageFileUtil).getAbsoluteFilePath(any(String.class));
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		spy.getDiffImage(0, 0, response);

		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
}
