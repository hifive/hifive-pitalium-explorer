package com.htmlhifive.testexplorer.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScreenshotRepository extends JpaRepository<Screenshot, Integer> {

	@Query("select s from Screenshot as s, TestCaseResult as r "
			+ "where r.executeTime = :executeTime and s.testCaseResult = r.id "
			+ "order by s.result asc")
	public List<Screenshot> find(@Param("executeTime") String executeTime);
}
