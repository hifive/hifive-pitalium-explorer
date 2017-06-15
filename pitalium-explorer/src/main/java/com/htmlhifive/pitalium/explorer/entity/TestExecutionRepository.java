/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;

public interface TestExecutionRepository extends JpaRepository<TestExecution, Integer> {

	@Query("SELECT NEW com.htmlhifive.pitalium.explorer.response.TestExecutionResult ( " + "te, "
			+ "SUM(CASE WHEN s.comparisonResult = TRUE THEN 1 ELSE 0 END), " + "COUNT(s.id) "
			+ ") FROM TestExecution AS te, Screenshot AS s " + "WHERE te.id = s.testExecution "
			+ "AND s.testMethod LIKE %:testMethod% " + "AND s.testScreen LIKE %:testScreen% " + "GROUP BY te.id ")
	public Page<TestExecutionResult> search(@Param("testMethod") String testMethod,
			@Param("testScreen") String testScreen, Pageable page);

}
