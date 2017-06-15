/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.cache;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.cache.CacheTaskQueue;
import com.htmlhifive.pitalium.explorer.cache.PrioritizedTask;

public class CacheTaskQueueTest {
	@Test
	public void TestCreation() throws InterruptedException
	{
		CacheTaskQueue q = new CacheTaskQueue();
		q.interruptAndJoin();
	}
	@Test
	public void TestRun() throws InterruptedException
	{
		CacheTaskQueue q = new CacheTaskQueue();
		Semaphore s = new Semaphore(0);
		q.addTask(new PrioritizedTask(0, new Runnable() {
			private Semaphore sem;
			@Override
			public void run() {
				sem.release();
			}
			public Runnable setSemaphore(Semaphore sem)
			{
				this.sem = sem;
				return this;
			}
		}.setSemaphore(s)));
		q.addTask(new PrioritizedTask(0, new Runnable() {
			private Semaphore sem;
			@Override
			public void run() {
				sem.release();
			}
			public Runnable setSemaphore(Semaphore sem)
			{
				this.sem = sem;
				return this;
			}
		}.setSemaphore(s)));
		s.acquire();
		s.acquire();
		Assert.assertFalse(s.tryAcquire(100, TimeUnit.MILLISECONDS));
		q.interruptAndJoin();
	}
}
