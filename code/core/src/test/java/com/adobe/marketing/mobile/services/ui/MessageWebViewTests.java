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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessageWebViewTests {

    @Mock Context mockContext;

    @Mock Canvas mockCanvas;

    private final float cornerRadius = 25.0f;
    private final int leftMargin = 0;
    private final int topMargin = 0;
    private final int rightMargin = 1000;
    private final int bottomMargin = 1000;

    @Test
    public void testCreateMessageWebView() throws IllegalArgumentException {
        // setup
        MessageWebView messageWebView = null;
        // test
        messageWebView =
                new MessageWebView(
                        mockContext,
                        cornerRadius,
                        leftMargin,
                        topMargin,
                        rightMargin,
                        bottomMargin);
        // verify
        Assert.assertNotNull(messageWebView);
        final Map<String, Object> dimensions = messageWebView.getDimensions();
        Assert.assertEquals(cornerRadius, (float) dimensions.get("cornerRadius"), 0.0f);
        Assert.assertEquals(leftMargin, dimensions.get("left"));
        Assert.assertEquals(rightMargin, dimensions.get("right"));
        Assert.assertEquals(topMargin, dimensions.get("top"));
        Assert.assertEquals(bottomMargin, dimensions.get("bottom"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMessageWebView_WhenContextIsNull() throws IllegalArgumentException {
        // test
        MessageWebView messageWebView =
                new MessageWebView(
                        null, cornerRadius, leftMargin, topMargin, rightMargin, bottomMargin);
        // verify
        Assert.fail("IllegalArgumentException was not thrown.");
    }

    @Test
    public void testMessageWebViewOnDraw() throws IllegalArgumentException {
        // setup
        MessageWebView messageWebView =
                new MessageWebView(
                        mockContext,
                        cornerRadius,
                        leftMargin,
                        topMargin,
                        rightMargin,
                        bottomMargin);
        // call onSizeChanged to setup a RectF for the webview
        messageWebView.onSizeChanged(
                messageWebView.getWidth(),
                messageWebView.getHeight(),
                messageWebView.getWidth(),
                messageWebView.getHeight());
        // test
        messageWebView.onDraw(mockCanvas);
        // verify
        Assert.assertNotNull(messageWebView);
        Mockito.verify(mockCanvas, Mockito.times(1))
                .drawRoundRect(
                        ArgumentMatchers.any(RectF.class),
                        ArgumentMatchers.anyFloat(),
                        ArgumentMatchers.anyFloat(),
                        ArgumentMatchers.any(Paint.class));
    }
}
