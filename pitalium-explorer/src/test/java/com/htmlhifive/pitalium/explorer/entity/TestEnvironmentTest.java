/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;

public class TestEnvironmentTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		TestEnvironment t = new TestEnvironment();

		t.setId(7357);
		t.setLabel("TEST LABEL");
		t.setPlatform("TEST PLATFORM");
		t.setPlatformVersion("TEST PLATFORM VERSION");
		t.setDeviceName("TEST DEVICE NAME");
		t.setBrowserName("TEST BROWSER NAME");
		t.setBrowserVersion("TEST BROWSER VERSION");

		Assert.assertEquals(7357, t.getId().intValue());
		Assert.assertEquals("TEST LABEL", t.getLabel());
		Assert.assertEquals("TEST PLATFORM", t.getPlatform());
		Assert.assertEquals("TEST PLATFORM VERSION", t.getPlatformVersion());
		Assert.assertEquals("TEST DEVICE NAME", t.getDeviceName());
		Assert.assertEquals("TEST BROWSER NAME", t.getBrowserName());
		Assert.assertEquals("TEST BROWSER VERSION", t.getBrowserVersion());

		t = (TestEnvironment)new SerializeTestUtil().serializeAndDeserialize(t);

		Assert.assertEquals(7357, t.getId().intValue());
		Assert.assertEquals("TEST LABEL", t.getLabel());
		Assert.assertEquals("TEST PLATFORM", t.getPlatform());
		Assert.assertEquals("TEST PLATFORM VERSION", t.getPlatformVersion());
		Assert.assertEquals("TEST DEVICE NAME", t.getDeviceName());
		Assert.assertEquals("TEST BROWSER NAME", t.getBrowserName());
		Assert.assertEquals("TEST BROWSER VERSION", t.getBrowserVersion());
	}
}
