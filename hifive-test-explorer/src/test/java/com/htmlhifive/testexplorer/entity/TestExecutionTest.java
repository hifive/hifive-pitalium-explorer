package com.htmlhifive.testexplorer.entity;

import java.io.IOException;
import java.sql.Timestamp;

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
}
