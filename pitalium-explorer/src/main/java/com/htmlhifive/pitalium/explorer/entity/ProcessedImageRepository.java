/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ProcessedImageRepository extends JpaRepository<ProcessedImage, ProcessedImageKey> {
}