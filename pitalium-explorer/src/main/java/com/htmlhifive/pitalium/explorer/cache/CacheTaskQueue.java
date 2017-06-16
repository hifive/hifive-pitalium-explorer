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
package com.htmlhifive.pitalium.explorer.cache;

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

	/**
	 * Cleanup all worker threads.
	 *
	 * @throws InterruptedException
	 */
	public void interruptAndJoin() throws InterruptedException {
		for (Worker worker : workers) {
			worker.requestStop();
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

		/**
		 * Set stop flag and interrupt. The thread will stop soon
		 */
		public void requestStop() {
			this.stop = true;
			this.interrupt();
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