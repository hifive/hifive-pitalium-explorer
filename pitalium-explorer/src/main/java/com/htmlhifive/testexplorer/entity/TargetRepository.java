/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TargetRepository extends JpaRepository<Target, Integer> {

	public List<Target> findByScreenshotId(@Param("screenshotId") Integer screenshotId);
}
