/*
 *  Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */

package com.htmlhifive.pitalium.explorer.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScreenshotIdServiceImplTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	ScreenshotIdService service;

	@Before
	public void setup() throws Exception {
		service = new ScreenshotIdServiceImpl();
	}

	@Test
	public void nextId() throws Exception {
		int firstId = service.nextId(ScreenshotIdService.ScreenshotType.PITALIUM_FILE);
		assertThat(firstId, is(0));
		assertThat(service.getScreenshotType(firstId), is(ScreenshotIdService.ScreenshotType.PITALIUM_FILE));

		int secondId = service.nextId(ScreenshotIdService.ScreenshotType.TEMPORARY_FILE);
		assertThat(secondId, is(1));
		assertThat(service.getScreenshotType(firstId), is(ScreenshotIdService.ScreenshotType.PITALIUM_FILE));
		assertThat(service.getScreenshotType(secondId), is(ScreenshotIdService.ScreenshotType.TEMPORARY_FILE));

		int thirdId = service.nextId(ScreenshotIdService.ScreenshotType.PITALIUM_FILE);
		assertThat(thirdId, is(2));
		assertThat(service.getScreenshotType(thirdId), is(ScreenshotIdService.ScreenshotType.PITALIUM_FILE));
	}

	@Test
	public void getScreenshotType_illegal_under() throws Exception {
		expected.expect(IllegalArgumentException.class);
		service.getScreenshotType(-1);
	}

	@Test
	public void getScreenshotType_illegal_over() throws Exception {
		service.nextId(ScreenshotIdService.ScreenshotType.PITALIUM_FILE);
		service.getScreenshotType(0);

		expected.expect(IllegalArgumentException.class);
		service.getScreenshotType(1);
	}

}