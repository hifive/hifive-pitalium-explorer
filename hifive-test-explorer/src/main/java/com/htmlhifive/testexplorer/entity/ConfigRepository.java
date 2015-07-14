/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, String> {

	public static String ABSOLUTE_PATH_KEY = "absolutePath";

}
