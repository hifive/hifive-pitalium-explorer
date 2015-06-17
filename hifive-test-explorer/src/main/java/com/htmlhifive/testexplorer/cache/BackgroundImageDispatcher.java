package com.htmlhifive.testexplorer.cache;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.htmlhifive.testexplorer.entity.ProcessedImage;
import com.htmlhifive.testexplorer.entity.ProcessedImageKey;
import com.htmlhifive.testexplorer.entity.Repositories;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.file.ImageFileUtility;
import com.htmlhifive.testexplorer.image.EdgeDetector;

public class BackgroundImageDispatcher extends Thread {
	private CacheTaskQueue taskQueue;

	private Repositories repositories;
	
	private final ImageFileUtility imageFileUtil;
	private volatile boolean stop = false; 

	public BackgroundImageDispatcher(Repositories repositories, CacheTaskQueue taskQueue)
	{
		this.repositories = repositories;
		this.imageFileUtil = new ImageFileUtility(repositories);
		this.taskQueue = taskQueue;
	}

	public void requestStop()
	{
		this.stop = true;
	}

	@Override
	public void run() {
		Integer lastIndex = -1;
		while(!this.stop)
		{
			List<Screenshot> toProcess = repositories.getScreenshotRepository().findNotProcessedEdge(lastIndex);
			for (Screenshot s : toProcess)
			{
				lastIndex = Math.max(s.getId(), lastIndex);
				for (int colorIndex = -1; colorIndex <= 2; colorIndex++)
				{
					String algorithm = ProcessedImageUtility.getAlgorithmNameForEdge(colorIndex);
					ProcessedImageKey key = new ProcessedImageKey(s.getId(), algorithm);
					if (repositories.getProcessedImageRepository().exists(key))
						continue;
					addTask(s, colorIndex, key);
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void addTask(Screenshot s, int colorIndex, ProcessedImageKey key)
	{
		taskQueue.addTask(new PrioritizedTask(0, new Runnable() {
			Screenshot s;
			int colorIndex;
			ProcessedImageKey key;

			public Runnable init(Screenshot s, int colorIndex, ProcessedImageKey key) {
				this.s = s;
				this.colorIndex = colorIndex;
				this.key = key;
				return this;
			}

			@Override
			public void run() {
				EdgeDetector edgeDetector = new EdgeDetector(0.5);

				switch (colorIndex) {
				case 0:
					edgeDetector.setForegroundColor(new Color(255, 0, 0, 255));
					break;
				case 1:
					edgeDetector.setForegroundColor(new Color(0, 0, 255, 255));
					break;
				}

				BufferedImage image;
				try {
					image = ImageIO.read(imageFileUtil.getFile(s));
				} catch (IOException e) {
					return;
				}
				BufferedImage edgeImage = edgeDetector.DetectEdge(image);
				String path = imageFileUtil.newProcessedFilePath(key);
				String absolutePath = imageFileUtil.getAbsoluteFilePath(path);
				try {
					File parentDirectory = new File(new File(absolutePath).getParent());
					parentDirectory.mkdirs();
					ImageIO.write(edgeImage, "png", new File(absolutePath));
					ProcessedImage newEntry = new ProcessedImage();
					newEntry.setScreenshotId(key.getScreenshotId());
					newEntry.setAlgorithm(key.getAlgorithm());
					newEntry.setFileName(path);
					repositories.getProcessedImageRepository().saveAndFlush(newEntry);
				} catch (IOException e) {
					/* merely skip caching */
				}
			}
		}.init(s, colorIndex, key)));
	}
}
