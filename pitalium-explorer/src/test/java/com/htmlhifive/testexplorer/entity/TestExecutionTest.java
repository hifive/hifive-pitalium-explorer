/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class TestExecutionTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		TestExecution t = new TestExecution();
		Timestamp timestamp = new Timestamp(13371337);

		t.setId(7357);
		t.setLabel("TEST LABEL");
		t.setTime(timestamp);

		Assert.assertEquals(7357, t.getId().intValue());
		Assert.assertEquals("TEST LABEL", t.getLabel());
		Assert.assertEquals(13371337, t.getTime().getTime());

		Assert.assertEquals("1970_01_01_12_42_51", t.getTimeString());

		java.util.Date now = new java.util.Date();
		t.setTime(new Timestamp(now.getTime()));
		Assert.assertEquals(0, t.getTime().compareTo(now));

		t = (TestExecution)new SerializeTestUtil().serializeAndDeserialize(t);

		Assert.assertEquals(7357, t.getId().intValue());
		Assert.assertEquals("TEST LABEL", t.getLabel());
		Assert.assertEquals(0, t.getTime().compareTo(now));
	}

	@Test
	public void testEquality()
	{
		TestExecution t1 = new TestExecution();
		TestExecution t2 = new TestExecution();
		t1.setId(42);
		t2.setId(43);
		Assert.assertFalse(t1.equals(t2));
		Assert.assertFalse(t2.equals(t1));
		Assert.assertFalse(t1.equals(42));
		Assert.assertFalse(t1.equals(null));
		Assert.assertTrue(t1.equals(t1));
		t2.setId(42);
		Assert.assertTrue(t1.equals(t2));
		Assert.assertTrue(t2.equals(t1));
	}

	@Test
	public void testHashCode()
	{
		TestExecution t1 = new TestExecution();
		TestExecution t2 = new TestExecution();
		t1.setId(42);
		t2.setId(43);
		HashSet<TestExecution> hs = new HashSet<TestExecution>();
		hs.add(t1);
		hs.add(t2);
	}
}
