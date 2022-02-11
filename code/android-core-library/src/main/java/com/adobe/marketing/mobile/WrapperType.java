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

public enum WrapperType {
	NONE(EventHubConstants.Wrapper.Type.NONE),
	REACT_NATIVE(EventHubConstants.Wrapper.Type.REACT_NATIVE),
	FLUTTER(EventHubConstants.Wrapper.Type.FLUTTER),
	CORDOVA(EventHubConstants.Wrapper.Type.CORDOVA),
	UNITY(EventHubConstants.Wrapper.Type.UNITY),
	XAMARIN(EventHubConstants.Wrapper.Type.XAMARIN);

	private String wrapperTag;

	WrapperType(final String wrapperTag) {
		this.wrapperTag = wrapperTag;
	}

	public String getWrapperTag() {
		return this.wrapperTag;
	}

	public static WrapperType fromString(final String wrapperTag) {
		if (EventHubConstants.Wrapper.Type.REACT_NATIVE.equals(wrapperTag)) {
			return REACT_NATIVE;
		} else if (EventHubConstants.Wrapper.Type.FLUTTER.equals(wrapperTag)) {
			return FLUTTER;
		} else if (EventHubConstants.Wrapper.Type.CORDOVA.equals(wrapperTag)) {
			return CORDOVA;
		} else if (EventHubConstants.Wrapper.Type.UNITY.equals(wrapperTag)) {
			return UNITY;
		} else if (EventHubConstants.Wrapper.Type.XAMARIN.equals(wrapperTag)) {
			return XAMARIN;
		}

		return NONE;
	}

	public String getFriendlyName() {
		switch (this) {
			case REACT_NATIVE:
				return EventHubConstants.Wrapper.Name.REACT_NATIVE;

			case FLUTTER:
				return EventHubConstants.Wrapper.Name.FLUTTER;

			case CORDOVA:
				return EventHubConstants.Wrapper.Name.CORDOVA;

			case UNITY:
				return EventHubConstants.Wrapper.Name.UNITY;

			case XAMARIN:
				return EventHubConstants.Wrapper.Name.XAMARIN;

			case NONE:
			default:
				return EventHubConstants.Wrapper.Name.NONE;
		}
	}
}
