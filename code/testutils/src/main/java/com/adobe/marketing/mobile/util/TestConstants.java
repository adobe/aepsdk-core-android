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

package com.adobe.marketing.mobile.util;

public class TestConstants {

	public static final String EVENT_NAME_REQUEST_CONTENT = "AEP Request Event";
	public static final String EVENT_NAME_RESPONSE_CONTENT = "AEP Response Event Handle";
	public static final String EVENT_NAME_ERROR_RESPONSE_CONTENT = "AEP Error Response";
	public static final int NETWORK_REQUEST_MAX_RETRIES = 5;
	public static final String EDGE_DATA_STORAGE = "EdgeDataStorage";
	public static final String LOG_TAG = "FunctionalTestsFramework";
	public static final String EXTENSION_NAME = "com.adobe.edge";

	// Event type and sources used by Monitor Extension
	public static class EventType {

		public static final String CONFIGURATION = "com.adobe.eventType.configuration";
		public static final String EDGE = "com.adobe.eventType.edge";
		public static final String MONITOR = "com.adobe.functional.eventType.monitor";

		private EventType() {}
	}

	public static class EventSource {

		public static final String ERROR_RESPONSE_CONTENT = "com.adobe.eventSource.errorResponseContent";
		public static final String LOCATION_HINT_RESULT = "locationHint:result";
		public static final String RESPONSE_CONTENT = "com.adobe.eventSource.responseContent";
		public static final String SHARED_STATE_REQUEST = "com.adobe.eventSource.sharedStateRequest";
		public static final String SHARED_STATE_RESPONSE = "com.adobe.eventSource.sharedStateResponse";
		public static final String STATE_STORE = "state:store";
		public static final String UNREGISTER = "com.adobe.eventSource.unregister";
		public static final String XDM_SHARED_STATE_REQUEST = "com.adobe.eventSource.xdmsharedStateRequest";
		public static final String XDM_SHARED_STATE_RESPONSE = "com.adobe.eventSource.xdmsharedStateResponse";

		private EventSource() {}
	}

	public static class Defaults {

		public static final int WAIT_TIMEOUT_MS = 1000;
		public static final int WAIT_NETWORK_REQUEST_TIMEOUT_MS = 2000;
		public static final int WAIT_EVENT_TIMEOUT_MS = 2000;
		public static final int WAIT_SHARED_STATE_TIMEOUT_MS = 5000;

		public static final String EXEDGE_INTERACT_URL_STRING = "https://edge.adobedc.net/ee/v1/interact";
		public static final String EXEDGE_INTERACT_OR2_LOC_URL_STRING = "https://edge.adobedc.net/ee/or2/v1/interact";
		public static final String EXEDGE_INTERACT_PRE_PROD_URL_STRING =
			"https://edge.adobedc.net/ee-pre-prd/v1/interact";
		public static final String EXEDGE_INTERACT_INT_URL_STRING = "https://edge-int.adobedc.net/ee/v1/interact";

		public static final String EXEDGE_CONSENT_URL_STRING = "https://edge.adobedc.net/ee/v1/privacy/set-consent";
		public static final String EXEDGE_CONSENT_PRE_PROD_URL_STRING =
			"https://edge.adobedc.net/ee-pre-prd/v1/privacy/set-consent";
		public static final String EXEDGE_CONSENT_INT_URL_STRING =
			"https://edge-int.adobedc.net/ee/v1/privacy/set-consent";
		public static final String EXEDGE_MEDIA_PROD_URL_STRING = "https://edge.adobedc.net/ee/va/v1/sessionstart";
		public static final String EXEDGE_MEDIA_OR2_LOC_URL_STRING =
			"https://edge.adobedc.net/ee/or2/va/v1/sessionstart";

		private Defaults() {}
	}

	public static class EventDataKey {

		public static final String EDGE_REQUEST_ID = "requestId";
		public static final String REQUEST_EVENT_ID = "requestEventId";
		public static final String DATASET_ID = "datasetId";
		// Used by Monitor Extension
		public static final String STATE_OWNER = "stateowner";

		private EventDataKey() {}
	}

	public static class DataStoreKey {

		public static final String CONFIG_DATASTORE = "AdobeMobile_ConfigState";
		public static final String IDENTITY_DATASTORE = "com.adobe.edge.identity";
		public static final String IDENTITY_DIRECT_DATASTORE = "visitorIDServiceDataStore";
		public static final String STORE_PAYLOADS = "storePayloads";

		private DataStoreKey() {}
	}

	public static class SharedState {

		public static final String STATE_OWNER = "stateowner";
		public static final String EDGE = "com.adobe.edge";
		public static final String CONFIGURATION = "com.adobe.module.configuration";
		public static final String CONSENT = "com.adobe.edge.consent";
		public static final String ASSURANCE = "com.adobe.assurance";
		public static final String IDENTITY = "com.adobe.module.identity";
		public static final String LIFECYCLE = "com.adobe.module.lifecycle";

		class Configuration {

			public static final String EDGE_CONFIG_ID = "edge.configId";

			private Configuration() {}
		}

		class Identity {

			public static final String ECID = "mid";
			public static final String BLOB = "blob";
			public static final String LOCATION_HINT = "locationhint";
			public static final String VISITOR_IDS_LIST = "visitoridslist";

			private Identity() {}
		}

		class Assurance {

			public static final String INTEGRATION_ID = "integrationid";

			private Assurance() {}
		}

		private SharedState() {}
	}

	public static class NetworkKeys {

		public static final String REQUEST_URL = "https://edge.adobedc.net/ee/v1";
		public static final String REQUEST_PARAMETER_KEY_CONFIG_ID = "configId";
		public static final String REQUEST_PARAMETER_KEY_REQUEST_ID = "requestId";
		public static final String REQUEST_HEADER_KEY_REQUEST_ID = "X-Request-ID";

		public static final String HEADER_KEY_AEP_VALIDATION_TOKEN = "X-Adobe-AEP-Validation-Token";
		public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
		public static final int DEFAULT_READ_TIMEOUT_SECONDS = 5;
		public static final String HEADER_KEY_ACCEPT = "accept";
		public static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
		public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";

		private NetworkKeys() {}
	}

	private TestConstants() {}
}
