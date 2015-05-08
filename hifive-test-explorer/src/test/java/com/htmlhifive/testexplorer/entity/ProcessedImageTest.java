package com.htmlhifive.testexplorer.entity;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ProcessedImageTest {
	@Test
	public void testGetterSetter() throws ClassNotFoundException, IOException
	{
		ProcessedImage p = new ProcessedImage();
		Screenshot s = new Screenshot();
		p.setScreenshot(s);
		p.setAlgorithm("edge");
		p.setFileName("TEST FILE");

		Assert.assertEquals(s, p.getScreenshot());

		p = (ProcessedImage)new SerializeTestUtil().serializeAndDeserialize(p);

		Assert.assertEquals("edge", p.getAlgorithm());
		Assert.assertEquals("TEST FILE", p.getFileName());
	}
}
