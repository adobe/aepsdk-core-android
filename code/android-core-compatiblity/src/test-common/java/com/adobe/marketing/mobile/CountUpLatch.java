package com.adobe.marketing.mobile;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountUpLatch {

	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	int count = 0;


	public void await(final int expectedCount) {
		lock.lock();
		int timeout = 0;

		try {
			while (count < expectedCount && timeout < 20) {
				notFull.await(200, TimeUnit.MILLISECONDS);
				timeout++;
			}
		} catch (InterruptedException e) {

		} finally {
			lock.unlock();
		}
	}


	public void countUp() {
		lock.lock();

		try {
			count++;
			notFull.signal();
		} finally {
			lock.unlock();
		}


	}

}