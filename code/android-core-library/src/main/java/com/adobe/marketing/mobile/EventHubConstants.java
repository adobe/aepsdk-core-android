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

public class EventHubConstants {
	private  EventHubConstants() {}

	static final class Wrapper {
		private Wrapper() {}

		static final class Name {
			private Name() {}

			static final String REACT_NATIVE = "React Native";
			static final String FLUTTER = "Flutter";
			static final String CORDOVA = "Cordova";
			static final String UNITY = " Unity";
			static final String XAMARIN = "Xamarin";
			static final String NONE = "None";

		}

		static final class Type {
			private Type() {}

			static final String REACT_NATIVE = "R";
			static final String FLUTTER = "F";
			static final String CORDOVA = "C";
			static final String UNITY = "U";
			static final String XAMARIN = "X";
			static final String NONE = "N";

		}
	}

	static final class EventDataKeys {
		private EventDataKeys() { }

		static final class EventHub {
			static final String SHARED_STATE_NAME = "com.adobe.module.eventhub";
			static final String VERSION = "version";
			static final String FRIENDLY_NAME = "friendlyName";
			static final String EXTENSIONS = "extensions";
			static final String WRAPPER = "wrapper";
			static final String TYPE = "type";

			private EventHub() {}
		}

		static final class Configuration {

			static final String EVENT_STATE_OWNER 			 						= "stateowner";

			private Configuration() {}
		}
	}
}
