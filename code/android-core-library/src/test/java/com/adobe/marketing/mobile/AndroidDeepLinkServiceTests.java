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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import android.app.Activity;
import android.content.Intent;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.Silent.class)
public class AndroidDeepLinkServiceTests {

	@org.junit.Rule
	public TestName name = new TestName();

	@Mock
	Activity mockActivity;

	AndroidDeepLinkService deepLinkService;

	@Before
	public void beforeEach() {
		deepLinkService = new AndroidDeepLinkService();
	}

	@After
	public void afterEach() {
	}

	@Test
	public void noCrash_When_NoActivity() throws Exception {
		deepLinkService.triggerDeepLink("test");
	}

	@Test
	public void startActivity_When_UrlIsValid() throws Exception {
		App.setCurrentActivity(mockActivity);
		final ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);

		deepLinkService.triggerDeepLink("test");
		verify(mockActivity, times(1)).startActivity(captor.capture());
		final Intent intent = captor.getValue();
		assertEquals(Intent.class, intent.getClass());

	}

	@Test
	public void doNothing_When_UrlIsEmpty() throws Exception {
		deepLinkService.triggerDeepLink("");
		verify(mockActivity, times(0)).startActivity(any(Intent.class));

	}

	@Test
	public void doNothing_When_UrlIsNull() throws Exception {
		deepLinkService.triggerDeepLink(null);

		verify(mockActivity, times(0)).startActivity(any(Intent.class));
	}
}
