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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class FloatingButtonManager implements FloatingButton {

    private static final String LOG_TAG = FloatingButtonManager.class.getSimpleName();
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private AndroidUIService androidUIService;
    private FloatingButtonListener buttonListener = null;
    private float lastKnownXPos;
    private float lastKnownYPos;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private boolean displayFloatingButtonAcrossActivities = false;

    Map<String, FloatingButtonView> managedButtons = new HashMap<String, FloatingButtonView>();

    FloatingButtonManager(
            final AndroidUIService androidUIService, final FloatingButtonListener listener) {
        this.androidUIService = androidUIService;
        this.buttonListener = listener;
    }

    @Override
    public void display() {
        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();

        if (currentActivity == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format(
                            "%s (Current activity), will not display button.",
                            UNEXPECTED_NULL_VALUE));
            return;
        }

        if (activityLifecycleCallbacks != null) {
            Log.debug(ServiceConstants.LOG_TAG, LOG_TAG, "Display cannot be called twice!");
            return;
        }

        // We need to register for app states
        Application application =
                ServiceProvider.getInstance().getAppContextService().getApplication();

        if (application != null) {
            activityLifecycleCallbacks = getActivityLifecycleCallbacks();
            application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        }

        display(0, 0, currentActivity);

        displayFloatingButtonAcrossActivities = true;
    }

    /**
     * Returns a new instance of a {@code Application.ActivityLifecycleCallbacks}.
     *
     * <p>
     *
     * <p>Currently only onResume and onDestroy of an activity are being responded to in this
     * listener.
     *
     * @return An {@link Application.ActivityLifecycleCallbacks} instance
     */
    Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(final Activity activity, final Bundle bundle) {}

            @Override
            public void onActivityStarted(final Activity activity) {}

            @Override
            public void onActivityResumed(final Activity activity) {
                // if a floating button should no longer be displayed,
                // and this activity has a button showing, then remove it
                if (!displayFloatingButtonAcrossActivities) {
                    if (managedButtons.containsKey(activity.getLocalClassName())) {
                        // This activity has a managedButton that needs to be now removed
                        removeFloatingButtonFromActivity(activity);
                    }

                    if (managedButtons.isEmpty()) {
                        // All of the managed buttons have been hidden
                        deregisterLifecycleCallbacks();
                    }

                    return;
                }

                // Show the button (create new if does not exist for this activity)
                FloatingButtonView existingButton =
                        managedButtons.get(activity.getLocalClassName());

                if (existingButton == null) {
                    // We do not have an existing button showing, create one
                    FloatingButtonView newFloatingButtonView =
                            androidUIService.createFloatingButtonView(activity);
                    addManagedButton(activity.getLocalClassName(), newFloatingButtonView);
                }

                display(lastKnownXPos, lastKnownYPos, activity);
            }

            @Override
            public void onActivityPaused(final Activity activity) {}

            @Override
            public void onActivityStopped(final Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(final Activity activity, final Bundle bundle) {}

            @Override
            public void onActivityDestroyed(final Activity activity) {
                managedButtons.remove(activity.getLocalClassName());
            }
        };
    }

    void deregisterLifecycleCallbacks() {
        // deregister lifecycle listener
        Application application =
                ServiceProvider.getInstance().getAppContextService().getApplication();

        if (application != null) {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
            activityLifecycleCallbacks = null;
        }
    }

    /**
     * Adds an instance of {@code FloatingButtonView} to be managed by this.
     *
     * @param activityClassName The Class name of the {@link Activity} that this button should be
     *     placed on
     * @param uiButton The {@link FloatingButtonView} instance to be placed and managed
     * @see AndroidUIService#createFloatingButtonView(Activity)
     * @see Activity#getLocalClassName()
     */
    void addManagedButton(final String activityClassName, final FloatingButtonView uiButton) {
        uiButton.setFloatingButtonListener(buttonListener);
        managedButtons.put(activityClassName, uiButton);
    }

    /**
     * Display an instance of the {@code FloatingButtonView} on the {@code currentActivity}
     * supplied.
     *
     * <p>An instance of {@link FloatingButtonView} needs to be already instantiated and managed by
     * this manager before this method can be used to display the button on the activity. This
     * method also checks to see if there is already a button displaying on the activity, and if so,
     * will adjust the position as per the co-ordinates supplied. if the button does not exist, then
     * it will be created and displayed.
     *
     * @param x The x co-ordinate where the button will be displayed
     * @param y The y co-ordinate where the button will be displayed
     * @param currentActivity The {@link Activity} where the button will be displayed. If this
     *     activity does not have a window associated with it, then the button will not be shown.
     */
    void display(final float x, final float y, final Activity currentActivity) {
        try {
            // We will use the absolute width and height later if the rootview has not been measured
            // by then.
            DisplayMetrics displayMetrics = new DisplayMetrics();
            currentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int absHeightPx = displayMetrics.heightPixels;
            final int absWidthPx = displayMetrics.widthPixels;
            final ViewGroup rootViewGroup =
                    (ViewGroup) currentActivity.getWindow().getDecorView().getRootView();
            currentActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            final int width =
                                    rootViewGroup.getMeasuredWidth() == 0
                                            ? absWidthPx
                                            : rootViewGroup.getMeasuredWidth();
                            final int height =
                                    rootViewGroup.getMeasuredHeight() == 0
                                            ? absHeightPx
                                            : rootViewGroup.getMeasuredHeight();

                            FloatingButtonView floatingButton =
                                    (FloatingButtonView)
                                            rootViewGroup.findViewWithTag(
                                                    FloatingButtonView.VIEW_TAG);

                            if (floatingButton != null) {
                                // The button already exists as a child of the root
                                // Adjust x and y to account for orientation change.
                                lastKnownXPos = adjustXBounds(floatingButton, width, x);
                                lastKnownYPos = adjustYBounds(floatingButton, height, y);
                                floatingButton.setXYCompat(lastKnownXPos, lastKnownYPos);
                                return;
                            }

                            final String activityClassName = currentActivity.getLocalClassName();

                            final FloatingButtonView floatingButtonView =
                                    managedButtons.get(activityClassName);

                            if (floatingButtonView == null) {
                                Log.debug(
                                        ServiceConstants.LOG_TAG,
                                        LOG_TAG,
                                        String.format(
                                                "%s (Floating button view), for activity: %s",
                                                UNEXPECTED_NULL_VALUE, activityClassName));
                                return;
                            }

                            floatingButtonView.setOnPositionChangedListener(
                                    new FloatingButtonView.OnPositionChangedListener() {
                                        @Override
                                        public void onPositionChanged(
                                                final float newX, final float newY) {
                                            lastKnownXPos = newX;
                                            lastKnownYPos = newY;
                                        }
                                    });

                            final ViewTreeObserver viewTreeObserver =
                                    floatingButtonView.getViewTreeObserver();
                            ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener =
                                    new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            removeOnGlobalLayoutListenerCompat(
                                                    floatingButtonView, this);

                                            if (x >= 0 && y >= 0) {
                                                // Adjust x and y to account for orientation change.
                                                lastKnownXPos =
                                                        adjustXBounds(floatingButtonView, width, x);
                                                lastKnownYPos =
                                                        adjustYBounds(
                                                                floatingButtonView, height, y);
                                                floatingButtonView.setXYCompat(
                                                        lastKnownXPos, lastKnownYPos);
                                            } else {
                                                lastKnownXPos =
                                                        ((width / 2)
                                                                - (floatingButtonView.getWidth()
                                                                        / 2));
                                                lastKnownYPos =
                                                        ((height / 2)
                                                                - (floatingButtonView.getHeight()
                                                                        / 2));
                                                floatingButtonView.setXYCompat(
                                                        lastKnownXPos, lastKnownYPos);
                                            }
                                        }
                                    };

                            viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener);
                            rootViewGroup.addView(floatingButtonView);

                            ViewGroup.LayoutParams layoutParams =
                                    floatingButtonView.getLayoutParams();

                            if (layoutParams != null) {
                                final int LAYOUT_PARAMS_DP = 80;
                                layoutParams.width =
                                        getPxForDp(
                                                floatingButtonView.getContext(), LAYOUT_PARAMS_DP);
                                layoutParams.height =
                                        getPxForDp(
                                                floatingButtonView.getContext(), LAYOUT_PARAMS_DP);

                                floatingButtonView.setLayoutParams(layoutParams);
                            }
                        }
                    });
        } catch (Exception ex) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format("Could not display the button (%s)", ex));
        }
    }

    @Override
    public void remove() {
        Activity activity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
        removeFloatingButtonFromActivity(activity);
        displayFloatingButtonAcrossActivities = false;
    }

    void removeFloatingButtonFromActivity(final Activity activity) {
        if (activity == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    String.format("%s (Activity), cannot remove button!", UNEXPECTED_NULL_VALUE));
            return;
        }

        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Activity activity =
                                ServiceProvider.getInstance()
                                        .getAppContextService()
                                        .getCurrentActivity();

                        if (activity == null) {
                            Log.warning(
                                    ServiceConstants.LOG_TAG,
                                    LOG_TAG,
                                    String.format(
                                            "%s (Activity), cannot remove button!",
                                            UNEXPECTED_NULL_VALUE));
                            return;
                        }

                        final ViewGroup rootViewGroup =
                                (ViewGroup) activity.getWindow().getDecorView().getRootView();
                        FloatingButtonView floatingButton =
                                (FloatingButtonView)
                                        rootViewGroup.findViewWithTag(FloatingButtonView.VIEW_TAG);

                        if (floatingButton != null) {
                            floatingButton.setVisibility(ViewGroup.GONE);
                        } else {
                            Log.debug(
                                    ServiceConstants.LOG_TAG,
                                    LOG_TAG,
                                    String.format(
                                            "No button found to remove for %s",
                                            activity.getLocalClassName()));
                        }
                    }
                });

        managedButtons.remove(activity.getLocalClassName());
    }

    /**
     * Adjust the x co-ordinate so that it remains within the screen width.
     *
     * @param floatingButtonView The button for which the position needs to be adjusted
     * @param screenWidth The screen width in pixels
     * @param oldXvalue The x co-ordinate which needs to be adjusted
     * @return The adjusted x co-ordinate
     */
    private float adjustXBounds(
            final FloatingButtonView floatingButtonView,
            final float screenWidth,
            final float oldXvalue) {
        if (floatingButtonView != null
                && oldXvalue > (screenWidth - floatingButtonView.getWidth())) {
            return (screenWidth - floatingButtonView.getWidth());
        }

        return oldXvalue;
    }

    /**
     * Adjust the y co-ordinate so that it remains within the screen height.
     *
     * @param floatingButtonView The button for which the position needs to be adjusted
     * @param screenHeight The screen height in pixels
     * @param oldYvalue The y co-ordinate which needs to be adjusted
     * @return The adjusted y co-ordinate
     */
    private float adjustYBounds(
            final FloatingButtonView floatingButtonView,
            final float screenHeight,
            final float oldYvalue) {
        if (floatingButtonView != null
                && oldYvalue > (screenHeight - floatingButtonView.getHeight())) {
            return (screenHeight - floatingButtonView.getHeight());
        }

        return oldYvalue;
    }

    /**
     * Convert {@code dp} into {@code px} scale.
     *
     * @param context The {@link Context} instance
     * @param dp The dp value to be converted
     * @return The converted px value
     */
    private int getPxForDp(final Context context, final int dp) {
        try {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round((float) dp * density);
        } catch (Exception e) {
            final int DEFAULT_FALLBACK = 210; // dp=80 density=2.65
            return DEFAULT_FALLBACK;
        }
    }

    /**
     * Removes the {@code OnGlobalLayoutListener} registered earlier.
     *
     * <p>The {@code onGlobalLayoutListener} instance should be something that was registered
     * earlier. For example, {@link #display(float, float, Activity)} method registers a listener to
     * position the button on the view tree, and then once done, uses this method to de-register the
     * listener.
     *
     * @param floatingButtonView The {@link FloatingButtonView} instance which is used to retrieve
     *     the {@link ViewTreeObserver}
     * @param onGlobalLayoutListener The {@link ViewTreeObserver.OnGlobalLayoutListener} instance
     */
    void removeOnGlobalLayoutListenerCompat(
            final FloatingButtonView floatingButtonView,
            final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        ViewTreeObserver viewTreeObserver = floatingButtonView.getViewTreeObserver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 16
            try {
                Class<?> viewTreeObserverClass = viewTreeObserver.getClass();
                Method removeOnGlobalLayoutListenerMethod =
                        viewTreeObserverClass.getDeclaredMethod(
                                "removeOnGlobalLayoutListener",
                                ViewTreeObserver.OnGlobalLayoutListener.class);
                removeOnGlobalLayoutListenerMethod.invoke(viewTreeObserver, onGlobalLayoutListener);
            } catch (Exception e) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        LOG_TAG,
                        String.format("Error while cleaning up (%s)", e));
            }
        } else {
            viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener);
        }
    }
}
