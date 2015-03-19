package com.htmlhifive.testexplorer.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TestCaseResultRepository extends JpaRepository<TestCaseResult, Integer> {

	@Query("select r from TestCaseResult as r where expectedId = :expectedId")
	public List<TestCaseResult> find(@Param("expectedId") String expectedId);

	@Query("select r from TestCaseResult as r where expectedId like %:expectedId%")
	public List<TestCaseResult> findKeyword(@Param("expectedId") String expectedId);

	@Query("select r from TestCaseResult as r where executeTime between :start and :end")
	public List<TestCaseResult> findRange(@Param("start") String start, @Param("end") String end);
}
