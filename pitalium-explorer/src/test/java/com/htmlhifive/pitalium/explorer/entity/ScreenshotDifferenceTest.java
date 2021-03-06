/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotDifference;

public class ScreenshotDifferenceTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		ScreenshotDifference sd = new ScreenshotDifference();
		Screenshot s = new Screenshot();
		sd.setScreenshot(s);
		sd.setType("marker");
		sd.setData("<marker blahblah>");

		Assert.assertEquals(s, sd.getScreenshot());

		sd = (ScreenshotDifference)new SerializeTestUtil().serializeAndDeserialize(sd);

		Assert.assertEquals("marker", sd.getType());
		Assert.assertEquals("<marker blahblah>", sd.getData());
	}
}
