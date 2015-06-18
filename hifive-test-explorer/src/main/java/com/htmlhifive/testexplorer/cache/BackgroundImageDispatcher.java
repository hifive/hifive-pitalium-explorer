package com.htmlhifive.testexplorer.cache;

import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	/**
	 * The constructor
	 * 
	 * @param repositories 
	 * @param taskQueue This dispatcher will send jobs to the specified taskQueue.
	 */
	public BackgroundImageDispatcher(Repositories repositories, CacheTaskQueue taskQueue)
	{
		this.repositories = repositories;
		this.imageFileUtil = new ImageFileUtility(repositories);
		this.taskQueue = taskQueue;
	}

	/**
	 * Set stop flag. The thread will stop soon.
	 */
	public void requestStop()
	{
		this.stop = true;
		this.interrupt();
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
				ArrayList<Integer> colorIndices = new ArrayList<Integer>();
				for (int colorIndex = -1; colorIndex <= 1; colorIndex++)
				{
					String algorithm = ProcessedImageUtility.getAlgorithmNameForEdge(colorIndex);
					ProcessedImageKey key = new ProcessedImageKey(s.getId(), algorithm);
					if (repositories.getProcessedImageRepository().exists(key))
						continue;
					colorIndices.add(colorIndex);
				}
				if (colorIndices.isEmpty())
					continue;
				addTask(s, colorIndices);
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/**
	 * Create and add edge processing task.
	 *  
	 * @param s screenshot to be processed 
	 * @param colorIndex the colorIndex to be processed
	 * @param key the key for ProcessedImage class.
	 */
	private void addTask(Screenshot s, ArrayList<Integer> colorIndices)
	{
		taskQueue.addTask(new PrioritizedTask(0, new Runnable() {
			Screenshot s;
			ArrayList<Integer> colorIndices;

			public Runnable init(Screenshot s, ArrayList<Integer> colorIndices) {
				this.s = s;
				this.colorIndices = colorIndices;
				return this;
			}

			public LookupOp getBlackToRedOp()
			{
				short[] red = new short[256];
				short[] id = new short[256];
				for (int i = 0; i < 256; i++) {
					red[i] = 255;
					id[i] = (short)i;
				}
				return new LookupOp(new ShortLookupTable(0, new short[][] { red, id, id, id }), null);
			}

			public LookupOp getBlackToBlueOp()
			{
				short[] id= new short[256];
				short[] blue = new short[256];
				for (int i = 0; i < 256; i++) {
					id[i] = (short)i;
					blue[i] = 255;
				}
				return new LookupOp(new ShortLookupTable(0, new short[][] { id, id, blue, id }), null);
			}

			@Override
			public void run() {
				EdgeDetector edgeDetector = new EdgeDetector(0.5);

				BufferedImage image;
				try {
					image = ImageIO.read(imageFileUtil.getFile(s));
				} catch (IOException e) {
					return;
				}
				BufferedImage blackEdgeImage = edgeDetector.DetectEdge(image);

				for (Integer colorIndex : colorIndices)
				{
					BufferedImage edgeImage;
					LookupOp op;
					switch (colorIndex) {
					case 0:
						op = getBlackToRedOp();
						edgeImage = op.filter(blackEdgeImage, null);
						break;
					case 1:
						op = getBlackToBlueOp();
						edgeImage = op.filter(blackEdgeImage, null);
						break;
					default:
						edgeImage = blackEdgeImage;
					}

					String algorithm = ProcessedImageUtility.getAlgorithmNameForEdge(colorIndex);
					ProcessedImageKey key = new ProcessedImageKey(s.getId(), algorithm);
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
			}
		}.init(s, colorIndices)));
	}
}
