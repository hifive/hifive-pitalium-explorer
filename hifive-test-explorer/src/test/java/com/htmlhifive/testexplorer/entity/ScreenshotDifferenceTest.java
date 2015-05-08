package com.htmlhifive.testexplorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

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
