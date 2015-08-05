/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.cache;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.pitalium.explorer.cache.BackgroundImageDispatcher;
import com.htmlhifive.pitalium.explorer.cache.CacheTaskQueue;
import com.htmlhifive.pitalium.explorer.cache.PrioritizedTask;
import com.htmlhifive.pitalium.explorer.entity.Config;
import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImage;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageKey;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageRepository;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotRepository;
import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.entity.RepositoryMockCreator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class BackgroundImageDispatcherTest {

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

		ArrayList<Screenshot> notProcessed = new ArrayList<Screenshot>();
		notProcessed.add(screenshots.get(0));
		notProcessed.add(screenshots.get(1));
		notProcessed.add(screenshots.get(2));
		when(screenshotRepo.findNotProcessedEdge(any(Integer.class))).thenReturn(notProcessed);

		/* test edge_1 skipping */
		when(processedImageRepo.exists(any(ProcessedImageKey.class))).then(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				ProcessedImageKey key = invocation.getArgumentAt(0, ProcessedImageKey.class);
				boolean b = (key != null &&
						key.getScreenshotId().intValue() == 1 &&
						key.getAlgorithm().equals("edge_0"));
				return b;
			}
		});
	}

	@Test
	public void TestCreation() throws InterruptedException
	{
		Repositories repo = new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo);
		CacheTaskQueue q = mock(CacheTaskQueue.class);
		BackgroundImageDispatcher d = new BackgroundImageDispatcher(repo, q);
		d.start();
		d.requestStop();
		q.interruptAndJoin();
		d.join();
	}

	@Test
	public void TestRun() throws InterruptedException
	{
		Repositories repo = new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo);
		CacheTaskQueue q = mock(CacheTaskQueue.class);
		BackgroundImageDispatcher d = new BackgroundImageDispatcher(repo, q);
		Semaphore sem = new Semaphore(0);
		Answer<Object> answer = new Answer<Object>() {
			private Semaphore sem;
			public Object answer(InvocationOnMock invocation) {
				Runnable r = invocation.getArgumentAt(0, Runnable.class);
				r.run();
				sem.release();
				return null;
			}
			public Answer<Object> setSemaphore(Semaphore sem) {
				this.sem = sem;
				return this;
			}
		}.setSemaphore(sem);
		doAnswer(answer).when(q).addTask(any(PrioritizedTask.class));
		d.start();
		sem.acquire();
		sem.acquire();
		d.requestStop();
		q.interruptAndJoin();
		d.join();
	}
}
