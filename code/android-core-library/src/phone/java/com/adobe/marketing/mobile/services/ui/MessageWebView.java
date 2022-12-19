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
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.webkit.WebView;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of {@link WebView} that customizes the drawing of the {@link Canvas} to allow for
 * rounded corners.
 */
class MessageWebView extends WebView {

    private RectF webviewRect;
    private int left, top, right, bottom;
    private float cornerRadius;
    private Paint contentPaint, framePaint, shaderPaint;
    private Bitmap bitmap;
    private BitmapShader shader;

    /**
     * Constructor.
     *
     * @param context the current {@link Context}
     * @param cornerRadius a {@code float} defining the angle to use when rounding the message's
     *     webview.
     * @param left an {@code int} specifying the x origin point of the {@link WebView}.
     * @param top an {@code int} specifying the y origin point of the {@code WebView}.
     * @param right an {@code int} specifying the x endpoint of the {@code WebView}.
     * @param bottom an {@code int} specifying the y endpoint of the {@code WebView}.
     */
    public MessageWebView(
            final Context context,
            final float cornerRadius,
            final int left,
            final int top,
            final int right,
            final int bottom)
            throws IllegalArgumentException {
        super(context);
        if (context == null) {
            throw new IllegalArgumentException(
                    "Unable to create the MessageWebView, the context is null.");
        }

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.cornerRadius = cornerRadius;
        initialize();
    }

    @Override
    public void onDraw(final Canvas canvas) {
        super.onDraw(new Canvas(bitmap));
        canvas.drawRoundRect(webviewRect, cornerRadius, cornerRadius, shaderPaint);
    }

    @Override
    public void onSizeChanged(
            final int newWidth, final int newHeight, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        webviewRect = new RectF(left, top, newWidth, newHeight);
    }

    /**
     * Initializes the {@link Paint}, {@link Bitmap}, and {@link BitmapShader} objects needed to add
     * the rounded corners to the webview.
     */
    private void initialize() {
        // setup paint objects for style and color
        contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(Color.WHITE);
        contentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.WHITE);
        shaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // setup bitmap shader to apply the rounded corners as a bitmap texture
        bitmap = Bitmap.createBitmap(right, bottom, Bitmap.Config.ARGB_8888);
        shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shaderPaint.setShader(shader);
        setWillNotDraw(false);
    }

    // for unit testing
    public Map<String, Object> getDimensions() {
        final Map<String, Object> dimensions = new HashMap<>();
        dimensions.put("cornerRadius", cornerRadius);
        dimensions.put("left", left);
        dimensions.put("right", right);
        dimensions.put("top", top);
        dimensions.put("bottom", bottom);
        return dimensions;
    }
}
