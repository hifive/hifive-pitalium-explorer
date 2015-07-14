/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ScreenshotTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		Screenshot s = new Screenshot();
		Screenshot s2 = new Screenshot();
		TestEnvironment tenv = new TestEnvironment();

		s.setId(7357);
		s.setExpectedScreenshot(s2);
		s.setFileName("TEST FILE NAME");
		s.setComparisonResult(true);
		s.setTestClass("TEST CLASS");
		s.setTestMethod("TEST METHOD");
		s.setTestScreen("TEST SCREEN");
		s.setTestExecutionId(111);
		s.setTestEnvironment(tenv);

		Assert.assertEquals(7357, s.getId().intValue());
		Assert.assertEquals(s2, s.getExpectedScreenshot());
		Assert.assertEquals("TEST FILE NAME", s.getFileName());
		Assert.assertEquals(true, s.getComparisonResult());
		Assert.assertEquals("TEST CLASS", s.getTestClass());
		Assert.assertEquals("TEST METHOD", s.getTestMethod());
		Assert.assertEquals("TEST SCREEN", s.getTestScreen());
		Assert.assertEquals(111, s.getTestExecutionId().intValue());
		Assert.assertEquals(tenv, s.getTestEnvironment());

		s = (Screenshot)new SerializeTestUtil().serializeAndDeserialize(s);

		Assert.assertEquals(7357, s.getId().intValue());
		Assert.assertEquals("TEST FILE NAME", s.getFileName());
		Assert.assertEquals(true, s.getComparisonResult());
		Assert.assertEquals("TEST CLASS", s.getTestClass());
		Assert.assertEquals("TEST METHOD", s.getTestMethod());
		Assert.assertEquals("TEST SCREEN", s.getTestScreen());
	}
}
