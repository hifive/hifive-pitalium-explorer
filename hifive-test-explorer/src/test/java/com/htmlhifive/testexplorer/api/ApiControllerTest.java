package com.htmlhifive.testexplorer.api;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.testexplorer.entity.Config;
import com.htmlhifive.testexplorer.entity.ProcessedImage;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestEnvironment;
import com.htmlhifive.testexplorer.entity.TestExecution;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class ApiControllerTest {
	@Autowired
	private ApiController apiController;

	@Autowired
	private TestExecutionRepository testExecutionRepo;

	@Autowired
	private ScreenshotRepository screenshotRepo;

	private ArrayList<Config> configs;
	private ArrayList<Screenshot> screenshots;
	private ArrayList<ProcessedImage> processedImages;
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
		}
		{
			TestExecution testExecution = new TestExecution();
			testExecution.setId(42);
			testExecution.setLabel("API TEST LABEL 2");
			testExecution.setTime(new Timestamp(1111111));
			testExecutions.add(testExecution);
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
			if (i%3 == 0 && i > 0)
			{
				sc.setExpectedScreenshot(screenshots.get(1000000007%i));
				sc.setComparisonResult(i%2 == 0);
			}
			sc.setFileName("screenshot" + Integer.toString(i));
			sc.setId(i);
			sc.setTestClass("class" + Integer.toString(i%5));
			sc.setTestEnvironment(testEnvironments.get(1023%(testEnvironments.size())));
			sc.setTestExecution(testExecutions.get(511%(testExecutions.size())));
			sc.setTestMethod("method" + Integer.toString(i/2%2));
			sc.setTestScreen("screen" + Integer.toString(i/3%2));
			screenshots.add(sc);
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
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<TestExecution> getTestExecutionMockClone() throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		{
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(testExecutions);
			oos.close();
		}
		bos.close();
		byte[] byteData = bos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
		return (ArrayList<TestExecution>)new ObjectInputStream(bais).readObject();
	}

	@Test
	public void testListTestExecution() throws ClassNotFoundException, IOException
	{
		List<TestExecution> cloned = getTestExecutionMockClone();
		when(testExecutionRepo.findAll()).thenReturn(cloned);

		ResponseEntity<List<TestExecution>> response = this.apiController.listTestExecution();
		Assert.assertEquals(200, response.getStatusCode().value());
		List<TestExecution> responseBody = response.getBody();
		for (int i = 0; i < responseBody.size(); i++)
		{
			Assert.assertEquals(testExecutions.get(i).getId().intValue(),
					responseBody.get(i).getId().intValue());
			Assert.assertEquals(testExecutions.get(i).getTimeString(),
					responseBody.get(i).getTimeString());
		}
		verify(testExecutionRepo).findAll();
	}

	@Test
	public void testListScreenshot()
	{
		Map<Integer, ArrayList<Screenshot>> mapTE2S = new HashMap<Integer, ArrayList<Screenshot>>();
		for (Screenshot sc : screenshots)
		{
			Integer tid = sc.getTestExecution().getId(); 
			if (!mapTE2S.containsKey(tid))
			{
				mapTE2S.put(tid, new ArrayList<Screenshot>());
			}
			mapTE2S.get(tid).add(sc); 
		}
		for (Map.Entry<Integer, ArrayList<Screenshot>> entry : mapTE2S.entrySet())
		{
			when(screenshotRepo.findByTestExecutionId(entry.getKey().intValue())).thenReturn(entry.getValue());
		}

		for (Map.Entry<Integer, ArrayList<Screenshot>> entry : mapTE2S.entrySet())
		{
			int testExecuteId = entry.getKey().intValue();  
			ResponseEntity<List<Screenshot>> response = this.apiController.listScreenshot(testExecuteId);
			Assert.assertEquals(200,  response.getStatusCode().value());
			verify(screenshotRepo).findByTestExecutionId(testExecuteId);
		}
	}
}
