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

import java.util.concurrent.atomic.AtomicLong;

public class PrioritizedTask implements Comparable<PrioritizedTask>, Runnable {
	private static final AtomicLong seq = new AtomicLong(0);
	private final long seqNum;
	private final int priority;
	private final Runnable runnable;

	/**
	 * A constructor with priority and runnable.
	 *
	 * @param priority specifies the priority of the task
	 * @param runnable procedure of the task.
	 */
	public PrioritizedTask(int priority, Runnable runnable) {
		this.seqNum = seq.getAndIncrement();
		this.priority = priority;
		this.runnable = runnable;
	}

	/**
	 * Compare based on priority.
	 *
	 * @param o the object to which compare
	 */
	@Override
	public int compareTo(PrioritizedTask o) {
		if (o == null)
			return 1;
		// higher priority is less
		int priorityComparison = Integer.compare(o.priority, this.priority);
		if (priorityComparison != 0)
			return priorityComparison;
		if (this.seqNum < o.seqNum)
			return -1;
		return (this.seqNum > o.seqNum) ? 1 : 0;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	@Override
	public void run() {
		this.runnable.run();
	}
}