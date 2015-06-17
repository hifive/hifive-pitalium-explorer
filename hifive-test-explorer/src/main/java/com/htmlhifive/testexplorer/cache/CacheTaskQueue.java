package com.htmlhifive.testexplorer.cache;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class CacheTaskQueue {
	private ArrayList<Worker> workers;
	private PriorityBlockingQueue<PrioritizedTask> waitList;

	/**
	 * The default constructor
	 */
	public CacheTaskQueue() {
		this(1);
	}

	/**
	 * Construct with threadCount parameter
	 *
	 * @param threadCount specifies the number of threads to spawn
	 */
	public CacheTaskQueue(int threadCount) {
		workers = new ArrayList<Worker>();
		waitList = new PriorityBlockingQueue<PrioritizedTask>();

		for (int i = 0; i < threadCount; i++) {
			workers.add(new Worker(this));
		}

		/* start threads */
		for (Worker worker : workers) {
			worker.start();
		}
	}

	/**
	 * Add task
	 * 
	 * @param task
	 */
	public void addTask(PrioritizedTask task) {
		waitList.add(task);
	}
	
	public void interruptAndJoin() throws InterruptedException
	{
		for (Worker worker : workers) {
			worker.requestStop();
			worker.interrupt();
		}
		for (Worker worker : workers) {
			worker.join();
		}
		workers.clear();
	}

	/**
	 * Returns if there exists any task. Otherwise block.
	 * 
	 * @return the task that can be run
	 * @throws InterruptedException if interrupted while waiting
	 */
	private PrioritizedTask TakeTask() throws InterruptedException {
		return waitList.take();
	}
	
	private class Worker extends Thread {
		private CacheTaskQueue taskQueue;
		private volatile boolean stop = false; 

		/**
		 * Worker constructor
		 *
		 * @param cacheTaskQueue the queue from which the worker will get tasks
		 */
		public Worker(CacheTaskQueue cacheTaskQueue) {
			this.taskQueue = cacheTaskQueue;
		}

		public void requestStop()
		{
			this.stop = true;
		}

		@Override
		public void run() {
			try {
				while (!this.stop) {
					PrioritizedTask task = taskQueue.TakeTask();
					task.run();
					if (Thread.interrupted())
						return;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
