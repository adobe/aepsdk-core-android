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
public class RulesEngineConstantsTests {
	static final class EventDataKeys {

		static final String CONSEQUENCE_DETAIL              		= "detail";
		static final String CONSEQUENCE_DETAIL_EVENT_DATA   		= "eventdata";
		static final String CONSEQUENCE_DETAIL_EVENT_DATA_ACTION   	= "eventdataaction";
		static final String CONSEQUENCE_DETAIL_ACTION_COPY			= "copy";
		static final String CONSEQUENCE_DETAIL_ACTION_NEW			= "new";
		static final String CONSEQUENCE_JSON_ID             		= "id";
		static final String CONSEQUENCE_JSON_TYPE           		= "type";
		static final String CONSEQUENCE_JSON_DETAIL         		= "detail";
		static final String CONSEQUENCE_TRIGGERED           		= "triggeredconsequence";
		static final String DISPATCH_CONSEQUENCE_SOURCE     		= "source";
		static final String DISPATCH_CONSEQUENCE_TYPE     			= "type";

		private EventDataKeys() {}
	}

	static final class ConsequenceTypes {
		static final String ATTACH_DATA             = "add";
		static final String MODIFY_DATA             = "mod";
		static final String SEND_DATA_TO_ANALYTICS  = "an";
		static final String DISPATCH  				= "dispatch";

		private ConsequenceTypes() {}
	}

	private RulesEngineConstantsTests() {}

}
