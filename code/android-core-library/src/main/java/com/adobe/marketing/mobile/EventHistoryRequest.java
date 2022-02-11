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

import java.util.Map;

/**
 * This object is used to make select or delete queries with the {@link EventHistoryDatabase}.
 */
final class EventHistoryRequest {
	Map<String, Variant> mask;
	long fromDate;
	long toDate;

	EventHistoryRequest(final Map<String, Variant> mask, final long fromDate, final long toDate) {
		this.mask = mask;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
}
