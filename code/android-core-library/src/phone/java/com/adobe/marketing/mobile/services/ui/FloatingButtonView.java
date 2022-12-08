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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FloatingButtonView extends Button implements View.OnTouchListener {

    private float oldXvalue;
    private float oldYvalue;
    private float maxButtonTouch;
    private static final float BUTTON_MOVEMENT_TOLERANCE = 20;
    private OnPositionChangedListener onPositionChangedListener;
    private FloatingButtonListener buttonListener;

    // This tag will be used to identify the floating button in the root view.
    // This means that only one button per activity is supported as of now.
    static final String VIEW_TAG = "ADBFloatingButtonTag";

    private static final String TAG = FloatingButtonView.class.getSimpleName();

    /** An interface to receive postion changed callbacks for the button */
    interface OnPositionChangedListener {
        /**
         * Will be called whenever the button is moved to a new location on screen.
         *
         * @param newX The new x co-ordinate
         * @param newY The new y co-ordinate
         */
        void onPositionChanged(float newX, float newY);
    }

    public FloatingButtonView(final Context context) {
        super(context);
        setBackgroundCompat();
        setOnTouchListener(this);
    }

    public FloatingButtonView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingButtonView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the {@code UIService.FloatingButtonListener} for the button.
     *
     * <p>The {@link OnClickListener} for the button calls through to the {@link
     * FloatingButtonListener#onTapDetected()}. <br>
     *
     * @param listener The {@code UIService.FloatingButtonListener} instance. If this is null, the
     *     listener is cleared.
     */
    void setFloatingButtonListener(final FloatingButtonListener listener) {
        buttonListener = listener;

        if (listener != null) {
            setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            listener.onTapDetected();
                        }
                    });
        }
    }

    void setOnPositionChangedListener(final OnPositionChangedListener onPositionChangedListener) {
        this.onPositionChangedListener = onPositionChangedListener;
    }

    void setBitmap(final Bitmap bitmap)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap is null!");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 16
            // Should be View.class
            Class<?> classs = this.getClass().getSuperclass().getSuperclass().getSuperclass();
            Method setBackgroundMethod = classs.getDeclaredMethod("setBackground", Drawable.class);
            setBackgroundMethod.invoke(this, new BitmapDrawable(getResources(), bitmap));
        } else {
            setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }
    }

    /**
     * Sets the background image from the encoded file {@link
     * ButtonBackground#ENCODED_BACKGROUND_PNG}
     */
    private void setBackgroundCompat() {
        byte[] backgroundImage =
                Base64.decode(ButtonBackground.ENCODED_BACKGROUND_PNG, Base64.DEFAULT);

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(backgroundImage));
            setBitmap(bitmap);
        } catch (Exception e) {
            // fallback
            setText("Preview");
        }
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_UP) {
            if (maxButtonTouch < BUTTON_MOVEMENT_TOLERANCE) {
                // Let the view handle the click.
                // If a onClickListener is registered by the caller,
                // then it will be called by the system - else not.
                performClick();
            }
        } else if (me.getAction() == MotionEvent.ACTION_DOWN) {
            maxButtonTouch = 0.0f;
            oldXvalue = me.getRawX();
            oldYvalue = me.getRawY();
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float currentXvalue = me.getRawX();
            float currentYvalue = me.getRawY();
            setXYCompat(currentXvalue - (getWidth() / 2), currentYvalue - (getHeight() / 2));

            float displacement =
                    Math.abs(currentXvalue - oldXvalue) + Math.abs(currentYvalue - oldYvalue);

            if (displacement > maxButtonTouch) {
                maxButtonTouch = displacement;
            }
        }

        return true;
    }

    /**
     * Set X and Y co-ordinates for the button. This is only available for API 11 and above. In
     * lower versions, this will have not effect.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     */
    void setXYCompat(final float x, final float y) {
        try {
            Class<?> buttonClass = this.getClass();

            // Should be View.class
            Class<?> classs = buttonClass.getSuperclass().getSuperclass().getSuperclass();

            Method setXMethod = classs.getDeclaredMethod("setX", float.class);
            Method setYMethod = classs.getDeclaredMethod("setY", float.class);
            setXMethod.invoke(this, x);
            setYMethod.invoke(this, y);

            if (onPositionChangedListener != null) {
                onPositionChangedListener.onPositionChanged(x, y);
            }

            if (buttonListener != null) {
                buttonListener.onPanDetected();
            }
        } catch (Exception e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Error while setting the position (%s)", e));
        }
    }
}
