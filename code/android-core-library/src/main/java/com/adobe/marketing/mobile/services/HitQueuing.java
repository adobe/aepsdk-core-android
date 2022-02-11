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

package com.adobe.marketing.mobile.services;

//Provides the functionality for Queuing Hits.
public interface HitQueuing {

	/**
	 * Queues a {@link DataEntity} to be processed
	 * @param entity the entity to be processed
	 * @return a boolean indication whether queuing the entity was successful or not
	 */
	boolean queue(DataEntity entity);

	/**
	 * Puts the Queue in non-suspended state and begin processing hits
	 */
	void beginProcessing();

	/**
	 * Puts the Queue in suspended state and discontinue processing hits
	 */
	void suspend();

	/**
	 * Removes all the persisted hits from the queue
	 */
	void clear();

	/**
	 * Returns the number of items in the queue
	 */
	int count();

	/**
	 * Close the current <code>HitQueuing</code>
	 */
	void close();
}
