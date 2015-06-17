package com.htmlhifive.testexplorer.cache;

import java.util.concurrent.atomic.AtomicLong;

public class PrioritizedTask implements Comparable<PrioritizedTask>, Runnable
{
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
	public PrioritizedTask(int priority, Runnable runnable)
	{
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
		return priorityComparison != 0 ? priorityComparison :
			Long.compare(this.seqNum, o.seqNum);
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
