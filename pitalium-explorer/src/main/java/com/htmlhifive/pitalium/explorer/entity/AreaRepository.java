/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AreaRepository extends JpaRepository<Area, Integer> {

	public List<Area> findByTargetId(@Param("targetId") Integer targetId);

	public Area getByTargetIdAndExcluded(@Param("targetId") Integer targetId, @Param("excluded") Boolean excluded);
}
