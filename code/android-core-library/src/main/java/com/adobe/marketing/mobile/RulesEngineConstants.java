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

class RulesEngineConstants {

	static final String DISPATCH_CONSEQUENCE_EVENT_NAME = "Dispatch Consequence Result";
	static final int MAX_CHAINED_CONSEQUENCE_COUNT = 1;

	static final class ConsequenceType {
		static final String ATTACH = "add";
		static final String MODIFY = "mod";
		static final String DISPATCH = "dispatch";
	}

	static final class EventDataKeys {
		static final String CONSEQUENCE_DETAIL              		= "detail";
		static final String CONSEQUENCE_DETAIL_TYPE         		= "type";
		static final String CONSEQUENCE_DETAIL_SOURCE       		= "source";
		static final String CONSEQUENCE_DETAIL_EVENT_DATA   		= "eventdata";
		static final String CONSEQUENCE_DETAIL_EVENT_DATA_ACTION 	= "eventdataaction";
		static final String CONSEQUENCE_JSON_TYPE           		= "type";
		static final String CONSEQUENCE_TRIGGERED           		= "triggeredconsequence";
		static final String CONSEQUENCE_DETAIL_ACTION_COPY			= "copy";
		static final String CONSEQUENCE_DETAIL_ACTION_NEW			= "new";

		private EventDataKeys() {}
	}

	static final class EventHistory {
		static final String ANY = "any";

		static final class RuleDefinition {
			static final String SEARCH_TYPE = "searchType";
			static final String MATCHER = "matcher";
			static final String VALUE = "value";
			static final String FROM = "from";
			static final String TO = "to";
			static final String EVENTS = "events";

			private RuleDefinition() {}
		}
		private EventHistory() {}
	}

	private RulesEngineConstants() {}
}
