/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

package com.htmlhifive.pitalium.explorer.service;

import com.htmlhifive.pitalium.explorer.io.ExplorerPersister;

public interface PersisterService extends ExplorerPersister {

	ExplorerPersister getPersister();

}
