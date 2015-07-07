package com.htmlhifive.testexplorer.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedImageRepository extends JpaRepository<ProcessedImage, ProcessedImageKey> {
}