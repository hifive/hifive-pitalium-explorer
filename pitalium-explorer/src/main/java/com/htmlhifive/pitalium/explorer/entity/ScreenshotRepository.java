/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScreenshotRepository extends JpaRepository<Screenshot, Integer> {

	public List<Screenshot> findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(
			Integer testExecutionId, String testMethod, String testScreen);

	@Query("SELECT s " + "FROM Screenshot AS s " + "WHERE s.id > :from " + "AND 3 > (" + "SELECT COUNT(*) "
			+ "FROM ProcessedImage AS p " + "WHERE s.id = p.screenshotId " + "AND ( " + "p.algorithm = 'edge' "
			+ "OR p.algorithm = 'edge_0' " + "OR p.algorithm = 'edge_1' )) " + "ORDER BY s.id desc" /*
																									 * from recent to
																									 * older
																									 */
	)
	public List<Screenshot> findNotProcessedEdge(@Param("from") Integer from);

	public Page<Screenshot> findByTestExecutionIdAndTestEnvironmentId(@Param("testExecutionId") Integer testExecutionId, 
			@Param("testEnvironmentId") Integer testEnvironmentId, Pageable page);

	public long countByTestExecutionIdAndTestEnvironmentId(@Param("testExecutionId") Integer testExecutionId, 
			@Param("testEnvironmentId") Integer testEnvironmentId);

	@Query("select distinct new com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment( "
			+ "exe.id, exe.time, env.id, env.platform, env.platformVersion, env.deviceName, "
			+ "env.browserName, env.browserVersion ) from Screenshot as s, TestExecution as exe, TestEnvironment as env "
			+ "where s.testExecution = exe.id and s.testEnvironment = env.id "
			+ "order by exe.id asc, env.id asc")
	public Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(Pageable page);

}
