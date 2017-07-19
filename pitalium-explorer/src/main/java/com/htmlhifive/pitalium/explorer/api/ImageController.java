/*
 * Copyright (C) 2015-2017 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htmlhifive.pitalium.explorer.api;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.htmlhifive.pitalium.explorer.cache.BackgroundImageDispatcher;
import com.htmlhifive.pitalium.explorer.cache.CacheTaskQueue;
import com.htmlhifive.pitalium.explorer.service.ExplorerService;
import com.htmlhifive.pitalium.explorer.service.TemporaryFileService;

@Controller
public class ImageController {
	@Autowired
	private ExplorerService service;

	@Autowired
	private TemporaryFileService temporaryFileService;

	private CacheTaskQueue cacheTaskQueue;
	private BackgroundImageDispatcher backgroundImageDispatcher;

	/**
	 * This method is called by spring after auto wiring. Do initialization here.
	 */
	@PostConstruct
	public void init() {
		if (service.getApplicationConfig().isDiffImageCacheOn()) {
			this.cacheTaskQueue = new CacheTaskQueue();
			this.backgroundImageDispatcher = new BackgroundImageDispatcher(service.getRepositories(), cacheTaskQueue);
			/* start background worker */
			this.backgroundImageDispatcher.start();
		}
	}

	/**
	 * This method is called when the application is about to die. Cleanup things.
	 *
	 * @throws InterruptedException
	 */
	@PreDestroy
	public void destory() throws InterruptedException {
		if (service.getApplicationConfig().isDiffImageCacheOn()) {
			this.backgroundImageDispatcher.requestStop();
			this.cacheTaskQueue.interruptAndJoin();
			this.backgroundImageDispatcher.join();
		}
	}

	/**
	 * Get the image from id.
	 *
	 * @param screenshotId screenshot id
	 * @param targetId id of the target area to be used for image comparison
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "image", method = RequestMethod.GET)
	public void getImage(@RequestParam(value = "screenshotId") Integer screenshotId,
			@RequestParam(value = "targetId") Integer targetId, HttpServletResponse response) {
		service.getImage(screenshotId, targetId, response);
	}

	/**
	 * Get edge detection result of an image.
	 *
	 * @param screenshotId id of screenshot to be processed by edge detector.
	 * @param targetId id of the target area to be used for image comparison
	 * @param colorIndex edge color index
	 * @param response HttpServletResponse
	 */
	public void getEdgeImage(Integer screenshotId, Integer targetId, int colorIndex, HttpServletResponse response) {
		service.getEdgeImage(screenshotId, targetId, colorIndex, response);
	}

	/**
	 * Get processed image.
	 *
	 * @param screenshotId id of an image to be processed
	 * @param targetId id of the target area to be used for image comparison
	 * @param algorithm currently only "edge" is supported
	 * @param colorIndex edge color index
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "image/processed", method = RequestMethod.GET)
	public void getProcessed(@RequestParam(value = "screenshotId") Integer screenshotId,
			@RequestParam(value = "targetId") Integer targetId, @RequestParam(value = "algorithm") String algorithm,
			@RequestParam(value = "colorIndex") int colorIndex, HttpServletResponse response) {
		service.getProcessed(screenshotId, targetId, algorithm, colorIndex, response);
	}

	/**
	 * Get the diff image with a marker of comparison result. If there is no difference, return normal image.
	 *
	 * @param sourceScreenshotId comparison source image id
	 * @param targetScreenshotId comparison target image id
	 * @param sourceTargetId id of the target area of source image to be used for image comparison
	 * @param targetTargetId id of the target area of target image
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "image/diff", method = RequestMethod.GET)
	public void getDiffImage(@RequestParam(value = "sourceScreenshotId") Integer sourceScreenshotId,
			@RequestParam(value = "targetScreenshotId") Integer targetScreenshotId,
			@RequestParam(value = "sourceTargetId") Integer sourceTargetId,
			@RequestParam(value = "targetTargetId") Integer targetTargetId, HttpServletResponse response) {
		service.getDiffImage(sourceScreenshotId, targetScreenshotId, sourceTargetId, targetTargetId, response);
	}

	@RequestMapping(value = "comparisonResult", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public Boolean getComparisonResult(@RequestParam(value = "sourceScreenshotId") Integer sourceScreenshotId,
			@RequestParam(value = "targetScreenshotId") Integer targetScreenshotId,
			@RequestParam(value = "sourceTargetId") Integer sourceTargetId,
			@RequestParam(value = "targetTargetId") Integer targetTargetId) {
		return service.getComparisonResult(sourceScreenshotId, targetScreenshotId, sourceTargetId, targetTargetId);
	}

	@RequestMapping(value = "files/upload", method = RequestMethod.POST)
	@ResponseBody
	public List<Integer> multipartUpload(@RequestParam("files") List<MultipartFile> files) throws Exception {
		return temporaryFileService.upload(files);
	}

	/**
	 * 設定されているresultsフォルダのキーのリストを取得する。
	 *
	 * @return 設定されているresultsフォルダのキーのリスト
	 */
	@RequestMapping(value = "directoryKeys/list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public ResponseEntity<List<String>> listResultDirectoryKeys() {
		List<String> list = service.listResultDirectoryKeys();
		return new ResponseEntity<List<String>>(list, HttpStatus.OK);
	}


	@Deprecated
	@RequestMapping(value = "files/diff", method = RequestMethod.GET)
	public void getDiffImage(@RequestParam("file1") String fileName1, @RequestParam("file2") String fileName2,
			HttpServletResponse response) throws Exception {
		service.getDiffImage(fileName1, fileName2, response);
	}

}
