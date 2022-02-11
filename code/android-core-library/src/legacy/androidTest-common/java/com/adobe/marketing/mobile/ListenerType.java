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

public class ListenerType {

	String eventType;
	String eventSource;

	public ListenerType(String eventType, String eventSource) {
		this.eventSource = eventSource;
		this.eventType = eventType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ListenerType)) {
			return false;
		}

		ListenerType listenerType = (ListenerType) obj;

		if (this == listenerType) {
			return true;
		}

		return (eventType == listenerType.eventType || (eventType != null && eventType.equals(listenerType.eventType))
				&& (eventSource == listenerType.eventSource || (eventSource != null && eventSource.equals(listenerType.eventSource))));
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((eventSource == null) ? 0 : eventSource.hashCode());
		return result;
	}
}
