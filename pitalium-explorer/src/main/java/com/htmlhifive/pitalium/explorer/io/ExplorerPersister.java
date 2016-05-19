/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.htmlhifive.pitalium.explorer.service.ScreenshotIdService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.htmlhifive.pitalium.core.io.Persister;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.image.ComparedRectangle;
import com.htmlhifive.pitalium.explorer.response.Result;
import com.htmlhifive.pitalium.explorer.response.ResultDirectory;
import com.htmlhifive.pitalium.explorer.response.ResultListOfExpected;
import com.htmlhifive.pitalium.explorer.response.ScreenshotFile;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;

public interface ExplorerPersister extends Persister {

	int defaultPageSize = 20;

	void setScreenshotIdService(ScreenshotIdService screenshotIdService);
	
	/**
	 * Get the list of sub-directories under 'results' directory.
	 * 
	 * @param searchTestMethod
	 * @param searchTestScreen
	 * @param page
	 * @param pageSize
	 * @param refresh 
	 * @return Directories under 'results' folder
	 */
	Page<ResultDirectory> findResultDirectory(String searchTestMethod, String searchTestScreen, int page, int pageSize, boolean refresh);
	
	/**
	 * Get the screenshot files under selected sub-directory of 'results' folder
	 * 
	 * @param name2 
	 * @return Screenshot files under subdirectory of 'results' folder
	 */
	Map<String, List> findScreenshotFiles(String path, boolean refresh);

	ResultListOfExpected executeComparing(String path, String expectedFilename, String[] targetFilenames);
	Map<String, byte[]> getImages(String path, String expectedFilename, String targetFilename);
	List<ComparedRectangle> getComparedResult(String path, int resultListId, int targetResultId);

	/**
	 * TestExecutionのリストを取得する。 引数のメソッド名、スクリーンショットを含む（like検索）Screenshotを持つ TestExecutionのリストを取得する。
	 * 
	 * @param searchTestMethod メソッド名
	 * @param searchTestScreen スクリーンショット
	 * @param page 表示ページ番号
	 * @param pageSize 1ページあたりの表示数
	 * @return TestExecutionのリスト
	 */
	Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page,
			int pageSize);

	/**
	 * Screenshotのリストを取得する。 引数のメソッド名、スクリーンショットを含む（like検索）Screenshotのリストを取得する。
	 * 
	 * @param testExecutionId テスト実行ID
	 * @param searchTestMethod メソッド名
	 * @param searchTestScreen スクリーンショット
	 * @return Screenshotのリスト
	 */
	List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen);

	/**
	 * Screenshotを取得する。
	 * 
	 * @param screenshotid スクリーンショットID
	 * @return Screenshot
	 */
	Screenshot getScreenshot(Integer screenshotid);

	/**
	 * Targetを取得する。
	 * 
	 * @param screenshotId スクリーンショットID
	 * @param targetId 比較対象のID
	 * @return Target
	 */
	Target getTarget(Integer screenshotId, Integer targetId);

	/**
	 * 画像ファイルを取得する。
	 * 
	 * @param screenshotId スクリーンショットID
	 * @param targetId 比較対象のID
	 * @return 画像ファイル
	 * @throws IOException
	 */
	File getImage(Integer screenshotId, Integer targetId) throws IOException;

	/**
	 * Screenshotのリストを取得する。 引数のテスト実行ID、テスト環境IDと一致するScreenshotのリストを取得する。
	 * 
	 * @param testExecutionId テスト実行ID
	 * @param testEnvironmentId テスト環境ID
	 * @param page 表示ページ番号
	 * @param pageSize 1ページあたりの表示数
	 * @return Screenshotのリスト
	 */
	Page<Screenshot> findScreenshot(Integer testExecutionId, Integer testEnvironmentId, int page, int pageSize);

	/**
	 * TestExecutionAndEnvironmentのリストを取得する。
	 * 
	 * @param page 表示ページ番号
	 * @param pageSize 1ページあたりの表示数
	 * @return TestExecutionAndEnviromentのリスト
	 */
	Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(int page, int pageSize);

	File searchProcessedImageFile(Integer screenshotId, String algorithm);

	List<Screenshot> findNotProcessedEdge();

	boolean exsitsProcessedImage(Integer screenshotId, String algorithm);

	String getEdgeFileName(Integer screenshotId, String algorithm);

	void saveProcessedImage(Integer screenshotId, String algorithm, String edgeFileName);



	
}
