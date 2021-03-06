/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-1-15 上午11:00:51
 */
package com.absir.core.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author absir
 * 
 */
public class UtilAtom {

	/** atomic */
	protected int atomic;

	/** lock */
	protected Lock lock = new ReentrantLock();

	/** condition */
	protected Condition condition = lock.newCondition();

	/**
	 * @return the atomic
	 */
	public int getAtomic() {
		return atomic;
	}

	/**
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * 
	 */
	public synchronized void increment() {
		++atomic;
	}

	/**
	 * 
	 */
	public synchronized void decrement() {
		if (--atomic == 0) {
			lock.lock();
			condition.signal();
			lock.unlock();
		}
	}

	/**
	 * 
	 */
	public void await() {
		if (atomic > 0) {
			lock.lock();
			try {
				condition.await();
				return;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lock.unlock();
		}
	}
}
