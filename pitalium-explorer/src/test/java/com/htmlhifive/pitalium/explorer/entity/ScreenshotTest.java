/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;

public class ScreenshotTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		Screenshot s = new Screenshot();
		Screenshot s2 = new Screenshot();
		TestEnvironment tenv = new TestEnvironment();
		TestExecution texe = new TestExecution();

		s.setId(7357);
		s.setExpectedScreenshotId(s2.getId());
		s.setFileName("TEST FILE NAME");
		s.setComparisonResult(true);
		s.setTestClass("TEST CLASS");
		s.setTestMethod("TEST METHOD");
		s.setTestScreen("TEST SCREEN");
		s.setTestExecution(texe);
		s.setTestEnvironment(tenv);

		Assert.assertEquals(7357, s.getId().intValue());
		Assert.assertEquals(s2.getId(), s.getExpectedScreenshotId());
		Assert.assertEquals("TEST FILE NAME", s.getFileName());
		Assert.assertEquals(true, s.getComparisonResult());
		Assert.assertEquals("TEST CLASS", s.getTestClass());
		Assert.assertEquals("TEST METHOD", s.getTestMethod());
		Assert.assertEquals("TEST SCREEN", s.getTestScreen());
		Assert.assertEquals(texe, s.getTestExecution());
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
