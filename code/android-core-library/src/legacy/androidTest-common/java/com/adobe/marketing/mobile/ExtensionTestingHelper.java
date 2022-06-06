///*
//  Copyright 2022 Adobe. All rights reserved.
//  This file is licensed to you under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License. You may obtain a copy
//  of the License at http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under
//  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
//  OF ANY KIND, either express or implied. See the License for the specific language
//  governing permissions and limitations under the License.
// */
//
//package com.adobe.marketing.mobile;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
///**
// * Class {@link ExtensionTestingHelper} that defines the necessary helper methods to work with the third party extension  {@link TestableExtension} that extends {@link ExtensionListener} class of the Adobe Experience Platform SDK.
// * <p>
// * This class provides the following features to write automated tests for a third party extension
// * <p>
// * 1. necessary helper methods for the automated tests cases to work with an extension instance. The instance will be captured using the eventHub getActiveModules() method.
// * 2. necessary helper methods to get access to the listeners registered by the TestableExtension class. The listeners that are registered in the eventHub will be accessed with the getModuleListeners(Module) method.
// * 3. uses static countdown latch to wait for all the listeners to be registered before dispatching events
// * 4. has provisions for setting the shared state, retrieving the shared state, clearing the shared state
// *
// * @author Adobe
// * @version 5.0
// */
//
//
//public class ExtensionTestingHelper {
//
//    private static final String LOG_TAG = ExtensionTestingHelper.class.getSimpleName();
//    static AsyncHelper asyncHelper = new AsyncHelper();
//    static boolean isDispatched = false;
//    static String confirmExtensionUnregisteredCall;
//    static String confirmListenerUnregisteredCall;
//
//
//    /**
//     * Returns an CreateExtensionResponse object
//     * <p>
//     * This method helps registering a third party extension with a list of required listeners and
//     * retuns the status of creation as an CreateExtensionResponse object
//     *
//     * @param tExtensionName Name of the extension to be created
//     * @param listenerTypes  The list of listeners as a ListenerType to be registered as part of creating the third party extension
//     * @return returns an CreateExtensionResponse object as a result of creating the extension.
//     */
//
//    public CreateExtensionResponse registerExtension(String tExtensionName, List<ListenerType> listenerTypes) {
//        final String extensionName = tExtensionName;
//        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
//            @Override
//            public void error(final ExtensionError adbExtensionError) {
//                Log.debug(LOG_TAG, String.format("[registerExtension] Registration failed with error %s ",
//                        adbExtensionError.getErrorName()));
//            }
//        };
//        TestableExtension.setListOfListeners(listenerTypes);
//        TestableExtension.setNameCallback(new Callback() {
//            public String call() {
//                return extensionName;
//            }
//
//            ;
//        });
//        MobileCore.registerExtension(TestableExtension.class, errorCallback);
//        asyncHelper.waitForAppThreads(500, true);
//
//        if (TestableExtension.getExtensionUnexpectedError() != null) {
//            Log.debug(LOG_TAG, String.format("[registerExtension] Registration failed with error %s ",
//                    TestableExtension.getExtensionUnexpectedError().getMessage()));
//        }
//
//        return new CreateExtensionResponse(TestableExtension.getExtensionUnexpectedError(),
//                TestableExtension.createdExtensionName, TestableExtension.createdExtensionVersion);
//    }
//
//    /**
//     * Returns an Map<String, Extension> object
//     * <p>
//     * This method helps finding out all the third party extensions currently registered at the EventHub
//     *
//     * @return returns the list of Extensions registered as a Map object.
//     */
//
//    public static Map<String, Extension> getAllThirdpartyExtensions(EventHub eventhub) {
//
//        Map<String, Extension> thirdPartyExtensions = new HashMap<String, Extension>();
//        Collection<Module> allExtensions = eventhub.getActiveModules();
//        Iterator<Module> iterator = allExtensions.iterator();
//
//        while (iterator.hasNext()) {
//            Module currentModule = iterator.next();
//
//            if (currentModule instanceof Extension) {
//                ExtensionApi extensionApi = (ExtensionApi) currentModule;
//                Extension ext = extensionApi.getExtension();
//                thirdPartyExtensions.put(ext.getName(), ext);
//            }
//        }
//
//        return thirdPartyExtensions;
//    }
//
//    /**
//     * Returns a boolean value
//     * <p>
//     * This method helps to check if an extension is currently registered at the EventHub
//     *
//     * @param extensionName Name of the extension to be checked
//     * @return returns true if the given extension is registered otherwise false.
//     */
//
//    public static boolean isRegistered(String extensionName) {
//        return (getExtensionInstance(extensionName) != null);
//    }
//
//    /**
//     * Returns an Extension Object
//     * <p>
//     * This method helps to get the instance of an extension  currently registered at the EventHub
//     *
//     * @param extensionName Name of the extension
//     * @return returns instance of an extension if the given extension is registered otherwise a null object.
//     */
//
//    public static Extension getExtensionInstance(String extensionName) {
//        Map<String, Extension> thirdPartyExtensions = getAllThirdpartyExtensions();
//        return thirdPartyExtensions.get(extensionName);
//    }
//
//    /**
//     * Returns a boolean value
//     * <p>
//     * This method helps to unregister an extension that's currently registered at the EventHub
//     *
//     * @param extensionName Name of the extension to be checked
//     * @return returns true if the given extension got registered at the EventHub and successfully unregistered otherwise false.
//     */
//
//    public static boolean unregisterExtension(String extensionName) {
//        boolean status = false;
//        TestableExtension.confirmExtensionUnregisteredCall = "";
//        TestableListener.confirmListenerUnregisteredCall = "";
//        Extension testableExtension = getExtensionInstance(extensionName);
//
//        if (testableExtension != null) {
//            testableExtension.getApi().unregisterExtension();
//            asyncHelper.waitForAppThreads(500, true);
//            status = true;
//        }
//
//        confirmExtensionUnregisteredCall = TestableExtension.confirmExtensionUnregisteredCall;
//        confirmListenerUnregisteredCall = TestableListener.confirmListenerUnregisteredCall;
//        return status;
//    }
//
//    /**
//     * Returns all the registered listeners of an extension as a Collection Object
//     * <p>
//     * This method helps finding out the list of listeners that are registered as part of creating a third party extension.
//     *
//     * @param extensionName The name of the 3rd party extension as a String
//     * @return returns all the registered listeners of an extension as a Collection Object
//     */
//    public static Collection getRegisteredListeners(EventHub eventhub, String extensionName) {
//        ConcurrentLinkedQueue<EventListener> listeners = new ConcurrentLinkedQueue<EventListener>();
//        Extension testableExtension = getExtensionInstance(extensionName);
//
//        if (testableExtension != null) {
//            listeners = eventhub.getModuleListeners(testableExtension.getApi());
//        }
//
//        return listeners;
//    }
//
//    /**
//     * Returns the instance of the EventListener from the list of listeners registered by the extension.
//     * <p>
//     *
//     * @param extensionName The name of the extension.
//     * @param listenerType  The listerType that contains the EventType and EventSource of the listener.
//     * @return returns the instance of the EventListener from the list of listeners registered by the extension.
//     * If the expected listener type is not registered this method will return null.
//     */
//
//    public static EventListener getListenerInstance(String extensionName, ListenerType listenerType) {
//
//        Collection registeredListeners = getRegisteredListeners(extensionName);
//        Iterator<EventListener> listenersIterator = registeredListeners.iterator();
//
//        while (listenersIterator.hasNext()) {
//            EventListener eventListener = listenersIterator.next();
//
//            if ((eventListener.getEventType().getName().equalsIgnoreCase(listenerType.eventType))
//                    && (eventListener.getEventSource().getName().equalsIgnoreCase(listenerType.eventSource))) {
//                return eventListener;
//            }
//
//        }
//
//        return null;
//    }
//
//
//    /**
//     * Returns a boolean value as the status of the dispatching of an Event specified.
//     * <p>
//     * This method helps dispatching an event to the EventHub.
//     *
//     * @param listenerType The listerType that contains the EventType and EventSource of the Event to be dispatched.
//     * @param data         EventData to be dispatched.
//     * @return returns the status of  the dispatch of an Event as a boolean.
//     */
//    public static boolean dispatchAnEvent(ListenerType listenerType, Map<String, Object> data) {
//        isDispatched = true;
//        Event event = new Event.Builder("DispatchedEvent", listenerType.eventType,
//                listenerType.eventSource).setEventData(data).build();
//        ExtensionErrorCallback<ExtensionError> dispatchCallback = new ExtensionErrorCallback<ExtensionError>() {
//            @Override
//            public void error(final ExtensionError ec) {
//                Log.debug(LOG_TAG, String.format("[dispatchAnEvent] Dispatch failed with error %s ", ec.getErrorCode()));
//                isDispatched = false;
//            }
//        };
//        MobileCore.dispatchEvent(event, dispatchCallback);
//        asyncHelper.waitForAppThreads(500, true);
//        return isDispatched;
//    }
//
//    /**
//     * Returns an Event Object of the Listener that's registered by a Third party extension.
//     * <p>
//     * This method helps confirming if the Event that's dispatched  was listened by the specified listener.
//     * returns  an Event Object of the Listener that's registered by a Third party extension.
//     *
//     * @param extensionName The name of the extension as a String type that owns the listener that's being checked.
//     * @param listener      The name of the listener as a ListenerType which is being checked to confirm whether it received the event that gets published with dispatchEvent call
//     * @return returns an Event Object of the Listener that's registered by a Third party extension.
//     * The caller can access the Event Type, Event Source, and Event Data with  getEventType().getName(), getEventSource().getName() + getEventData() methods
//     */
//    public static Event getLastEventHeardByListener(String extensionName, ListenerType listener) {
//        List<Event> events = getAllEventsHeardByListener(extensionName, listener);
//        return ((events.size() > 0) ? events.get(events.size() - 1) : null);
//    }
//
//    /**
//     * Returns an Event Object of the Listener that's registered by a Third party extension.
//     * <p>
//     * This method helps confirming if the Event that's dispatched  was listened by the specified listener.
//     * returns  an Event Object of the Listener that's registered by a Third party extension.
//     *
//     * @param extensionName The name of the extension as a String type that owns the listener that's being checked.
//     * @param listener      The name of the listener as a ListenerType which is being checked to confirm whether it received the event that gets published with dispatchEvent call
//     * @return returns an Event Object of the Listener that's registered by a Third party extension.
//     * The caller can access the Event Type, Event Source, and Event Data with  getEventType().getName(), getEventSource().getName() + getEventData() methods
//     */
//    public static List<Event> getAllEventsHeardByListener(String extensionName, ListenerType listener) {
//        Collection registeredListeners = new ConcurrentLinkedQueue<EventListener>();
//        registeredListeners = getRegisteredListeners(extensionName);
//        Iterator<ExtensionListener> listenersIterator = registeredListeners.iterator();
//        TestableListener listenerType = null;
//
//        while (listenersIterator.hasNext()) {
//            listenerType = (TestableListener) listenersIterator.next();
//
//            if ((listenerType != null) &&
//                    (listenerType.getEventType().getName().equalsIgnoreCase(listener.eventType)
//                            && listenerType.getEventSource().getName().equalsIgnoreCase(listener.eventSource))) {
//                return listenerType.getReceivedEvents();
//            }
//        }
//
//        return new ArrayList<>();
//    }
//}
