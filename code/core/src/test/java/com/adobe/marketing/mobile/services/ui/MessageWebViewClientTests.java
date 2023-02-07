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

package com.adobe.marketing.mobile.services.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import java.lang.reflect.Field;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessageWebViewClientTests {

    @Mock private AEPMessage mockAEPMessage;

    @Mock private FullscreenMessageDelegate mockFullscreenMessageDelegate;

    @Mock private MessageWebView mockWebView;

    @Mock private WebResourceRequest mockWebResourceRequest;

    @Mock private Uri mockUri;

    private MessageWebViewClient messageWebViewClient;

    @Before
    public void setup() throws Exception {
        // set the private fullscreen message delegate var using reflection
        final Field listener = mockAEPMessage.getClass().getDeclaredField("listener");
        listener.setAccessible(true);
        listener.set(mockAEPMessage, mockFullscreenMessageDelegate);
        messageWebViewClient = new MessageWebViewClient(mockAEPMessage);
    }

    @Test
    public void testShouldOverrideUrlLoading_WithValidWebResourceRequest_ThenUrlHandled() {
        // setup
        Mockito.when(mockUri.toString()).thenReturn("url");
        Mockito.when(mockWebResourceRequest.getUrl()).thenReturn(mockUri);
        // test
        final boolean urlHandlingAborted =
                messageWebViewClient.shouldOverrideUrlLoading(mockWebView, mockWebResourceRequest);
        // verify
        Assert.assertFalse(urlHandlingAborted);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .overrideUrlLoad(any(AEPMessage.class), anyString());
    }

    @Test
    public void
            testShouldOverrideUrlLoading_WithWebResourceRequestContainsNullUri_ThenUrlNotHandled() {
        Mockito.when(mockUri.toString()).thenReturn(null);
        Mockito.when(mockWebResourceRequest.getUrl()).thenReturn(mockUri);
        // test
        final boolean urlHandlingAborted =
                messageWebViewClient.shouldOverrideUrlLoading(mockWebView, mockWebResourceRequest);
        // verify
        Assert.assertTrue(urlHandlingAborted);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(any(AEPMessage.class), anyString());
    }

    @Test
    public void testShouldOverrideUrlLoading_WithValidString_ThenUrlHandled() {
        // test
        final boolean urlHandlingAborted =
                messageWebViewClient.shouldOverrideUrlLoading(mockWebView, "url");
        // verify
        Assert.assertFalse(urlHandlingAborted);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .overrideUrlLoad(any(AEPMessage.class), anyString());
    }

    @Test
    public void testShouldOverrideUrlLoading_WithNullString_ThenUrlNotHandled() {
        // test
        final boolean urlHandlingAborted =
                messageWebViewClient.shouldOverrideUrlLoading(mockWebView, (String) null);
        // verify
        Assert.assertTrue(urlHandlingAborted);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(any(AEPMessage.class), ArgumentMatchers.<String>any());
    }

    @Test
    public void testShouldOverrideUrlLoading_WithEmptyString_ThenUrlNotHandled() {
        // test
        final boolean urlHandlingAborted =
                messageWebViewClient.shouldOverrideUrlLoading(mockWebView, "");
        // verify
        Assert.assertTrue(urlHandlingAborted);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(any(AEPMessage.class), ArgumentMatchers.<String>any());
    }

    @Test
    public void testShouldOverrideUrlLoading_WithNullMessageDelegate_ThenUrlNotHandled()
            throws IllegalAccessException, NoSuchFieldException {
        // setup
        // set the private fullscreen message delegate var using reflection
        final Field listener = mockAEPMessage.getClass().getDeclaredField("listener");
        listener.setAccessible(true);
        listener.set(mockAEPMessage, null);

        // test
        final boolean urlHandlingAborted =
                messageWebViewClient.shouldOverrideUrlLoading(mockWebView, (String) null);
        // verify
        Assert.assertTrue(urlHandlingAborted);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(any(AEPMessage.class), ArgumentMatchers.<String>any());
    }
}
