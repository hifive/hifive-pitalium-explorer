/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.pitalium.explorer.entity.Area;
import com.htmlhifive.pitalium.explorer.entity.Config;
import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImage;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageRepository;
import com.htmlhifive.pitalium.explorer.entity.Repositories;
import com.htmlhifive.pitalium.explorer.entity.RepositoryMockCreator;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotRepository;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionRepository;
import com.htmlhifive.pitalium.explorer.service.ExplorerService;
import com.htmlhifive.pitalium.explorer.service.PersisterService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class ImageControllerTest {
	@Autowired
	private ImageController imageController;
	@Autowired
	private TestExecutionRepository testExecutionRepo;

	@Autowired
	private ScreenshotRepository screenshotRepo;

	@Autowired
	private ConfigRepository configRepo;

	@Autowired
	private ProcessedImageRepository processedImageRepo;

	private ArrayList<Config> configs;
	private ArrayList<Screenshot> screenshots;
	private ArrayList<TestExecution> testExecutions;
	private ArrayList<TestEnvironment> testEnvironments;

	// ImageControllerのserviceのバックアップ
	private ExplorerService explorerService;

	/**
	 * Initialize some mock objects for testing. This method is called before each test method.
	 */
	@Before
	public void initializeDefaultMockObjects() {
		RepositoryMockCreator r = new RepositoryMockCreator(new Repositories(configRepo, processedImageRepo,
				screenshotRepo, testExecutionRepo));
		configs = r.getConfigs();
		screenshots = r.getScreenshots();
		new ArrayList<ProcessedImage>();
		testExecutions = r.getTestExecutions();
		testEnvironments = r.getTestEnvironments();
	}

	@Before
	public void testInit() {
		// ImageControllerのserviceを各テストで置き換えるのでバックアップを取っておく
		// バックアップを戻さないとthis.imageController.destory()でエラーがでる
		explorerService = (ExplorerService) Whitebox.getInternalState(this.imageController, "service");
	}

	@Test
	public void testGetImageNotFound() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getImage(-1, 0, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetImageFileError() throws Exception {
		// setStatusが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// getImage(), getTarget()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenThrow(new IOException());

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したserviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getImage(0, 0, response);

		// setStatus()が引数HttpServletResponse.SC_INTERNAL_SERVER_ERRORで呼び出されたか確認
		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetImageOk() throws IOException {
		// response.getOutputStream()の戻り値用意
		// ServletOutputStreamはインスタンス化できないので、mockを返すようにする
		ServletOutputStream stream = mock(ServletOutputStream.class);

		// setContentTypeが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(stream);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// 戻り値を置き換えるためのdummyの画像ファイル
		File file = new File("src/test/resources/images/edge_detector_0.png");

		// 戻り値を置き換えるためのAreaなしのdummyのTarget
		Target target = new Target();
		target.setExcludeAreas(new ArrayList<Area>());

		// getImage(), getTarget()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenReturn(file);
		when(persisterService.getTarget(0, 0)).thenReturn(target);

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getImage(0, 0, response);

		// setContentType()が引数"image/png"で呼び出されたか確認
		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetDiffImageNotFoundSource() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getDiffImage(-1, 0, 0, 0, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetDiffImageNotFoundTarget() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getDiffImage(0, -1, 0, 0, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetDiffImageFileError() throws Exception {
		// setStatusが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// getImage()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenThrow(new IOException());

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getDiffImage(0, 1, 0, 0, response);

		// setStatus()が引数HttpServletResponse.SC_INTERNAL_SERVER_ERRORで呼び出されたか確認
		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetDiffImageOk() throws IOException {
		// response.getOutputStream()の戻り値用意
		// ServletOutputStreamはインスタンス化できないので、mockを返すようにする
		ServletOutputStream stream = mock(ServletOutputStream.class);

		// setContentTypeが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(stream);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// 戻り値を置き換えるためのdummyの画像ファイル
		File file = new File("src/test/resources/images/edge_detector_0.png");

		// 戻り値を置き換えるためのAreaなしのdummyのTarget
		Target target = new Target();
		target.setExcludeAreas(new ArrayList<Area>());

		// getImage(), getTarget()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenReturn(file);
		when(persisterService.getTarget(0, 0)).thenReturn(target);

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getDiffImage(0, 0, 0, 0, response);

		// setContentType()が引数"image/png"で呼び出されたか確認
		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetDiffImageOkDifferent() throws IOException {
		// response.getOutputStream()の戻り値用意
		// ServletOutputStreamはインスタンス化できないので、mockを返すようにする
		ServletOutputStream stream = mock(ServletOutputStream.class);

		// setContentTypeが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(stream);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// 戻り値を置き換えるためのdummyの画像ファイル
		File file1 = new File("src/test/resources/images/edge_detector_0.png");
		File file2 = new File("src/test/resources/images/edge_detector_0_edge.png");

		// 戻り値を置き換えるためのAreaなしのdummyのTarget
		Target target = new Target();
		target.setExcludeAreas(new ArrayList<Area>());

		// getImage(), getTarget()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenReturn(file1);
		when(persisterService.getImage(1, 0)).thenReturn(file2);
		when(persisterService.getTarget(0, 0)).thenReturn(target);

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getDiffImage(0, 1, 0, 0, response);

		// setContentType()が引数"image/png"で呼び出されたか確認
		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetProcessedNotFound() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getProcessed(-1, 0, "edge", -1, response);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetProcessedUnknownMethod() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		this.imageController.getProcessed(-1, 0, "aaaaaa", -1, response);
		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void testGetProcessedFileError() throws Exception {
		// setStatusが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// getImage()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenThrow(new IOException());

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getProcessed(0, 0, "edge", 0, response);

		// setStatus()が引数HttpServletResponse.SC_INTERNAL_SERVER_ERRORで呼び出されたか確認
		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetProcessedEdgeColorIndex0() throws IOException {
		// response.getOutputStream()の戻り値用意
		// ServletOutputStreamはインスタンス化できないので、mockを返すようにする
		ServletOutputStream stream = mock(ServletOutputStream.class);

		// setContentTypeが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(stream);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// 戻り値を置き換えるためのdummyの画像ファイル
		File file = new File("src/test/resources/images/edge_detector_0.png");

		// getImage()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenReturn(file);

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getProcessed(0, 0, "edge", 0, response);

		// setContentType()が引数"image/png"で呼び出されたか確認
		verify(response).setContentType("image/png");
	}

	@Test
	public void testGetProcessedEdgeColorIndex1() throws IOException {
		// response.getOutputStream()の戻り値用意
		// ServletOutputStreamはインスタンス化できないので、mockを返すようにする
		ServletOutputStream stream = mock(ServletOutputStream.class);

		// setContentTypeが呼ばれたかを確認するためmockにする
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(stream);

		// 戻り値を置き換えないのでmockにする必要がない
		ExplorerService service = new ExplorerService();

		// 戻り値を置き換えるためのdummyの画像ファイル
		File file = new File("src/test/resources/images/edge_detector_0.png");

		// getImage()の戻り値を置き換えるためmockにする
		PersisterService persisterService = mock(PersisterService.class);
		when(persisterService.getImage(0, 0)).thenReturn(file);

		// ExplorerServiceのprivateなfieldのpersisterServiceを用意したpersisterServiceで置き換える
		Whitebox.setInternalState(service, "persisterService", persisterService);

		// ImageControllerのprivateなfieldのserviceを用意したexplorerSeviceで置き換える
		Whitebox.setInternalState(this.imageController, "service", service);

		// 実行
		this.imageController.getProcessed(0, 0, "edge", 1, response);

		// setContentType()が引数"image/png"で呼び出されたか確認
		verify(response).setContentType("image/png");
	}

	// FIXME: キャッシュ対応が終わった後に復活させる
	//	@Test
	//	public void testGetDiffImageFileExists() throws IOException {
	//		HttpServletResponse response = mock(HttpServletResponse.class);
	//
	//		ExplorerPersister persister = mock(ExplorerFilePersister.class);
	//		when(TestResultManager.getInstance().getPersister()).thenReturn(persister);
	//		ImageController spy = spy(this.imageController);
	//
	//		doReturn("src/test/resources/images/edge_detector_0.png").when(spy.imageFileUtil).getAbsoluteFilePath(
	//				any(String.class));
	//		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
	//
	//		spy.getDiffImage(0, 0, 0, response);
	//
	//		verify(response).setContentType("image/png");
	//	}

	//	@Test
	//	public void testGetDiffImageFileDirectory() throws IOException {
	//		HttpServletResponse response = mock(HttpServletResponse.class);
	//		ImageController spy = spy(this.imageController);
	//		spy.imageFileUtil = spy(spy.imageFileUtil);
	//		doReturn("src/test/resources/images/").when(spy.imageFileUtil).getAbsoluteFilePath(any(String.class));
	//		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
	//
	//		spy.getDiffImage(0, 0, response);
	//
	//		verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	//	}

	@Test
	@After
	public void testCleanup() throws InterruptedException {
		// ImageControllerのserviceのバックアップを戻す
		Whitebox.setInternalState(this.imageController, "service", explorerService);

		/* must be ok to call multiple times */
		this.imageController.destory();
		this.imageController.destory();
		this.imageController.destory();
	}
}
