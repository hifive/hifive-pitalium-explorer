/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium-explorer.cache;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.cache.PrioritizedTask;

public class PrioritizedTaskTest {
	@Test
	public void testGet()
	{
		PrioritizedTask t = new PrioritizedTask(0, mock(Runnable.class));
		Assert.assertEquals(0, t.getPriority());
	}
	@Test
	public void testComparison()
	{
		Runnable r = mock(Runnable.class);
		PrioritizedTask t1 = new PrioritizedTask(0, r);
		PrioritizedTask t2 = new PrioritizedTask(1, r);
		PrioritizedTask t3 = new PrioritizedTask(1, r);
		Assert.assertEquals(Integer.compare(1,0), t1.compareTo(t2));
		Assert.assertEquals(Integer.compare(0,1), t2.compareTo(t1));
		Assert.assertEquals(1, t2.compareTo(null));
		Assert.assertEquals(-1, t2.compareTo(t3));
		Assert.assertEquals(1, t3.compareTo(t2));
		Assert.assertEquals(0, t2.compareTo(t2));
	}
}
