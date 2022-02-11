package com.adobe.marketing.mobile;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountUpLatch {

	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	int count = 0;

	public void await(final int expectedCount, final int timeoutInMilli) {
		lock.lock();
		int timeout = 0;

		try {
			while (count < expectedCount && timeout < timeoutInMilli / 100) {
				notFull.await(100, TimeUnit.MILLISECONDS);
				timeout++;
			}
		} catch (InterruptedException e) {

		} finally {
			lock.unlock();
		}
	}

	public void await(final int expectedCount) {
		this.await(expectedCount, 2000);
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