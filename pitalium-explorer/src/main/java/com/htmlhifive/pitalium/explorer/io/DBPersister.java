/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.io;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.htmlhifive.pitalium.core.io.PersistMetadata;
import com.htmlhifive.pitalium.core.io.Persister;
import com.htmlhifive.pitalium.core.io.ResourceUnavailableException;
import com.htmlhifive.pitalium.core.model.TargetResult;
import com.htmlhifive.pitalium.core.model.TestResult;

public class DBPersister implements Persister {

	@Override
	public void saveDiffImage(PersistMetadata metadata, BufferedImage image) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedImage loadDiffImage(PersistMetadata metadata) throws ResourceUnavailableException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveScreenshot(PersistMetadata metadata, BufferedImage image) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getImageStream(PersistMetadata metadata) throws ResourceUnavailableException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedImage loadScreenshot(PersistMetadata metadata) throws ResourceUnavailableException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveTargetResults(PersistMetadata metadata, List<TargetResult> results) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public List<TargetResult> loadTargetResults(PersistMetadata metadata) throws ResourceUnavailableException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveTestResult(PersistMetadata metadata, TestResult result) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public TestResult loadTestResult(PersistMetadata metadata) throws ResourceUnavailableException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveExpectedIds(Map<String, Map<String, String>> expectedIds) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Map<String, String>> loadExpectedIds() throws ResourceUnavailableException {
		// FIXME
		return new HashMap<>();
	}

}
