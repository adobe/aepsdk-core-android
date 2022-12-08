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

/** Class {@code Application} */
@SuppressWarnings("unused")
class XDMLifecycleApplication {

    private XDMLifecycleCloseTypeEnum closeType;
    private String id;
    private boolean isClose;
    private boolean isInstall;
    private boolean isLaunch;
    private boolean isUpgrade;
    private String name;
    private int sessionLength;
    private String version;

    XDMLifecycleApplication() {}

    Map<String, Object> serializeToXdm() {
        Map<String, Object> map = new HashMap<String, Object>();

        if (this.id != null) {
            map.put("id", this.id);
        }

        if (this.name != null) {
            map.put("name", this.name);
        }

        if (this.version != null) {
            map.put("version", this.version);
        }

        if (this.isClose) {
            map.put("isClose", this.isClose);
        }

        if (this.isInstall) {
            map.put("isInstall", this.isInstall);
        }

        if (this.isLaunch) {
            map.put("isLaunch", this.isLaunch);
        }

        if (this.isUpgrade) {
            map.put("isUpgrade", this.isUpgrade);
        }

        if (this.closeType != null) {
            map.put("closeType", this.closeType.toString());
        }

        if (this.sessionLength > 0) {
            map.put("sessionLength", this.sessionLength);
        }

        return map;
    }

    /**
     * Returns the CloseType property The type of the close event. A value of 'close' indicates the
     * application signaled the close event. A value of 'unknown' indicates the application was
     * launched without previously signaling it closed.
     *
     * @return {@link XDMLifecycleCloseTypeEnum} value or null if the property is not set
     */
    XDMLifecycleCloseTypeEnum getCloseType() {
        return this.closeType;
    }

    /**
     * Sets the CloseType property The type of the close event. A value of 'close' indicates the
     * application signaled the close event. A value of 'unknown' indicates the application was
     * launched without previously signaling it closed.
     *
     * @param newValue the new CloseType value
     */
    void setCloseType(final XDMLifecycleCloseTypeEnum newValue) {
        this.closeType = newValue;
    }

    /**
     * Returns the Application identifier property Identifier of the application.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getId() {
        return this.id;
    }

    /**
     * Sets the Application identifier property Identifier of the application.
     *
     * @param newValue the new Application identifier value
     */
    void setId(final String newValue) {
        this.id = newValue;
    }

    /**
     * Returns the Is Close property Indicates whether or not this is an application close event.
     *
     * @return boolean value
     */
    boolean getIsClose() {
        return this.isClose;
    }

    /**
     * Sets the Is Close property Indicates whether or not this is an application close event.
     *
     * @param newValue the new Is Close value
     */
    void setIsClose(final boolean newValue) {
        this.isClose = newValue;
    }

    /**
     * Returns the Is Install property Indicates whether or not this is an application install
     * event.
     *
     * @return boolean value
     */
    boolean getIsInstall() {
        return this.isInstall;
    }

    /**
     * Sets the Is Install property Indicates whether or not this is an application install event.
     *
     * @param newValue the new Is Install value
     */
    void setIsInstall(final boolean newValue) {
        this.isInstall = newValue;
    }

    /**
     * Returns the Is Launch property Indicates whether or not this is an application launch event.
     *
     * @return boolean value
     */
    boolean getIsLaunch() {
        return this.isLaunch;
    }

    /**
     * Sets the Is Launch property Indicates whether or not this is an application launch event.
     *
     * @param newValue the new Is Launch value
     */
    void setIsLaunch(final boolean newValue) {
        this.isLaunch = newValue;
    }

    /**
     * Returns the Is Upgrade property Indicates whether or not this is an application upgrade
     * event.
     *
     * @return boolean value
     */
    boolean getIsUpgrade() {
        return this.isUpgrade;
    }

    /**
     * Sets the Is Upgrade property Indicates whether or not this is an application upgrade event.
     *
     * @param newValue the new Is Upgrade value
     */
    void setIsUpgrade(final boolean newValue) {
        this.isUpgrade = newValue;
    }

    /**
     * Returns the Name property Name of the application.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getName() {
        return this.name;
    }

    /**
     * Sets the Name property Name of the application.
     *
     * @param newValue the new Name value
     */
    void setName(final String newValue) {
        this.name = newValue;
    }

    /**
     * Returns the Session Length property Reports the number of seconds that a previous application
     * session lasted based on how long the application was open and in the foreground. Session
     * length is a positive value with no max bound (no max session length)
     *
     * @return int value
     */
    int getSessionLength() {
        return this.sessionLength;
    }

    /**
     * Sets the Session Length property Reports the number of seconds that a previous application
     * session lasted based on how long the application was open and in the foreground. Session
     * length is a positive value with no max bound (no max session length)
     *
     * @param newValue the new Session Length value
     */
    void setSessionLength(final int newValue) {
        this.sessionLength = newValue;
    }

    /**
     * Returns the Version property Version of the application.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getVersion() {
        return this.version;
    }

    /**
     * Sets the Version property Version of the application.
     *
     * @param newValue the new Version value
     */
    void setVersion(final String newValue) {
        this.version = newValue;
    }
}
