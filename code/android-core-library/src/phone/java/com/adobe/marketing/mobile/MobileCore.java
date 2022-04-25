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

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.adobe.marketing.mobile.internal.eventhub.history.EventHistory;

import java.util.Date;
import java.util.Map;

public class MobileCore {
    private final static String VERSION = "1.10.0";
    private final static String TAG = MobileCore.class.getSimpleName();
    private static final String NULL_CONTEXT_MESSAGE = "Context must be set before calling SDK methods";

    private static Core core;
    private static PlatformServices platformServices;
    private static final Object mutex = new Object();

    private MobileCore() {
    }

    // For test
    static void setCore(final Core core) {
        synchronized (mutex) {
            MobileCore.core = core;
        }
    }

    // For test
    static void setPlatformServices(final PlatformServices platformServices) {
        synchronized (mutex) {
            MobileCore.platformServices = platformServices;
        }
    }

    static Core getCore() {
        synchronized (mutex) {
            return core;
        }
    }

    public static EventHistory getEventHistory() {
        if (core == null) return null;
        return core.getEventHistory();
    }


    /**
     * Returns the version for the {@code MobileCore} extension
     *
     * @return The version string
     */
    public static String extensionVersion() {
        synchronized (mutex) {
            if (core == null) {
                Log.warning(TAG, "Returning version without wrapper type info. Make sure setApplication API is called.");
                return VERSION;
            }

            return core.getSdkVersion();
        }
    }

    /**
     * Set the current {@link Application}, which enables the SDK get the app {@code Context},
     * register a {@link Application.ActivityLifecycleCallbacks}
     * to monitor the lifecycle of the app and get the {@link android.app.Activity} on top of the screen.
     * <p>
     * NOTE: This method should be called right after the app starts, so it gives the SDK all the
     * contexts it needed.
     *
     * @param app the current {@code Application}
     */
    public static void setApplication(final Application app) {
        // AMSDK-8502
        // workaround to prevent a crash happening on Android 8.0/8.1 related to TimeZoneNamesImpl
        // https://issuetracker.google.com/issues/110848122
        try {
            new Date().toString();
        } catch (AssertionError e) {
            // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
        } catch (Exception e) {
            // Workaround for a bug in Android that can cause crashes on Android 8.0 and 8.1
        }

        App.setApplication(app);
        V4ToV5Migration migrationTool = new V4ToV5Migration();
        migrationTool.migrate();

        if (core == null) {
            synchronized (mutex) {
                if (platformServices == null) {
                    platformServices = new AndroidPlatformServices();
                }

                core = new Core(platformServices, VERSION);
            }
        }

        com.adobe.marketing.mobile.internal.context.App.getInstance().initializeApp(new
                                                                                            com.adobe.marketing.mobile.internal.context.App.AppContextProvider() {
                                                                                                @Override
                                                                                                public Context getAppContext() {
                                                                                                    return App.getAppContext();
                                                                                                }

                                                                                                @Override
                                                                                                public Activity getCurrentActivity() {
                                                                                                    return App.getCurrentActivity();
                                                                                                }
                                                                                            });

    }

    /**
     * Get the global {@link Application} object of the current process.
     * <p>
     * NOTE: {@link #setApplication(Application)} must be called before calling this method.
     *
     * @return the current {@code Application}, or null if no {@code Application} was set or
     * the {@code Application} process was destroyed.
     */
    public static Application getApplication() {
        return App.getApplication();
    }

    /**
     * Set the {@link LoggingMode} level for the Mobile SDK.
     *
     * @param mode the logging mode
     */
    public static void setLogLevel(LoggingMode mode) {
        Log.setLogLevel(mode);
    }

    /**
     * Get the {@link LoggingMode} level for the Mobile SDK
     *
     * @return the set {@code LoggingMode}
     */
    public static LoggingMode getLogLevel() {
        return Log.getLogLevel();
    }

    /**
     * Sends a log message of the given {@code LoggingMode}. If the specified {@code mode} is
     * more verbose than the current {@link LoggingMode} set from {@link #setLogLevel(LoggingMode)}
     * then the message is not printed.
     *
     * @param mode    the {@link LoggingMode} used to print the message
     * @param tag     used to identify the source of the log message
     * @param message the message to log
     */
    public static void log(final LoggingMode mode, final String tag, final String message) {
        if (mode == null) {
            return;
        }

        switch (mode) {
            case ERROR:
                Log.error(tag, message);
                break;

            case WARNING:
                Log.warning(tag, message);
                break;

            case DEBUG:
                Log.debug(tag, message);
                break;

            case VERBOSE:
                Log.trace(tag, message);
                break;
        }
    }

    /**
     * Start the Core processing. This should be called after the initial set of extensions have been registered.
     * <p>
     * This call will wait for any outstanding registrations to complete and then start event processing.
     * You can use the callback to kickoff additional operations immediately after any operations kicked off during registration.
     * You shouldn't call this method more than once in your app, if so, sdk will ignore it and print error log.
     *
     * @param completionCallback An optional {@link AdobeCallback} invoked after registrations are completed
     */
    public static void start(final AdobeCallback completionCallback) {
        synchronized (mutex) {
            if (core == null) {
                Log.debug(TAG, "Failed to start SDK (%s)", NULL_CONTEXT_MESSAGE);

                if (completionCallback != null & completionCallback instanceof AdobeCallbackWithError) {
                    ((AdobeCallbackWithError) completionCallback).fail(AdobeError.EXTENSION_NOT_INITIALIZED);
                }

                return;
            }

            core.start(completionCallback);
        }
    }

    // ========================================================
    // Configuration methods
    // ========================================================


    public static void configureWithAppID(final String appId) {
        if (core == null) {
            Log.debug(TAG, "Failed to set Adobe App ID (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.configureWithAppID(appId);
    }


    /**
     * Registers an extension class which has {@code Extension} as parent.
     * <p>
     * In order to ensure that your extension receives all the internal events, this method needs
     * to be called after {@link MobileCore#setApplication(Application)} is called, but before
     * any other method in this class.
     *
     * @param extensionClass a class whose parent is {@link Extension}
     * @param errorCallback  an optional {@link ExtensionErrorCallback} for the eventuality of an error,
     *                       called when this method returns false
     * @return {@code boolean} indicating if the provided parameters are valid and no error occurs
     */
    public static boolean registerExtension(final Class<? extends Extension> extensionClass,
                                            final ExtensionErrorCallback<ExtensionError> errorCallback) {
        if (core == null) {
            Log.debug(TAG, "Failed to register the extension. (%s)", NULL_CONTEXT_MESSAGE);

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
            }

            return false;
        }

        if (extensionClass == null) {
            if (errorCallback != null) {
                errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
            }

            return false;
        }

        core.registerExtension(extensionClass, errorCallback);
        return true;
    }

    /**
     * Called by the extension public API to dispatch an event for other extensions or the internal SDK to consume.
     *
     * @param event         required parameter, {@link Event} instance to be dispatched, should not be null
     * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred during dispatching
     * @return {@code boolean} indicating if the the event dispatching operation succeeded
     */
    public static boolean dispatchEvent(final Event event, final ExtensionErrorCallback<ExtensionError> errorCallback) {
        if (core == null) {
            Log.debug(TAG, "Failed to dispatch event. (%s)", NULL_CONTEXT_MESSAGE);

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
            }

            return false;
        }

        return core.dispatchEvent(event, errorCallback);
    }

    /**
     * This method will be used when the provided {@code Event} is used as a trigger and a response event
     * is expected in return. The returned event needs to be sent using
     * {@link #dispatchResponseEvent(Event, Event, ExtensionErrorCallback)}.
     * <p>
     * Passes an {@link ExtensionError} to {@code errorCallback} if {@code event} or
     * {@code responseCallback} are null.
     *
     * @param event            required parameter, {@link Event} instance to be dispatched, used as a trigger
     * @param responseCallback required parameters, {@link AdobeCallback} to be called with the response event received
     * @param errorCallback    optional {@link ExtensionErrorCallback} which will be called if an error occurred during dispatching
     * @return {@code boolean} indicating if the the event dispatching operation succeeded
     * @see MobileCore#dispatchResponseEvent(Event, Event, ExtensionErrorCallback)
     */
    public static boolean dispatchEventWithResponseCallback(final Event event,
                                                            final AdobeCallback<Event> responseCallback,
                                                            final ExtensionErrorCallback<ExtensionError> errorCallback) {
        if (core == null) {
            Log.debug(TAG, "Failed to dispatch event with a response callback. (%s)", NULL_CONTEXT_MESSAGE);

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
            }

            return false;
        }

        // the core will validate and copy this event
        return core.dispatchEventWithResponseCallback(event, responseCallback, errorCallback);
    }

    /**
     * This method will be used when the provided {@code Event} is used as a trigger and a response event
     * is expected in return. The returned event needs to be sent using
     * {@link #dispatchResponseEvent(Event, Event, ExtensionErrorCallback)}.
     * <p>
     * Passes an {@link AdobeError} to {@link AdobeCallbackWithError#fail(AdobeError)} if {@code event} is null.
     * Passes an {@link AdobeError} to {@link AdobeCallbackWithError#fail(AdobeError)} if {@code event} processing timeout occurs.
     *
     * @param event            required parameter, {@link Event} instance to be dispatched, used as a trigger
     * @param responseCallback required parameters, {@link AdobeCallback} to be called with the response event received
     * @see MobileCore#dispatchResponseEvent(Event, Event, ExtensionErrorCallback)
     */
    public static void dispatchEventWithResponseCallback(final Event event,
                                                         final AdobeCallbackWithError<Event> responseCallback) {
        if (core == null) {
            Log.debug(TAG, "Failed to dispatch event with a response callback. (%s)", NULL_CONTEXT_MESSAGE);

            if (responseCallback != null) {
                responseCallback.fail(AdobeError.UNEXPECTED_ERROR);
            }

            return;
        }

        if (event == null) {
            Log.debug(TAG, "Failed to dispatch event with a response callback: the given event is null ");

            if (responseCallback != null) {
                responseCallback.fail(AdobeError.UNEXPECTED_ERROR);
            }

            return;
        }

        if (responseCallback == null) {
            Log.warning(TAG,
                    "Failed to dispatch event with a response callback: the given callback (AdobeCallbackWithError) object is null ");
            return;
        }

        // the core will validate and copy this event
        core.dispatchEventWithResponseCallback(event, responseCallback);
    }

    /**
     * Dispatches a response event for a paired event that was sent to {@code dispatchEventWithResponseCallback}
     * and received by an extension listener {@code hear} method.
     * <p>
     * Passes an {@link ExtensionError} to {@code errorCallback} if {@code responseEvent} or {@code requestEvent} are null.
     * <p>
     * Note: The {@code responseEvent} will not be sent to any listeners, it is sent only to the response callback registered
     * using {@link MobileCore#dispatchEventWithResponseCallback(Event, AdobeCallback, ExtensionErrorCallback)}.
     *
     * @param responseEvent required parameter, {@link Event} instance to be dispatched as a response for the
     *                      event sent using {@link MobileCore#dispatchEventWithResponseCallback(Event, AdobeCallback, ExtensionErrorCallback)}
     * @param requestEvent  required parameter, the event sent using
     *                      {@link MobileCore#dispatchEventWithResponseCallback(Event, AdobeCallback, ExtensionErrorCallback)}
     * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred during dispatching
     * @return {@code boolean} indicating if the the event dispatching operation succeeded
     * @see MobileCore#dispatchEventWithResponseCallback(Event, AdobeCallback, ExtensionErrorCallback)
     */
    public static boolean dispatchResponseEvent(final Event responseEvent, final Event requestEvent,
                                                final ExtensionErrorCallback<ExtensionError> errorCallback) {

        if (core == null) {
            Log.debug(TAG, "Failed to dispatch the response event. (%s)", NULL_CONTEXT_MESSAGE);

            if (errorCallback != null) {
                errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
            }

            return false;
        }

        // the core will validate and copy this events
        return core.dispatchResponseEvent(responseEvent, requestEvent, errorCallback);
    }

    /**
     * Load configuration from the file in the assets folder. SDK automatically reads config from `ADBMobileConfig.json` file if
     * it exists in the assets folder. Use this API only if the config needs to be read from a different file.
     * <p>
     * On application relaunch, the configuration from the file at {@code filepath} is not preserved and this method must be called
     * again if desired.
     * <p>
     * On failure to read the file or parse the JSON contents, the existing configuration remains unchanged.
     * <p>
     * Calls to this API will replace any existing SDK configuration except those set using
     * {@link #updateConfiguration(Map)} or {@link #setPrivacyStatus(MobilePrivacyStatus)}.
     * Configuration updates made using {@link #updateConfiguration(Map)} and {@link #setPrivacyStatus(MobilePrivacyStatus)}
     * are always applied on top of configuration changes made using this API.
     * absolute
     *
     * @param fileName the name of the configure file in the assets folder. A value of {@code null} has no effect.
     */
    public static void configureWithFileInAssets(final String fileName) {
        if (core == null) {
            Log.debug(TAG, "Failed to load configuration with asset file (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.configureWithFileInAssets(fileName);
    }

    /**
     * Load configuration from local file.
     * <p>
     * Configure the SDK by reading a local file containing the JSON configuration.  On application relaunch,
     * the configuration from the file at {@code filepath} is not preserved and this method must be called again if desired.
     * <p>
     * On failure to read the file or parse the JSON contents, the existing configuration remains unchanged.
     * <p>
     * Calls to this API will replace any existing SDK configuration except those set using
     * {@link #updateConfiguration(Map)} or {@link #setPrivacyStatus(MobilePrivacyStatus)}.
     * Configuration updates made using {@link #updateConfiguration(Map)} and {@link #setPrivacyStatus(MobilePrivacyStatus)}
     * are always applied on top of configuration changes made using this API.
     *
     * @param filepath absolute path to a local configuration file. A value of {@code null} has no effect.
     */
    public static void configureWithFileInPath(final String filepath) {
        if (core == null) {
            Log.debug(TAG, "Failed to load configuration with file path (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.configureWithFileInPath(filepath);
    }

    /**
     * Update specific configuration parameters.
     * <p>
     * Update the current SDK configuration with specific key/value pairs. Keys not found in the current
     * configuration are added. Configuration updates are preserved and applied over existing or new
     * configurations set by calling {@link #configureWithAppID(String)} or {@link #configureWithFileInPath(String)},
     * even across application restarts.
     * <p>
     * Using {@code null} values is allowed and effectively removes the configuration parameter from the current configuration.
     *
     * @param configMap configuration key/value pairs to be updated or added. A value of {@code null} has no effect.
     */
    public static void updateConfiguration(final Map<String, Object> configMap) {
        if (core == null) {
            Log.debug(TAG, "Failed to update configuration (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.updateConfiguration(configMap);
    }

    /**
     * Clear the changes made by {@link #updateConfiguration(Map)} and {@link #setPrivacyStatus(MobilePrivacyStatus)}
     * to the initial configuration provided either by {@link #configureWithAppID(String)}
     * or {@link #configureWithFileInPath(String)} or {@link #configureWithFileInAssets(String)}
     */
    public static void clearUpdatedConfiguration() {
        if (core == null) {
            Log.debug(TAG, "Failed to clear updated configuration (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.clearUpdatedConfiguration();
    }

    /**
     * Set the Adobe Mobile Privacy status.
     * <p>
     * Sets the {@link MobilePrivacyStatus} for this SDK. The set privacy status is preserved and applied over any new
     * configuration changes from calls to {@link #configureWithAppID(String)} or {@link #configureWithFileInPath(String)},
     * even across application restarts.
     *
     * @param privacyStatus {@link MobilePrivacyStatus} to be set to the SDK
     * @see MobilePrivacyStatus
     */
    public static void setPrivacyStatus(final MobilePrivacyStatus privacyStatus) {
        if (core == null) {
            Log.debug(TAG, "Failed to set privacy status (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.setPrivacyStatus(privacyStatus);
    }

    /**
     * Get the current Adobe Mobile Privacy Status.
     * <p>
     * Gets the currently configured {@link MobilePrivacyStatus} and passes it as a parameter to the given
     * {@link AdobeCallback#call(Object)} function.
     *
     * @param callback {@link AdobeCallback} instance which is invoked with the configured privacy status as a parameter
     * @see AdobeCallback
     * @see MobilePrivacyStatus
     */
    public static void getPrivacyStatus(final AdobeCallback<MobilePrivacyStatus> callback) {
        if (core == null) {
            Log.debug(TAG, "Failed to retrieve the privacy status (%s)", NULL_CONTEXT_MESSAGE);

            if (callback != null & callback instanceof AdobeCallbackWithError) {
                ((AdobeCallbackWithError) callback).fail(AdobeError.EXTENSION_NOT_INITIALIZED);
            }

            return;
        }

        core.getPrivacyStatus(callback);
    }

    /**
     * Retrieve all identities stored by/known to the SDK in a JSON {@code String} format.
     *
     * @param callback {@link AdobeCallback} instance which is invoked with all the known identifier in JSON {@link String} format
     * @see AdobeCallback
     */
    public static void getSdkIdentities(final AdobeCallback<String> callback) {
        if (callback == null) {
            Log.debug(TAG, "%s (Callback), provide a callback to retrieve the all SDK identities", Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        if (core == null) {
            Log.debug(TAG, "Failed to retrieve the all SDK identities (%s)", NULL_CONTEXT_MESSAGE);

            if (callback != null & callback instanceof AdobeCallbackWithError) {
                ((AdobeCallbackWithError) callback).fail(AdobeError.EXTENSION_NOT_INITIALIZED);
            }

            return;
        }

        core.getSdkIdentities(callback);
    }

    // ========================================================
    // Generic methods
    // ========================================================


    /**
     * This method dispatches an Analytics track {@code action} event
     * <p>
     * Actions represent events that occur in your application that you want to measure; the corresponding metrics will
     * be incremented each time the event occurs. For example, you may want to track when an user click on the login
     * button or a certain article was viewed.
     * <p>
     *
     * @param action      {@code String} containing the name of the action to track
     * @param contextData {@code Map<String, String>} containing context data to attach on this hit
     */
    public static void trackAction(final String action, final Map<String, String> contextData) {
        if (core == null) {
            Log.debug(TAG, "Failed to track action %s (%s)", action, NULL_CONTEXT_MESSAGE);
            return;
        }

        core.trackAction(action, contextData);
    }

    /**
     * This method dispatches an Analytics track {@code state} event
     * <p>
     * States represent different screens or views of your application. When the user navigates between application pages,
     * a new track call should be sent with current state name. Tracking state name is typically called from an
     * Activity in the onResume method.
     * <p>
     *
     * @param state       {@code String} containing the name of the state to track
     * @param contextData contextData {@code Map<String, String>} containing context data to attach on this hit
     */
    public static void trackState(final String state, final Map<String, String> contextData) {
        if (core == null) {
            Log.debug(TAG, "Failed to track state %s (%s)", state, NULL_CONTEXT_MESSAGE);
            return;
        }

        core.trackState(state, contextData);
    }

    /**
     * This method dispatches an event to notify the SDK of a new {@code advertisingIdentifier}
     *
     * @param advertisingIdentifier {@code String} representing Android advertising identifier
     */
    public static void setAdvertisingIdentifier(final String advertisingIdentifier) {
        if (core == null) {
            Log.debug(TAG, "Failed to set advertising identifier (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.setAdvertisingIdentifier(advertisingIdentifier);
    }

    /**
     * This method dispatches an event to notify the SDK of a new {@code pushIdentifier}
     *
     * @param pushIdentifier {@code String} representing the new push identifier
     */
    public static void setPushIdentifier(final String pushIdentifier) {
        if (core == null) {
            Log.debug(TAG, "Failed to set push identifier (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.setPushIdentifier(pushIdentifier);
    }


    /**
     * Start/resume lifecycle session.
     * <p>
     * Start a new lifecycle session or resume a previously paused lifecycle session. If a previously paused session
     * timed out, then a new session is created. If a current session is running, then calling this method does nothing.
     * <p>
     * Additional context data may be passed when calling this method. Lifecycle data and any additional data are
     * sent as context data parameters to Analytics, to Target as mbox parameters, and for Audience Manager they are
     * sent as customer variables. Any additional data is also used by the Rules Engine when processing rules.
     * <p>
     * This method should be called from the Activity onResume method.
     *
     * @param additionalContextData optional additional context for this session.
     */
    public static void lifecycleStart(final Map<String, String> additionalContextData) {
        if (core == null) {
            Log.debug(TAG, "Failed to start lifecycle session (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.lifecycleStart(additionalContextData);
    }

    /**
     * Pause/stop lifecycle session.
     * <p>
     * Pauses the current lifecycle session. Calling pause on an already paused session updates the paused timestamp,
     * having the effect of resetting the session timeout timer. If no lifecycle session is running, then calling
     * this method does nothing.
     * <p>
     * A paused session is resumed if {@link #lifecycleStart(Map)} is called before the session timeout. After
     * the session timeout, a paused session is closed and calling {@link #lifecycleStart(Map)} will create
     * a new session. The session timeout is defined by the {@code lifecycle.sessionTimeout} configuration parameter.
     * If not defined, the default session timeout is five minutes.
     * <p>
     * This method should be called from the Activity onPause method.
     */
    public static void lifecyclePause() {
        if (core == null) {
            Log.debug(TAG, "Failed to pause lifecycle session (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.lifecyclePause();
    }

    /**
     * Collect PII data. Although using this call enables collection of PII data, the SDK does not
     * automatically send the data to any Adobe endpoint.
     *
     * @param data the map containing the PII data to be collected
     */
    public static void collectPii(final Map<String, String> data) {
        if (core == null) {
            Log.debug(TAG, "Failed to collect PII (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.collectPii(data);
    }

    /**
     * Sets the resource Id for small icon.
     *
     * @param resourceID the resource Id of the icon
     */
    public static void setSmallIconResourceID(final int resourceID) {
        App.setSmallIconResourceID(resourceID);
    }

    /**
     * Sets the resource Id for small icon.
     *
     * @param resourceID the resource Id of the icon
     */
    public static void setLargeIconResourceID(final int resourceID) {
        App.setLargeIconResourceID(resourceID);
    }

    /**
     * Collects message data from various points in the application.
     * <p>
     * This method can be invoked to support the following use cases:
     * <ol>
     *      <li>Tracking Push Message receive and click.</li>
     *      <li>Tracking Local Notification receive and click.</li>
     * </ol>
     * <p>
     * The message tracking information can be supplied in the {@code messageInfo} Map. For scenarios where the application
     * is launched as a result of notification click, {@link #collectLaunchInfo(Activity)} will be invoked with the target
     * Activity and message data will be extracted from the Intent extras.
     *
     * @param messageInfo {@code Map<String, Object>} containing message tracking information
     */
    public static void collectMessageInfo(final Map<String, Object> messageInfo) {
        if (core == null) {
            Log.debug(TAG, "Failed to collect Message Info (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.collectData(messageInfo);
    }

    /**
     * Collects data from the Activity / context to be used later by the SDK.
     * <p>
     * This method marshals the {@code activity} instance and extracts the intent data / extras. It should be called to support
     * the following use cases:
     * <ol>
     *      <li>Tracking Deep Link click-through
     *          <ul>
     *              <li>Update AndroidManifest.xml to support intent-filter in the activity with the intended action and type of data.</li>
     *              <li>Handle the intent in the activity.</li>
     *              <li>Pass activity with deepLink intent to SDK in {@code collectLaunchInfo}.</li>
     *          </ul>
     *      </li>
     *      <li>Tracking Push Message click-through
     *          <ul>
     *              <li>Push message data must be added to the Intent used to open target activity on click-through.</li>
     *              <li>The data can be added in intent extras which is then collected by SDK when target activity is passed in {@code collectedLaunchInfo}.</li>
     *          </ul>
     *      </li>
     *      <li>Tracking Local Notification click-through
     *          <ul>
     *              <li>Add manifest-declared broadcast receiver {@code <receiver android:name=".LocalNotificationHandler" />} in your app.</li>
     *              <li>Pass notifications activity reference in {@code collectLaunchInfo}.</li>
     *          </ul>
     *      </li>
     * </ol>
     * <p>
     * Invoke this method from {@link Activity#onResume} callback in your activity.
     *
     * @param activity current {@link Activity} reference.
     */
    static void collectLaunchInfo(Activity activity) {
        if (core == null) {
            Log.debug(TAG, "Failed to collect Activity data (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        DataMarshaller marshaller = new DataMarshaller();
        marshaller.marshal(activity);
        core.collectData(marshaller.getData());
    }

    /**
     * Sets the SDK's current wrapper type. This API should only be used if
     * being developed on platforms such as React Native.
     * <p>
     * NOTE: {@link #setApplication(Application)} must be called before calling this method.
     *
     * @param wrapperType the type of wrapper being used.
     */
    public static void setWrapperType(WrapperType wrapperType) {
        if (core == null) {
            Log.warning(TAG, "Cannot set wrapper type, core is null. Make sure setApplication API is called.");
            return;
        }

        core.setWrapperType(wrapperType);
    }

    /**
     * Registers an event listener for the provided event type and source.
     *
     * @param eventType   required parameter, the event type as a valid string (not null or empty)
     * @param eventSource required parameter, the event source as a valid string (not null or empty)
     * @param callback    required parameter, {@link AdobeCallbackWithError#call(Object)} will be called when the event is heard
     */
    public static void registerEventListener(final String eventType, final String eventSource,
                                             final AdobeCallbackWithError<Event> callback) {
        if (core == null) {
            Log.debug(TAG, "Failed to register the event listener (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.registerEventListener(eventType, eventSource, callback);
    }

    /**
     * Clears all identifiers from Edge extensions and generates a new Experience Cloud ID (ECID).
     */
    public static void resetIdentities() {
        if (core == null) {
            Log.debug(TAG, "Failed to reset identities (%s)", NULL_CONTEXT_MESSAGE);
            return;
        }

        core.resetIdentities();
    }


}
