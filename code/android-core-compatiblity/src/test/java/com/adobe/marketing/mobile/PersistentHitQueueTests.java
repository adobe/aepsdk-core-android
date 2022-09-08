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

import static org.junit.Assert.assertEquals;

import com.adobe.marketing.mobile.services.DataEntity;
import com.adobe.marketing.mobile.services.DataQueue;
import com.adobe.marketing.mobile.services.HitProcessing;
import com.adobe.marketing.mobile.services.PersistentHitQueue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PersistentHitQueueTests {

	@Mock
	DataQueue dataQueue;

	@Mock
	HitProcessing processor;

	@Test
	public void testIllegalArguementExceptionIsThrownWhenPassNullToConstructor() {
		//Setup
		boolean isExceptionThrown = false;

		try {
			new PersistentHitQueue(null, null);
		} catch (IllegalArgumentException e) {
			isExceptionThrown = true;
		}

		//Assert
		Assert.assertTrue(isExceptionThrown);
	}

	@Test
	public void testDataEntityQueuingShouldReturnTrue() {
		//Setup
		DataEntity dataEntity = new DataEntity("");
		Mockito.when(dataQueue.add(dataEntity)).thenReturn(true);

		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(dataQueue, processor);

		//Action
		boolean result = persistentHitQueue.queue(dataEntity);

		//Assert
		Assert.assertTrue(result);
	}

	@Test
	public void testClearShouldCallDataQueueClear() {

		//Setup
		Mockito.when(dataQueue.clear()).thenReturn(true);
		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(dataQueue, processor);

		//Action
		persistentHitQueue.clear();

		//Assert
		Mockito.verify(dataQueue, Mockito.times(1)).clear();

	}

	@Test
	public void testCountShouldCallDataQueueCount() {

		//Setup
		Mockito.when(dataQueue.count()).thenReturn(1);
		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(dataQueue, processor);

		//Action
		int count = persistentHitQueue.count();

		//Assert
		Mockito.verify(dataQueue, Mockito.times(1)).count();
		assertEquals(count, 1);
	}

	@Test
	public void testCloseShouldCallDataQueueClose() {

		//Setup
		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(dataQueue, processor);

		//Action
		persistentHitQueue.close();

		//Assert
		Mockito.verify(dataQueue, Mockito.times(1)).close();
	}

	@Test
	public void testBeginProcessingCallsProcess() {
		//Setup
		DataEntity dataEntity1 = new DataEntity("dataEntity1");
		DataEntity dataEntity2 = new DataEntity("dataEntity2");

		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(dataQueue, processor);
		Mockito.when(dataQueue.peek()).thenReturn(dataEntity1).thenReturn(dataEntity2).thenReturn(null);

		Mockito.when(processor.processHit(dataEntity1)).thenReturn(true);
		Mockito.when(processor.processHit(dataEntity2)).thenReturn(true);

		Mockito.when(dataQueue.remove()).thenReturn(true).thenReturn(true);

		//Action
		persistentHitQueue.beginProcessing();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Assert
		Mockito.verify(dataQueue, Mockito.times(3)).peek();
		Mockito.verify(dataQueue, Mockito.times(2)).remove();
		Mockito.verify(processor, Mockito.times(1)).processHit(dataEntity1);
		Mockito.verify(processor, Mockito.times(1)).processHit(dataEntity2);
	}

	@Test
	public void testBeginProcessingCallsProcessWithTimeInterval() {
		//Setup
		DataEntity dataEntity = new DataEntity("dataEntity1");

		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(dataQueue, processor);
		Mockito.when(dataQueue.peek()).thenReturn(dataEntity).thenReturn(dataEntity).thenReturn(null);

		Mockito.when(processor.processHit(dataEntity)).thenReturn(false).thenReturn(true);

		Mockito.when(dataQueue.remove()).thenReturn(true);

		Mockito.when(processor.retryInterval(dataEntity)).thenReturn(1);

		//Action
		persistentHitQueue.beginProcessing();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Assert
		Mockito.verify(dataQueue, Mockito.times(3)).peek();
		Mockito.verify(dataQueue, Mockito.times(1)).remove();
		Mockito.verify(processor, Mockito.times(2)).processHit(dataEntity);
	}

	@Test
	public void testQueueMultipleEntitiesWithRetryInterval() throws Exception {
		DataEntity dataEntity1 = new DataEntity("dataEntity1");
		DataEntity dataEntity2 = new DataEntity("dataEntity2");
		DataEntity dataEntity3 = new DataEntity("dataEntity3");

		// Mockito sets the ordering of expected events too nicely, so use mocked objects here instead.
		SimpleDataQueue queue = new SimpleDataQueue();
		MockHitProcessor processor = new MockHitProcessor();
		processor.hitResult = false; // retry hits
		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(queue, processor);

		persistentHitQueue.beginProcessing(); // initialize hit queue
		persistentHitQueue.queue(dataEntity1);
		persistentHitQueue.queue(dataEntity2);
		persistentHitQueue.queue(dataEntity3);

		// Sleep 0.1 seconds, which is less than the retry interval of 1 sec, then set hit result to success
		Thread.sleep(100);
		processor.hitResult = true; // set hit result to success (no retry)

		// Wait to allow retry interval to pass and data queue to get processed and emptied
		queue.waitUntilEmpty(2000);

		// Verify queuing multiple hits does not process hits when the first hit is scheduled to be retried
		List<DataEntity> expectedHitOrder = new ArrayList<DataEntity>() {
			{
				add(dataEntity1);
				add(dataEntity1);
				add(dataEntity2);
				add(dataEntity3);
			}
		};

		assertEquals(0, queue.count());
		assertEquals(expectedHitOrder, processor.processedHits);
	}

	@Test
	public void testQueueMultipleEntitiesUnblocksWhenQueueEmpty() throws Exception {
		DataEntity dataEntity1 = new DataEntity("dataEntity1");
		DataEntity dataEntity2 = new DataEntity("dataEntity2");
		DataEntity dataEntity3 = new DataEntity("dataEntity3");

		// Mockito sets the ordering of expected events too nicely, so use mocked objects here instead.
		SimpleDataQueue queue = new SimpleDataQueue();
		MockHitProcessor processor = new MockHitProcessor();
		processor.hitResult = true; // hits successful
		PersistentHitQueue persistentHitQueue = new PersistentHitQueue(queue, processor);

		persistentHitQueue.beginProcessing(); // initialize hit queue
		persistentHitQueue.queue(dataEntity1);
		persistentHitQueue.queue(dataEntity2);

		// Wait for queue to empty before queuing next entity
		queue.waitUntilEmpty(1000);

		persistentHitQueue.queue(dataEntity3);
		queue.waitUntilEmpty(1000);

		// Verify entities are processed as expected
		List<DataEntity> expectedHitOrder = new ArrayList<DataEntity>() {
			{
				add(dataEntity1);
				add(dataEntity2);
				add(dataEntity3);
			}
		};

		assertEquals(0, queue.count());
		assertEquals(expectedHitOrder, processor.processedHits);
	}
}
