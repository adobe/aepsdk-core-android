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

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static junit.framework.Assert.assertEquals;

public class AbstractE2ETest {
	private static TestHelper testHelper = new TestHelper();
	E2ETestableNetworkService testableNetworkService;

	public void setUp() {
		TestingPlatform testingPlatform = new TestingPlatform();
		testableNetworkService = testingPlatform.e2EAndroidNetworkService;
		MobileCore.setApplication(defaultApplication);
		testHelper.cleanCache(defaultApplication.getApplicationContext());
		TestHelper.cleanLocalStorage();
	}

	public void tearDown() {

	}

	public class LogCat implements TestRule {
		public LogCat() {
		}

		public Statement apply(Statement base, Description description) {
			return statement(base, description);
		}

		private Statement statement(final Statement base, final Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					try {
						base.evaluate();
					} catch (Throwable t) {
						throw new Throwable(collectLogCat(), t);
					}
				}
			};
		}
	}
	@Rule
	public LogCat logCat = new LogCat();
	@Rule
	public TestName name = new TestName();

	public Context defaultContext;
	public Application defaultApplication;

	public static class CustomApplication extends Application {
		public CustomApplication() {

		}
	}
	@Rule
	public TestRule androidContainer = new TestRule() {
		@Override
		public Statement apply(final Statement base, Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Log.error("Monitor", "timestamp %d", System.currentTimeMillis());
					defaultContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
					defaultApplication = Instrumentation.newApplication(CustomApplication.class, defaultContext);
					base.evaluate();

				}
			};
		}
	};

	public String collectLogCat() {
		Process process;
		StringBuilder log = new StringBuilder();

		try {
			process = Runtime.getRuntime().exec("logcat -t 100 -d ADBMobile:V TestRunner:I Hermetic:V *:S");
			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
			String line = "";
			boolean ignoreLines = true;

			while ((line = bufferedReader.readLine()) != null) {
				if (ignoreLines && line.matches(".*started: " + name.getMethodName() + ".*")) {
					ignoreLines = false;
				}

				if (!ignoreLines) {
					log.append(line).append("\n");
				}
			}
		} catch (IOException e) {
		}

		return log.toString();
	}

	//	public void setupIdentityModuleForE2E() {
	//
	//		String idSyncMatcher = "d_cid_ic=type%01value%011";
	//		E2ERequestMatcher networkMatcher = new E2ERequestMatcher(idSyncMatcher);
	//		//setup identity module shared state
	//		String json =
	//				"{\"d_mid\":\"65200765694214613857127198643018103011\",\"id_sync_ttl\":604800,\"d_blob\":\"hmk_Lq6TPIBMW925SPhw3Q\",\"dcs_region\":9,\"d_ottl\":7200,\"ibs\":[],\"subdomain\":\"obumobile5\",\"tid\":\"d47JfAKTTsU=\"}";
	//
	//		E2ETestableNetworkService.NetworkResponse networkResponse = new E2ETestableNetworkService.NetworkResponse(json, 200,
	//				null);
	//		testableNetworkService.setResponse(networkMatcher, networkResponse);
	//		Identity.syncIdentifier("type", "value", VisitorID.AuthenticationState.AUTHENTICATED);
	//		//verify expected counts
	//
	//		assertEquals(3,testableNetworkService.waitAndGetCount(3));
	//
	//	}
}