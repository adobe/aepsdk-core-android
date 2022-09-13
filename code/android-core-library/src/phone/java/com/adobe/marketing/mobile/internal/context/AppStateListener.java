package com.adobe.marketing.mobile.internal.context;

/**
 * Listener for app state transition events.
 */
public interface AppStateListener {
    /**
     * invoked when the application transitions into the AppState.FOREGROUND state.
     */
    void onForeground();

    /**
     * invoked when the application transitions into the AppState.BACKGROUND state.
     */
    void onBackground();

}