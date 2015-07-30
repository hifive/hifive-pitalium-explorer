/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.testexplorer.cache.BackgroundImageDispatcher;
import com.htmlhifive.testexplorer.cache.CacheTaskQueue;
import com.htmlhifive.testexplorer.service.ExplorerService;

@Controller
@RequestMapping("/image")
public class ImageController {
	@Autowired
	private ExplorerService service;

	private CacheTaskQueue cacheTaskQueue;
	private BackgroundImageDispatcher backgroundImageDispatcher;

	/**
	 * This method is called by spring after auto wiring.
	 *
	 * Do initialization here.
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
	 * This method is called when the application is about to die.
	 * 
	 * Cleanup things.
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
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public void getImage(@RequestParam Integer screenshotId, @RequestParam Integer targetId, 
			HttpServletResponse response) {
		service.getImage(screenshotId, response);
	}

	/**
	 * Get edge detection result of an image.
	 *
	 * @param screenshotId id of screenshot to be processed by edge detector.
	 * @param targetId id of the target area to be used for image comparison
	 * @param allparams all parameters received by API
	 * @param response HttpServletResponse
	 */
	public void getEdgeImage(Integer screenshotId, Integer targetId, Map<String, String> allparams, 
			HttpServletResponse response) {
		service.getEdgeImage(screenshotId, allparams, response);
	}

	/**
	 * Get processed image.
	 * 
	 * @param screenshotId id of an image to be processed  
	 * @param targetId id of the target area to be used for image comparison
	 * @param algorithm currently only "edge" is supported
	 * @param allparams received all parameters
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getProcessed", method = RequestMethod.GET)
	public void getProcessed(@RequestParam Integer screenshotId, @RequestParam Integer targetId, 
			@RequestParam String algorithm, @RequestParam Map<String, String> allparams, 
			HttpServletResponse response) {
		service.getProcessed(screenshotId, algorithm, allparams, response);
	}

	/**
	 * Get the diff image with a marker of comparison result. If there is no difference, return normal image.
	 *
	 * @param sourceSceenshotId comparison source image id
	 * @param targetScreenshotId comparison target image id
	 * @param targetId id of the target area to be used for image comparison
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getDiff", method = RequestMethod.GET)
	public void getDiffImage(@RequestParam Integer sourceSceenshotId, 
			@RequestParam Integer targetScreenshotId, @RequestParam Integer targetId, 
			HttpServletResponse response) {
		service.getDiffImage(sourceSceenshotId, targetScreenshotId, response);
	}
}
