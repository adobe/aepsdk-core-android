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

package com.adobe.marketing.mobile.lifecycle;

import java.util.HashMap;
import java.util.Map;

/** This class is intended to be used by the {@link LifecycleV2Extension}. */
@SuppressWarnings("unused")
class XDMLifecycleMobileDetails {

    private XDMLifecycleApplication application;
    private XDMLifecycleDevice device;
    private XDMLifecycleEnvironment environment;
    private String eventType;
    private java.util.Date timestamp;

    XDMLifecycleMobileDetails() {}

    Map<String, Object> serializeToXdm() {
        Map<String, Object> map = new HashMap<String, Object>();

        if (this.application != null) {
            final Map<String, Object> applicationData = this.application.serializeToXdm();

            if (applicationData != null && !applicationData.isEmpty()) {
                map.put("application", applicationData);
            }
        }

        if (this.device != null) {
            final Map<String, Object> deviceData = this.device.serializeToXdm();

            if (deviceData != null && !deviceData.isEmpty()) {
                map.put("device", deviceData);
            }
        }

        if (this.environment != null) {
            final Map<String, Object> environmentData = this.environment.serializeToXdm();

            if (environmentData != null && !environmentData.isEmpty()) {
                map.put("environment", environmentData);
            }
        }

        if (this.eventType != null) {
            map.put("eventType", this.eventType);
        }

        if (this.timestamp != null) {
            map.put("timestamp", LifecycleUtil.dateTimeISO8601String(this.timestamp));
        }

        return map;
    }

    /**
     * Returns the Application property
     *
     * @return {@link XDMLifecycleApplication} value or null if the property is not set
     */
    XDMLifecycleApplication getApplication() {
        return this.application;
    }

    /**
     * Sets the Application property
     *
     * @param newValue the new Application value
     */
    void setApplication(final XDMLifecycleApplication newValue) {
        this.application = newValue;
    }

    /**
     * Returns the Device property An identified device, application or device browser instance that
     * is trackable across sessions, normally by cookies.
     *
     * @return {@link XDMLifecycleDevice} value or null if the property is not set
     */
    XDMLifecycleDevice getDevice() {
        return this.device;
    }

    /**
     * Sets the Device property An identified device, application or device browser instance that is
     * trackable across sessions, normally by cookies.
     *
     * @param newValue the new Device value
     */
    void setDevice(final XDMLifecycleDevice newValue) {
        this.device = newValue;
    }

    /**
     * Returns the Environment property Information about the surrounding situation the event
     * observation occurred in, specifically detailing transitory information such as the network or
     * software versions.
     *
     * @return {@link XDMLifecycleEnvironment} value or null if the property is not set
     */
    XDMLifecycleEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * Sets the Environment property Information about the surrounding situation the event
     * observation occurred in, specifically detailing transitory information such as the network or
     * software versions.
     *
     * @param newValue the new Environment value
     */
    void setEnvironment(final XDMLifecycleEnvironment newValue) {
        this.environment = newValue;
    }

    /**
     * Returns the Event Type property The primary event type for this time-series record.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getEventType() {
        return this.eventType;
    }

    /**
     * Sets the Event Type property The primary event type for this time-series record.
     *
     * @param newValue the new Event Type value
     */
    void setEventType(final String newValue) {
        this.eventType = newValue;
    }

    /**
     * Returns the Timestamp property The time when an event or observation occurred.
     *
     * @return {@link java.util.Date} value or null if the property is not set
     */
    java.util.Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the Timestamp property The time when an event or observation occurred.
     *
     * @param newValue the new Timestamp value
     */
    void setTimestamp(final java.util.Date newValue) {
        this.timestamp = newValue;
    }
}
