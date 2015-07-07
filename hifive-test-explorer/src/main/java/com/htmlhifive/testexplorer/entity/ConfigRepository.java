package com.htmlhifive.testexplorer.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, String> {

	public static String ABSOLUTE_PATH_KEY = "absolutePath";

}
