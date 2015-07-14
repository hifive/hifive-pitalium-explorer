/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ProcessedImageKeyTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		ProcessedImageKey p = new ProcessedImageKey();
		p.setScreenshotId(2);
		p.setAlgorithm("edge");

		Assert.assertEquals(2, p.getScreenshotId().intValue());

		p = (ProcessedImageKey)new SerializeTestUtil().serializeAndDeserialize(p);

		Assert.assertEquals("edge", p.getAlgorithm());
	}

}
