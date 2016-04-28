/*
 *  Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

package com.htmlhifive.pitalium.explorer.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScreenshotIdServiceImpl implements ScreenshotIdService {

	// FIXME 採番方法がかなり適当な実装
	List<ScreenshotType> types = new ArrayList<>();

	@Override
	public synchronized int nextId(ScreenshotType type) {
		int id = types.size();
		types.add(type);
		return id;
	}

	@Override
	public synchronized ScreenshotType getScreenshotType(int id) {
		if (id < 0 || types.size() <= id)
			throw new IllegalArgumentException();

		return types.get(id);
	}

}
