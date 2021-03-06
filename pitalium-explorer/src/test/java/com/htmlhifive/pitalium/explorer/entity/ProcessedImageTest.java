/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.entity.ProcessedImage;

public class ProcessedImageTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		ProcessedImage p = new ProcessedImage();
		p.setScreenshotId(2);
		p.setAlgorithm("edge");
		p.setFileName("TEST FILE");

		Assert.assertEquals(2, p.getScreenshotId().intValue());

		p = (ProcessedImage)new SerializeTestUtil().serializeAndDeserialize(p);

		Assert.assertEquals("edge", p.getAlgorithm());
		Assert.assertEquals("TEST FILE", p.getFileName());
	}
}
