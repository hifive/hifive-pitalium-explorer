package com.htmlhifive.testexplorer.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ResultRepository extends JpaRepository<Result, Integer> {

	@Query("select r from Result as r where expectedId = :expectedId")
	public List<Result> find(@Param("expectedId") String expectedId);

	@Query("select r from Result as r where expectedId like %:expectedId%")
	public List<Result> findKeyword(@Param("expectedId") String expectedId);

	@Query("select r from Result as r where executeTime between :start and :end")
	public List<Result> findRange(@Param("start") String start, @Param("end") String end);
}
