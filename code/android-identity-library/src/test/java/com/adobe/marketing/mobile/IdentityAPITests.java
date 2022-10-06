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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import com.adobe.marketing.mobile.identity.IdentityExtension;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IdentityAPITests {

    @Test
    public void test_extensionVersion() {
        assertEquals("2.0.0", Identity.extensionVersion());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void test_registerExtension() throws Exception {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            // mock MobileCore.registerExtension()
            ArgumentCaptor<Class> extensionClassCaptor = ArgumentCaptor.forClass(Class.class);
            ArgumentCaptor<ExtensionErrorCallback> callbackCaptor = ArgumentCaptor.forClass(
                    ExtensionErrorCallback.class
            );
            mobileCoreMockedStatic
                    .when(() -> MobileCore.registerExtension(extensionClassCaptor.capture(), callbackCaptor.capture()))
                    .thenReturn(true);
            // call registerExtension() API
            Identity.registerExtension();
            // verify: happy
            assertNotNull(callbackCaptor.getValue());
            assertEquals(IdentityExtension.class, extensionClassCaptor.getValue());
            // verify: error callback was called
            callbackCaptor.getValue().error(ExtensionError.UNEXPECTED_ERROR);
        }
    }

    @Test
    public void test_syncIdentifier() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            Identity.syncIdentifier("type", "id", VisitorID.AuthenticationState.AUTHENTICATED);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertTrue(eventData.containsKey("visitoridentifiers"));
            HashMap<String, String> identifiers = new HashMap<>();
            identifiers.put("type", "id");
            assertEquals(identifiers, eventData.get("visitoridentifiers"));
            assertEquals(false, eventData.get("forcesync"));
            assertEquals(false, eventData.get("forcesync"));
            assertEquals(1, eventData.get("authenticationstate"));
            assertEquals(true, eventData.get("issyncevent"));
        }
    }

    @Test
    public void test_syncIdentifiers() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            HashMap<String, String> identifiers = new HashMap<>();
            identifiers.put("type1", "id1");
            identifiers.put("type2", "id2");
            Identity.syncIdentifiers(identifiers);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertTrue(eventData.containsKey("visitoridentifiers"));
            assertEquals(identifiers, eventData.get("visitoridentifiers"));
            assertEquals(false, eventData.get("forcesync"));
            assertEquals(false, eventData.get("forcesync"));
            assertEquals(0, eventData.get("authenticationstate"));
            assertEquals(true, eventData.get("issyncevent"));
        }
    }

    @Test
    public void test_syncIdentifiers_withAuthState() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            HashMap<String, String> identifiers = new HashMap<>();
            identifiers.put("type1", "id1");
            identifiers.put("type2", "id2");
            Identity.syncIdentifiers(identifiers, VisitorID.AuthenticationState.AUTHENTICATED);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertTrue(eventData.containsKey("visitoridentifiers"));
            assertEquals(identifiers, eventData.get("visitoridentifiers"));
            assertEquals(false, eventData.get("forcesync"));
            assertEquals(false, eventData.get("forcesync"));
            assertEquals(1, eventData.get("authenticationstate"));
            assertEquals(true, eventData.get("issyncevent"));
        }
    }

    @Test
    public void test_appendVisitorInfoForURL() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.appendVisitorInfoForURL("https://adobe.com", new AdobeCallbackWithError<String>() {
                @Override
                public void fail(AdobeError error) {

                }

                @Override
                public void call(String value) {

                }
            });
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertEquals(new Long(500), timeoutCaptor.getValue());
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertEquals("https://adobe.com", eventData.get("baseurl"));
        }
    }

    @Test
    public void test_appendVisitorInfoForURL_callbackIsNull() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.appendVisitorInfoForURL("https://adobe.com", null);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()), never());
        }
    }

    @Test
    public void test_getUrlVariables() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.getUrlVariables(new AdobeCallbackWithError<String>() {
                @Override
                public void fail(AdobeError error) {

                }

                @Override
                public void call(String value) {

                }
            });
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertEquals(new Long(500), timeoutCaptor.getValue());
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertEquals(true, eventData.get("urlvariables"));
        }
    }

    @Test
    public void test_getUrlVariables_callbackIsNull() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.getUrlVariables(null);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()), never());
        }
    }

    @Test
    public void test_getIdentifiers() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.getIdentifiers(new AdobeCallbackWithError<List<VisitorID>>() {
                @Override
                public void fail(AdobeError error) {

                }

                @Override
                public void call(List<VisitorID> value) {

                }
            });
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertEquals(new Long(500), timeoutCaptor.getValue());
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            assertNull(dispatchedEvent.getEventData());
        }
    }

    @Test
    public void test_getIdentifiers_callbackIsNull() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.getIdentifiers(null);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()), never());
        }
    }

    @Test
    public void test_getExperienceCloudId() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.getExperienceCloudId(new AdobeCallbackWithError<String>() {
                @Override
                public void fail(AdobeError error) {

                }

                @Override
                public void call(String value) {

                }
            });
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertEquals(new Long(500), timeoutCaptor.getValue());
            assertNotNull(dispatchedEvent);
            assertEquals("IdentityRequestIdentity", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.identity", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", dispatchedEvent.getSource());
            assertNull(dispatchedEvent.getEventData());
        }
    }

    @Test
    public void test_getExperienceCloudId_callbackIsNull() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
            Identity.getExperienceCloudId(null);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), timeoutCaptor.capture(), any()), never());
        }
    }


}
