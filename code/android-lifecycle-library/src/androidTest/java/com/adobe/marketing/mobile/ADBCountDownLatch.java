/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ADBCountDownLatch {
	private final CountDownLatch latch;
	private final int initialCount;
	private final AtomicInteger currentCount;

	public ADBCountDownLatch(final int expectedCount) {
		this.initialCount = expectedCount;
		this.latch = new CountDownLatch(expectedCount);
		this.currentCount = new AtomicInteger();
	}

	public void await() throws InterruptedException {
		latch.await();
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return latch.await(timeout, unit);
	}

	public void countDown() {
		currentCount.incrementAndGet();
		latch.countDown();
	}

	public long getCount() {
		return latch.getCount();
	}

	public int getInitialCount() {
		return initialCount;
	}

	public int getCurrentCount() {
		return currentCount.get();
	}

	@Override
	public String toString() {
		return String.format("%s, initial: %d, current: %d", latch.toString(), initialCount, currentCount.get());
	}

}
