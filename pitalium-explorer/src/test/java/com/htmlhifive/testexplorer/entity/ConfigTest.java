/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.entity.Config;

public class ConfigTest {
	@Test
	public void testGetterSetter()
	{
		Config c = new Config();
		c.setKey("TEST KEY");
		c.setValue("TEST VALUE");
		Assert.assertEquals("TEST KEY", c.getKey());
		Assert.assertEquals("TEST VALUE", c.getValue());
	}

	@Test
	public void testSerialization() throws IOException, ClassNotFoundException
	{
		Config c = new Config();
		c.setKey("TEST KEY");
		c.setValue("TEST VALUE");

		c = (Config)new SerializeTestUtil().serializeAndDeserialize(c);

		Assert.assertEquals("TEST KEY", c.getKey());
		Assert.assertEquals("TEST VALUE", c.getValue());
	}
}
