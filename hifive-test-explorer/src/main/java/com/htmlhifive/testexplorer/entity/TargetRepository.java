/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TargetRepository extends JpaRepository<Target, Integer> {

	@Query("select "
			+ "new com.htmlhifive.testexplorer.entity.Target (t, a) "
			+ "from Target as t, Area as a "
			+ "where t.targetId = a.targetId and t.screenshotId = :screenshotId and a.excluded = false "
			+ "order by t.targetId")
	public List<Target> find(@Param("screenshotId") Integer screenshotId);

}
